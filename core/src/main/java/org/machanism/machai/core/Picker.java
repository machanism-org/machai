package org.machanism.machai.core;

import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.schema.BIndex;
import org.machanism.machai.schema.Classification;
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
import com.openai.models.ChatModel;

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

	private static ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

	private OpenAiEmbeddingModel embeddingModel;
	private MongoCollection<Document> collection;
	private MongoClient mongoClient;

	private String dbUrl = "cluster0.hivfnpr.mongodb.net/?appName=Cluster0";

	private GenAIProvider provider;

	public Picker(String instanceName, String connection) {
		this.provider = new GenAIProvider(ChatModel.GPT_5_MINI);

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

			Document bindexDocument = Document.parse(objectMapper.writeValueAsString(bindex.getClassification()))
					.append(BINDEX_PROPERTY_NAME, bindexJson)
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
		List<String> languages = classification.getLanguage().stream().map(l -> l.getName())
				.collect(Collectors.toList());
		String languagesQuery = StringUtils.join(languages, ", ");
		logger.info("- Languages: {}", languagesQuery);

		Collection<String> resultsByDescription = getResults(indexName, DESCRIPTION_EMBEDDING_PROPERTY_NAME, query,
				sourceLimits, Aggregates.match(Filters.in("language.name", languages)));

		List<String> domains = classification.getDomains();
		String domainsQuery = StringUtils.join(domains, ", ");
		logger.info("- Domains: {}", domainsQuery);
		Collection<String> resultsByDomains = getResults(indexName, DOMAIN_EMBEDDING_PROPERTY_NAME,
				domainsQuery,
				sourceLimits, null);
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

		Collection<String> results = docs.stream()
				.map(doc -> doc.getString("id"))
				.collect(Collectors.toList());

		return results;
	}
}