package org.machanism.machai.bindex;

import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;

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
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
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

/**
 * Performs Bindex registration, lookup, and semantic retrieval using embeddings and MongoDB.
 *
 * <p>This type supports two primary workflows:
 *
 * <ol>
 *   <li><strong>Registration</strong>: serialize and persist a {@link Bindex} into MongoDB, including
 *       a classification embedding vector used for semantic search.</li>
 *   <li><strong>Retrieval</strong>: classify a free-text query into one or more {@link Classification}
 *       objects using an LLM, then run vector search queries to retrieve relevant Bindexes and expand
 *       results using transitive dependencies.</li>
 * </ol>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Configurator config = ...;
 * try (Picker picker = new Picker("openai", null, config)) {
 *     List<Bindex> results = picker.pick("Find libraries for server-side logging");
 * }
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class Picker implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Picker.class);

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

	private final String dbUrl = "cluster0.hivfnpr.mongodb.net/?appName=Cluster0";
	private String embeddingModelName = "text-embedding-3-small";

	/** MongoDB field name used to store the serialized Bindex JSON payload. */
	public static final String BINDEX_PROPERTY_NAME = "bindex";

	private static final ResourceBundle PROMPT_BUNDLE = ResourceBundle.getBundle("prompts");

	private final MongoCollection<Document> collection;
	private final MongoClient mongoClient;

	private final GenAIProvider provider;

	private Double score = 0.9;
	private final Map<String, Double> scoreMap = new HashMap<>();

	/**
	 * Constructs a {@link Picker} for registration and semantic search.
	 *
	 * @param genai  GenAI provider identifier used for embedding and classification prompts
	 * @param uri    MongoDB connection URI; when {@code null}, a default URI is constructed using
	 *               {@code BINDEX_REG_PASSWORD} when available
	 * @param config configurator used to initialize the provider
	 * @throws IllegalArgumentException if {@code genai} or {@code config} is {@code null}
	 */
	public Picker(String genai, String uri, Configurator config) {
		if (genai == null) {
			throw new IllegalArgumentException("genai must not be null");
		}
		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}

		this.provider = GenAIProviderManager.getProvider(genai, config);
		FunctionToolsLoader.getInstance().applyTools(provider);

		String effectiveUri = uri;
		if (effectiveUri == null) {
			String bindexRegPassword = System.getenv("BINDEX_REG_PASSWORD");
			if (bindexRegPassword == null || bindexRegPassword.isEmpty()) {
				effectiveUri = "mongodb+srv://user:user@" + dbUrl;
			} else {
				effectiveUri = "mongodb+srv://machanismorg_db_user:" + bindexRegPassword + "@" + dbUrl;
			}
		}

		this.mongoClient = MongoClients.create(effectiveUri);
		MongoDatabase database = mongoClient.getDatabase(INSTANCENAME);
		this.collection = database.getCollection(CONNECTION);
	}

	/**
	 * Registers (inserts or updates) a Bindex document for the supplied {@link Bindex}.
	 *
	 * <p>Registration deletes any existing document with the same {@link Bindex#getId()} and inserts
	 * a fresh document containing:
	 *
	 * <ul>
	 *   <li>Core identity fields: {@code id}, {@code name}, {@code version}</li>
	 *   <li>Classification facets: domains, layers, normalized languages, integrations</li>
	 *   <li>A vector embedding under {@value #CLASSIFICATION_EMBEDDING_PROPERTY_NAME}</li>
	 * </ul>
	 *
	 * @param bindex Bindex instance to register
	 * @return generated database ID string
	 * @throws IllegalArgumentException if {@code bindex} is {@code null}
	 * @throws JsonProcessingException  if conversion to JSON fails
	 * @throws MongoCommandException    on MongoDB insert or connection errors
	 */
	public String create(Bindex bindex) throws JsonProcessingException {
		if (bindex == null) {
			throw new IllegalArgumentException("bindex must not be null");
		}

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
				LOGGER.warn("No language defined for: {}.", id);
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
				LOGGER.error("To register a Bindex, the BINDEX_REG_PASSWORD env property is required.");
			}
			throw e;
		}
	}

	/**
	 * Generates a BSON array representing the embedding of a classification for semantic search.
	 *
	 * @param classification classification instance
	 * @param dimensions     number of vector dimensions
	 * @return BSON array of vector values
	 * @throws IllegalArgumentException if {@code classification} is {@code null} or {@code dimensions} is not positive
	 * @throws JsonProcessingException  on embedding or serialization errors
	 */
	BsonArray getEmbeddingBson(Classification classification, int dimensions) throws JsonProcessingException {
		if (classification == null) {
			throw new IllegalArgumentException("classification must not be null");
		}
		if (dimensions <= 0) {
			throw new IllegalArgumentException("dimensions must be > 0");
		}

		String text = getClassificationText(classification);
		List<Double> descEmbedding = provider.embedding(text, dimensions);
		return new BsonArray(descEmbedding.stream().map(BsonDouble::new).collect(Collectors.toList()));
	}

	/**
	 * Looks up the registered database ID for a Bindex (if it exists).
	 *
	 * @param bindex Bindex instance
	 * @return MongoDB object id as string, or {@code null} if not present
	 * @throws IllegalArgumentException if {@code bindex} is {@code null}
	 */
	public String getRegistredId(Bindex bindex) {
		if (bindex == null) {
			throw new IllegalArgumentException("bindex must not be null");
		}
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
	 * Performs a semantic pick/search with a query string, retrieving Bindex results and transitive
	 * dependencies.
	 *
	 * @param query query string
	 * @return list of Bindex results related to the query (and their dependencies)
	 * @throws IllegalArgumentException if {@code query} is {@code null}
	 * @throws IOException              if classification or database retrieval fails
	 */
	public List<Bindex> pick(String query) throws IOException {
		if (query == null) {
			throw new IllegalArgumentException("query must not be null");
		}

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
			LOGGER.info("Layer: {} ({})", StringUtils.join(layers, ", "), languagesQuery);

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
			if (bindex != null) {
				dependencies.addAll(bindex.getDependencies());
			}
			return bindex;
		}).filter(b -> b != null).collect(Collectors.toList());

		Set<String> allDependencies = new HashSet<>();
		for (String bindexId : dependencies) {
			addDependencies(allDependencies, bindexId);
		}

		List<Bindex> dependenciesResult = allDependencies.stream().map(this::getBindex).filter(b -> b != null)
				.collect(Collectors.toList());

		pickResult.addAll(dependenciesResult);

		return pickResult;
	}

	/**
	 * Serializes the classification into a stable JSON string used for embedding and schema prompts.
	 *
	 * @param classification classification instance
	 * @return JSON string value of the classification
	 * @throws JsonProcessingException if serialization fails
	 */
	private String getClassificationText(Classification classification) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(classification);
	}

	/**
	 * Adds all transitive dependencies of a Bindex into the provided set.
	 *
	 * @param dependencies set of dependency IDs to accumulate
	 * @param bindexId     Bindex ID to explore
	 */
	void addDependencies(Set<String> dependencies, String bindexId) {
		Bindex bindex = getBindex(bindexId);
		if (bindex != null) {
			String id = bindex.getId();
			if (!dependencies.contains(id)) {
				dependencies.add(id);
				for (String dependencyId : bindex.getDependencies()) {
					addDependencies(dependencies, dependencyId);
				}
			}
		}
	}

	/**
	 * Performs a schema classification prompt using {@link GenAIProvider}.
	 *
	 * @param query search query string
	 * @return classification schema as a JSON string
	 * @throws IOException              if prompt or IO operation fails
	 * @throws JsonProcessingException  if JSON serialization fails
	 * @throws JsonMappingException     if JSON mapping fails
	 */
	private String getClassification(String query) throws IOException, JsonProcessingException, JsonMappingException {
		URL systemResource = Bindex.class.getResource(BindexBuilder.BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, "UTF8");
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode schemaJson = objectMapper.readTree(schema);
		JsonNode jsonNode = schemaJson.get("properties").get("classification");
		String classificationSchema = objectMapper.writeValueAsString(jsonNode);

		String classificationQuery = MessageFormat.format(PROMPT_BUNDLE.getString("classification_instruction"),
				classificationSchema, query);
		provider.prompt(classificationQuery);
		return provider.perform();
	}

	/**
	 * Retrieves a Bindex instance from the database by its Bindex id.
	 *
	 * @param id Bindex id (the {@code id} field in the stored document)
	 * @return parsed {@link Bindex}, or {@code null} if not present
	 * @throws IllegalArgumentException if {@code id} is {@code null} or the stored JSON cannot be parsed
	 */
	protected Bindex getBindex(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null");
		}

		Bindex result = null;
		Document doc = collection.find(Filters.eq("id", id)).first();
		if (doc != null) {
			String bindexStr = doc.getString(BINDEX_PROPERTY_NAME);
			try {
				result = new ObjectMapper().readValue(bindexStr, Bindex.class);
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException(e);
			}
		}

		return result;
	}

	/**
	 * Executes a vector search query for Bindexes by semantic embedding using MongoDB's aggregation
	 * pipeline.
	 *
	 * @param indexName    index name in MongoDB
	 * @param propertyPath embedding property field path
	 * @param query        classification query text
	 * @param dimensions   number of vector dimensions
	 * @param bsons        additional filters (language, layers, etc.)
	 * @return collection of result IDs ({@code name:version})
	 */
	private Collection<String> getResults(String indexName, String propertyPath, String query, int dimensions,
			Bson... bsons) {
		Iterable<Double> queryEmbedding = provider.embedding(query, dimensions);

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
			LOGGER.debug("BindexId: {}: {}", name, score);

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

		return libraryVersionMap.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue())
				.collect(Collectors.toList());
	}

	/**
	 * Normalizes a {@link Language} name for semantic aggregation queries.
	 *
	 * @param language language object
	 * @return normalized language name string
	 */
	static String getNormalizedLanguageName(Language language) {
		String lang = language.getName().toLowerCase().trim();
		lang = StringUtils.substringBefore(lang, "(").trim();
		return lang;
	}

	/**
	 * Sets the minimum similarity score for semantic vector search queries.
	 *
	 * @param score minimum similarity value
	 */
	public void setScore(Double score) {
		this.score = score;
	}

	/**
	 * Retrieves the similarity score for a Bindex result by its ID.
	 *
	 * @param id Bindex identifier
	 * @return score value for semantic result, or {@code null} if not present
	 */
	public Double getScore(String id) {
		return scoreMap.get(id);
	}

	/**
	 * Returns the embedding model name used by the provider.
	 *
	 * @return embedding model name
	 */
	public String getEmbeddingModelName() {
		return embeddingModelName;
	}

	/**
	 * Sets the embedding model name used by the provider.
	 *
	 * @param embeddingModelName embedding model name
	 */
	public void setEmbeddingModelName(String embeddingModelName) {
		this.embeddingModelName = embeddingModelName;
	}

	/**
	 * Closes the underlying MongoDB client.
	 */
	@Override
	public void close() {
		mongoClient.close();
	}
}
