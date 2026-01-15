package org.machanism.machai.bindex;

import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;
import org.machanism.machai.schema.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

/**
 * Picker handles Bindex registration, lookup, and semantic queries using
 * embeddings and MongoDB.
 * <p>
 * Provides registration and query operations for Bindex repositories,
 * supporting classification embedding for semantic retrieval and indexing.
 * <p>
 * Usage example:
 * 
 * <pre>
 * try (Picker picker = new Picker(provider)) {
 * 	List&lt;Bindex&gt; results = picker.pick("search query");
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class Picker implements Closeable {
	private static Logger logger = LoggerFactory.getLogger(Picker.class);

	private static final String INSTANCENAME = "machanism";
	private static final String CONNECTION = "bindex";
	private static final String INDEXNAME = "vector_index";
	private static final String LANGUAGES_PROPERTY_NAME = "languages";
	private static final String DOMAINS_PROPERTY_NAME = "domains";
	private static final String LAYERS_PROPERTY_NAME = "layers";
	private static final String INTEGRATIONS_PROPERTY_NAME = "integrations";

	/** Embedding vector dimensions for classification. */
	private static final int CLASSIFICATION_EMBEDDING_DIMENTIONS = 700;
	/** MongoDB field name for classification embeddings. */
	private static final String CLASSIFICATION_EMBEDDING_PROPERTY_NAME = "classification_embedding";
	/** Result limit for vector search operations. */
	private static final int VECTOR_SEARCH_LIMITS = 50;

	private String dbUrl = "cluster0.hivfnpr.mongodb.net/?appName=Cluster0";
	private String embeddingModelName = "text-embedding-3-small";

	public static final String BINDEX_PROPERTY_NAME = "bindex";

	private static ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

	private MongoCollection<Document> collection;
	private MongoClient mongoClient;

	private GenAIProvider provider;

	private Double score = 0.9;
	private Map<String, Double> scoreMap = new HashMap<>();

	/**
	 * Constructs a Picker for registration and semantic search.
	 *
	 * @param provider GenAIProvider instance used for embedding, schema
	 *                 classification, etc.
	 * @param uri      database URI. If the value is null, the default value will be
	 *                 used..
	 * @throws IllegalStateException If the required environment variables are
	 *                               missing for DB or OpenAI access
	 */
	public Picker(GenAIProvider provider, String uri) {
		this.provider = provider;

		if (uri == null) {
			String bindexRegPassword = System.getenv("BINDEX_REG_PASSWORD");
			if (bindexRegPassword == null || bindexRegPassword.isEmpty()) {
				uri = "mongodb+srv://user:user@" + dbUrl;
			} else {
				uri = "mongodb+srv://machanismorg_db_user:" + bindexRegPassword + "@" + dbUrl;
			}
		}

		mongoClient = MongoClients.create(uri);
		MongoDatabase database = mongoClient.getDatabase(INSTANCENAME);
		collection = database.getCollection(CONNECTION);
	}

	/**
	 * Closes the resource-backed connections.
	 *
	 * @throws IOException if closing the MongoDB client fails
	 */
	@Override
	public void close() throws IOException {
		mongoClient.close();
	}

	/**
	 * Registers (inserts or updates) a Bindex document for the supplied Bindex
	 * instance.
	 *
	 * @param bindex Bindex instance to register
	 * @return Generated database ID string
	 * @throws JsonProcessingException If conversion to JSON string fails or MongoDB
	 *                                 throws exception
	 * @throws MongoCommandException   On MongoDB insert or connection errors
	 */
	public String create(Bindex bindex) throws JsonProcessingException {
		try {

			ObjectMapper objectMapper = new ObjectMapper();
			String bindexJson = objectMapper.writeValueAsString(bindex);

			String id = bindex.getId();
			Bson filter = Filters.eq("id", id);
			collection.deleteOne(filter);

			Classification classification = bindex.getClassification();
			Set<String> languages = classification.getLanguages().stream().map(Picker::getNormalizedLanguageName)
					.distinct().collect(Collectors.toSet());

			Set<String> integrations = classification.getIntegrations().stream().map(e -> e.toLowerCase()).distinct()
					.collect(Collectors.toSet());

			if (languages.isEmpty()) {
				logger.warn("WARNING! No language defined for: {}.", id);
			}

			Document bindexDocument = new Document(BINDEX_PROPERTY_NAME, bindexJson).append("name", bindex.getName())
					.append("version", bindex.getVersion()).append(DOMAINS_PROPERTY_NAME, classification.getDomains())
					.append(LAYERS_PROPERTY_NAME, classification.getLayers()).append(LANGUAGES_PROPERTY_NAME, languages)
					.append(INTEGRATIONS_PROPERTY_NAME, integrations)
					.append(CLASSIFICATION_EMBEDDING_PROPERTY_NAME,
							getEmbeddingBson(bindex.getClassification(), CLASSIFICATION_EMBEDDING_DIMENTIONS))
					.append("id", bindex.getId());

			InsertOneResult result = collection.insertOne(bindexDocument);
			return result.getInsertedId().toString();

		} catch (MongoCommandException e) {
			String bindexRegPassword = System.getenv("BINDEX_REG_PASSWORD");
			if (bindexRegPassword == null || bindexRegPassword.isEmpty()) {
				logger.error("ERROR: To register  Bindex, the BINDEX_REG_PASSWORD env property is required.");
			}
			throw e;
		}
	}

	/**
	 * Generates a BSON array representing the embedding of a classification for
	 * semantic search.
	 *
	 * @param classification Classification instance
	 * @param dimensions     Number of vector dimensions
	 * @return BsonArray of vector values
	 * @throws JsonProcessingException On embedding or serialization errors
	 */
	BsonArray getEmbeddingBson(Classification classification, Integer dimensions) throws JsonProcessingException {
		String text = getClassificationText(classification);

		List<Double> descEmbedding = getEmbedding(text, dimensions);
		BsonArray bsonArray = new BsonArray(descEmbedding.stream().map(BsonDouble::new).collect(Collectors.toList()));
		return bsonArray;
	}

	/**
	 * Looks up the registered database ID for a Bindex (if it exists).
	 *
	 * @param bindex Bindex instance
	 * @return String database ID, or null if not present
	 */
	public String getRegistredId(Bindex bindex) {
		Document query = new Document("id", bindex.getId());
		FindIterable<Document> find = collection.find(query);
		Document document = find.first();
		String id = null;
		if (document != null) {
			id = ((ObjectId) document.get("_id")).toString();
		}
		return id;
	}

	/**
	 * Calculates OpenAI embedding vector for the given classification text.
	 *
	 * @param text       Classification text
	 * @param dimensions Required embedding vector dimensions
	 * @return List of Double values (embedding vector)
	 */
	private List<Double> getEmbedding(String text, Integer dimensions) {

		String embeddingApiKey = System.getenv("EMBEDDING_OPENAI_API_KEY");
		if (embeddingApiKey == null || embeddingApiKey.isEmpty()) {
			throw new IllegalStateException("EMBEDDING_OPENAI_API_KEY env variable is not set or is empty.");
		}

		String embeddingBaseUrl = System.getenv("EMBEDDING_OPENAI_BASE_URL");

		OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
				.apiKey(embeddingApiKey)
				.baseUrl(embeddingBaseUrl)
				.modelName(embeddingModelName)
				.timeout(java.time.Duration.ofSeconds(60))
				.dimensions(dimensions)
				.build();

		Response<Embedding> response = embeddingModel.embed(text);
		return response.content().vectorAsList().stream().map(Double::valueOf).collect(Collectors.toList());
	}

	/**
	 * Performs a semantic pick/search with a query string, retrieving Bindex
	 * results and dependencies.
	 *
	 * @param query Query string
	 * @return List of Bindex results related to the query (and their dependencies)
	 * @throws IOException If prompt or IO operation fails
	 */
	public List<Bindex> pick(String query) throws IOException {

		String classificationStr = getClassification(query);
		if (StringUtils.startsWith(classificationStr, "```json")) {
			classificationStr = StringUtils.substringBetween(classificationStr, "```json", "```");
		}
		Classification[] classifications = new ObjectMapper().readValue(classificationStr, Classification[].class);

		Collection<String> classificatioResults = new HashSet<>();
		for (Classification classification : classifications) {
			Set<String> languages = classification.getLanguages().stream().map(Picker::getNormalizedLanguageName)
					.distinct().collect(Collectors.toSet());

			List<Layer> layers = classification.getLayers();
			String languagesQuery = StringUtils.join(languages, ", ");
			logger.info("Layer: {} ({})", StringUtils.join(layers, ", "), languagesQuery);

			String classificationQuery = getClassificationText(classification);

			for (Layer layer : layers) {
				Collection<String> layerResults = getResults(INDEXNAME, CLASSIFICATION_EMBEDDING_PROPERTY_NAME,
						classificationQuery, CLASSIFICATION_EMBEDDING_DIMENTIONS,
						Aggregates.match(Filters.in(LANGUAGES_PROPERTY_NAME, languages)),
						Aggregates.match(Filters.in(LAYERS_PROPERTY_NAME, layer)));
				classificatioResults.addAll(layerResults);
			}
		}

		Set<String> dependencies = new HashSet<>();
		List<Bindex> pickResult = classificatioResults.stream().map(id -> {
			Bindex bindex = getBindex(id);
			List<String> dependencyList = bindex.getDependencies();
			dependencies.addAll(dependencyList);
			return bindex;
		}).collect(Collectors.toList());

		Set<String> allDependencies = new HashSet<>();
		for (String bindexId : dependencies) {
			addDependencies(allDependencies, bindexId);
		}

		List<Bindex> dependenciesResult = allDependencies.stream().map(id -> {
			return getBindex(id);
		}).collect(Collectors.toList());

		pickResult.addAll(dependenciesResult);

		return pickResult;
	}

	/**
	 * Generates the classification text for embedding and schema queries.
	 *
	 * @param classification Classification instance
	 * @return String JSON value of the classification
	 * @throws JsonProcessingException If serialization fails
	 */
	private String getClassificationText(Classification classification) throws JsonProcessingException {
		String classificationStr = new ObjectMapper().writeValueAsString(classification);
		return classificationStr;
	}

	/**
	 * Adds all transitive dependencies of a Bindex into the provided set.
	 *
	 * @param dependencies HashSet of dependency IDs to accumulate
	 * @param bindexId     Bindex ID to explore
	 */
	void addDependencies(Set<String> dependencies, String bindexId) {
		Bindex bindex = getBindex(bindexId);
		if (bindex != null) {
			String id = bindex.getId();
			if (!dependencies.contains(id)) {
				List<String> dependencyList = bindex.getDependencies();
				dependencies.add(id);
				for (String dependencyId : dependencyList) {
					addDependencies(dependencies, dependencyId);
				}
			}
		}
	}

	/**
	 * Performs a schema classification prompt using GenAIProvider.
	 *
	 * @param query Search query string
	 * @return Classification schema in String form
	 * @throws IOException, JsonProcessingException, JsonMappingException
	 */
	private String getClassification(String query) throws IOException, JsonProcessingException, JsonMappingException {
		URL systemResource = Bindex.class.getResource(BindexBuilder.BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, "UTF8");
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode schemaJson = objectMapper.readTree(schema);
		JsonNode jsonNode = schemaJson.get("properties").get("classification");
		String classificationSchema = objectMapper.writeValueAsString(jsonNode);

		String classificationQuery = MessageFormat.format(promptBundle.getString("classification_instruction"),
				classificationSchema, query);
		provider.prompt(classificationQuery);
		String classificationStr = provider.perform();
		return classificationStr;
	}

	/**
	 * Retrieves a Bindex instance from the database by its ID.
	 *
	 * @param id String identifier
	 * @return Bindex object, or null if not present in the DB
	 */
	private Bindex getBindex(String id) {
		Bindex result = null;
		Document doc = collection.find(com.mongodb.client.model.Filters.eq("id", id)).first();
		if (doc != null) {
			String bindexStr = doc.getString("bindex");
			try {
				result = new ObjectMapper().readValue(bindexStr, Bindex.class);
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException(e);
			}
		}

		return result;
	}

	/**
	 * Executes a vector search query for Bindexes by semantic embedding using
	 * MongoDB aggregation pipeline.
	 *
	 * @param indexName    Index name in MongoDB
	 * @param propertyPath Embedding property field path
	 * @param query        Classification query text
	 * @param dimensions   Number of vector dimensions
	 * @param bsons        Additional filters (language, layers, etc.)
	 * @return Collection of result IDs (name:version tuples)
	 */
	private Collection<String> getResults(String indexName, String propertyPath, String query, Integer dimensions,
			Bson... bsons) {
		Iterable<Double> queryEmbedding = getEmbedding(query, dimensions);

		List<Bson> pipeline = new ArrayList<>();

		pipeline.add(Aggregates.vectorSearch(fieldPath(propertyPath), queryEmbedding, indexName, VECTOR_SEARCH_LIMITS,
				exactVectorSearchOptions()));

		if (bsons != null) {
			for (Bson bson : bsons) {
				pipeline.add(bson);
			}
		}

		pipeline.add(Aggregates.project(
				Projections.fields(Projections.exclude("_id"), Projections.include("id"), Projections.include("name"),
						Projections.include("version"), Projections.metaVectorSearchScore("score"))));

		pipeline.add(Aggregates.match(Filters.gte("score", score)));

		List<Document> docs = collection.aggregate(pipeline).into(new ArrayList<>());

		Map<String, String> libraryVersionMap = new HashMap<>();
		for (Document doc : docs) {
			String id = doc.getString("id");
			String name = doc.getString("name");
			String version = doc.getString("version");

			Double score = doc.getDouble("score");
			scoreMap.put(id, score);
			logger.debug("BindexId: {}: {}", name, score);

			if (libraryVersionMap.containsKey(name)) {
				String existsVersion = libraryVersionMap.get(name);

				ComparableVersion v1 = new ComparableVersion(existsVersion);
				ComparableVersion v2 = new ComparableVersion(version);
				if (v1.compareTo(v2) > 0) {
					version = existsVersion;
				}
			}

			libraryVersionMap.put(name, version);
		}

		Collection<String> results = libraryVersionMap.entrySet().stream()
				.map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.toList());

		return results;
	}

	/**
	 * Normalizes a Language name for semantic aggregation queries.
	 *
	 * @param language Language object
	 * @return Normalized language name string
	 */
	static String getNormalizedLanguageName(Language language) {
		String lang = language.getName().toLowerCase().trim();
		lang = StringUtils.substringBefore(lang, "(").trim();
		return lang;
	}

	/**
	 * Manually set minimum score for semantic vector search queries.
	 *
	 * @param score Minimum similarity value
	 */
	public void setScore(Double score) {
		this.score = score;
	}

	/**
	 * Retrieves similarity score for a Bindex result by its ID.
	 *
	 * @param id Bindex identifier
	 * @return score value for semantic result
	 */
	public Double getScore(String id) {
		return scoreMap.get(id);
	}

	public String getEmbeddingModelName() {
		return embeddingModelName;
	}

	public void setEmbeddingModelName(String embeddingModelName) {
		this.embeddingModelName = embeddingModelName;
	}
}
