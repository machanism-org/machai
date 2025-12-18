package org.machanism.machai.core;

import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.schema.BIndex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.InsertOneResult;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.model.output.Response;

public class Picker implements Closeable {

	private static final OpenAiEmbeddingModelName EMBEDDING_MODEL_NAME = OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

	private static Logger logger = LoggerFactory.getLogger(Picker.class);

	public static final String BINDEX_PROPERTY_NAME = "bindex";
	public static final String DESCRIPTION_EMBEDDING_PROPERTY_NAME = "description_embedding";
	public static final String DOMAIN_EMBEDDING_PROPERTY_NAME = "domain_embedding";
	private static final String LANGUAGES_PROPERTY_NAME = "languages";
	private static final String DOMAINS_PROPERTY_NAME = "domains";

	private static ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

	private OpenAiEmbeddingModel embeddingModel;
	private MongoCollection<Document> collection;
	private MongoClient mongoClient;

	private String dbUrl = "cluster0.hivfnpr.mongodb.net/?appName=Cluster0";

	private GenAIProvider provider;

	public Picker(GenAIProvider provider, String instanceName, String connection) {
		this.provider = provider;

		String bindexRegPassword = System.getenv("BINDEX_REG_PASSWORD");
		String uri;
		if (bindexRegPassword == null || bindexRegPassword.isEmpty()) {
			uri = "mongodb+srv://user:user@" + dbUrl;
		} else {
			uri = "mongodb+srv://machanismorg_db_user:" + bindexRegPassword + "@" + dbUrl;
		}

		String apiKey = System.getenv("OPENAI_API_KEY");
		if (apiKey == null || apiKey.isEmpty()) {
			throw new IllegalStateException("OPEN_AI_API_KEY env variable is not set or is empty.");
		}

		embeddingModel = OpenAiEmbeddingModel.builder()
				.apiKey(apiKey)
				.modelName(EMBEDDING_MODEL_NAME)
				.timeout(java.time.Duration.ofSeconds(60))
				.dimensions(50)
				.build();

		mongoClient = MongoClients.create(uri);
		MongoDatabase database = mongoClient.getDatabase(instanceName);
		collection = database.getCollection(connection);
	}

	@Override
	public void close() throws IOException {
		mongoClient.close();
	}

	public String create(BIndex bindex) throws JsonProcessingException {
		try {

			ObjectMapper objectMapper = new ObjectMapper();
			String bindexJson = objectMapper.writeValueAsString(bindex);

			Bson filter = Filters.eq("id", bindex.getId());
			collection.deleteOne(filter);

			Classification classification = bindex.getClassification();
			Set<String> languages = classification.getLanguages().stream().map(Picker::getNormalizedLanguageName)
					.distinct().collect(Collectors.toSet());

			Document bindexDocument = new Document(BINDEX_PROPERTY_NAME, bindexJson)
					.append(DOMAINS_PROPERTY_NAME, classification.getDomains())
					.append(LANGUAGES_PROPERTY_NAME, languages)
					.append(DESCRIPTION_EMBEDDING_PROPERTY_NAME, getEmbeddingBson(bindex.getDescription()))
					.append(DOMAIN_EMBEDDING_PROPERTY_NAME, getEmbeddingBson(classification.getDomains()))
					.append("id", bindex.getId());

			InsertOneResult result = collection.insertOne(bindexDocument);
			return result.getInsertedId().toString();

		} catch (MongoCommandException e) {
			String bindexRegPassword = System.getenv("BINDEX_REG_PASSWORD");
			if (bindexRegPassword == null || bindexRegPassword.isEmpty()) {
				logger.error("ERROR: To register BIndex, the BINDEX_REG_PASSWORD env property is required.");
			}
			throw e;
		}
	}

	private BsonArray getEmbeddingBson(Object data) throws JsonProcessingException {
		String text;
		if (data instanceof String) {
			text = (String) data;
		} else {
			text = new ObjectMapper().writeValueAsString(data);
		}
		List<Double> descEmbedding = getEmbedding(text);
		BsonArray bsonArray = new BsonArray(
				descEmbedding.stream()
						.map(BsonDouble::new)
						.collect(Collectors.toList()));
		return bsonArray;
	}

	public String getRegistredId(BIndex bindex) {
		Document query = new Document("id", bindex.getId());
		FindIterable<Document> find = collection.find(query);
		Document document = find.first();
		String id = null;
		if (document != null) {
			id = ((ObjectId) document.get("_id")).toString();
		}
		return id;
	}

	private List<Double> getEmbedding(String text) {
		Response<Embedding> response = embeddingModel.embed(text);
		return response.content().vectorAsList().stream()
				.map(Double::new)
				.collect(Collectors.toList());
	}

	public List<BIndex> pick(String query, int limit) throws IOException {
		List<BIndex> arrayList = new ArrayList<>();
		String indexName = "vector_index";

		URL systemResource = BIndex.class.getResource(BIndexBuilder.BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, "UTF8");
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode schemaJson = objectMapper.readTree(schema);
		JsonNode jsonNode = schemaJson.get("properties").get("classification");
		String classificationSchema = objectMapper.writeValueAsString(jsonNode);

		String classificationInstruction = MessageFormat.format(promptBundle.getString("classification_instruction"),
				classificationSchema);
		String classificationQuery = provider.instructions(classificationInstruction).prompt(query).perform();
		Classification classification = new ObjectMapper().readValue(classificationQuery, Classification.class);

		int sourceLimits = limit * 4;

		logger.info("Detected classification:");
		Set<String> languages = classification.getLanguages().stream().map(Picker::getNormalizedLanguageName)
				.distinct().collect(Collectors.toSet());
		String languagesQuery = StringUtils.join(languages, ", ");
		logger.info("- Languages: {}", languagesQuery);

		Collection<String> resultsByDescription = getResults(indexName, DESCRIPTION_EMBEDDING_PROPERTY_NAME, query,
				sourceLimits, Aggregates.match(Filters.in(LANGUAGES_PROPERTY_NAME, languages)));

		List<String> domains = classification.getDomains();
		String domainsQuery = StringUtils.join(domains, ", ");
		logger.info("- Domains: {}", domainsQuery);
		Collection<String> resultsByDomains = getResults(indexName, DOMAIN_EMBEDDING_PROPERTY_NAME,
				domainsQuery,
				sourceLimits, Aggregates.match(Filters.in(LANGUAGES_PROPERTY_NAME, languages)));
		if (!resultsByDomains.isEmpty()) {
			resultsByDescription.retainAll(resultsByDomains);
		}

		if (resultsByDescription.isEmpty()) {
			logger.info("No results found.");
		} else {
			resultsByDescription.stream()
					.limit(limit)
					.forEach(id -> {
						BIndex bindex = getBindex(id);
						arrayList.add(bindex);
					});
		}

		return arrayList;
	}

	private BIndex getBindex(String id) {
		Document doc = collection.find(com.mongodb.client.model.Filters.eq("id", id)).first();
		String bindexStr = doc.getString("bindex");
		try {
			return new ObjectMapper().readValue(bindexStr, BIndex.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Collection<String> getResults(String indexName, String propertyPath, String query, long limit,
			Bson bson) {
		Iterable<Double> queryEmbedding = getEmbedding(query);

		List<Bson> pipeline = new ArrayList<>();

		pipeline.add(Aggregates.vectorSearch(
				fieldPath(propertyPath),
				queryEmbedding,
				indexName,
				limit,
				exactVectorSearchOptions()));

		if (bson != null) {
			pipeline.add(bson);
		}

		pipeline.add(Aggregates.project(Projections.fields(
				Projections.exclude("_id"),
				Projections.include("id"))));

		List<Document> docs = collection.aggregate(pipeline).into(new ArrayList<>());

		Map<String, String> libraryVersionMap = new HashMap<>();
		for (Document doc : docs) {
			String id = doc.getString("id");
			String name = StringUtils.substringBeforeLast(id, ":");
			String version = StringUtils.substringAfterLast(id, ":");
			
			if(libraryVersionMap.containsKey(name)) {
				String existsVersion = libraryVersionMap.get(name);
				
		        ComparableVersion v1 = new ComparableVersion(existsVersion);
		        ComparableVersion v2 = new ComparableVersion(version);
				if(v1.compareTo(v2) > 0) {
					version = existsVersion;
				} 
			}
			
			libraryVersionMap.put(name, version);
		}
		
		Collection<String> results = libraryVersionMap.entrySet().stream()
				.map(entry -> entry.getKey() + ":" + entry.getValue())
				.collect(Collectors.toList());

		return results;
	}

	private static String getNormalizedLanguageName(Language language) {
		String lang = language.getName().toLowerCase().trim();
		lang = StringUtils.substringBefore(lang, "(").trim();
		return lang;
	}
}