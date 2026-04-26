package org.machanism.machai.bindex;

import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;
import org.machanism.machai.schema.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.InsertOneResult;

/**
 * Performs Bindex registration, lookup, and semantic retrieval using embeddings
 * and MongoDB.
 */
public class Picker {

	private static final Logger LOGGER = LoggerFactory.getLogger(Picker.class);
	private static final String CLASSIFICATION_INSTRUCTION_PROP_NAME = "picker.classificationInstruction";
	private static final String INDEXNAME = "vector_index";
	private static final String LANGUAGES_PROPERTY_NAME = "languages";
	private static final String DOMAINS_PROPERTY_NAME = "domains";
	private static final String LAYERS_PROPERTY_NAME = "layers";
	private static final String INTEGRATIONS_PROPERTY_NAME = "integrations";
	private static final int CLASSIFICATION_EMBEDDING_DIMENTIONS = 700;
	private static final String CLASSIFICATION_EMBEDDING_PROPERTY_NAME = "classification_embedding";
	private static final int VECTOR_SEARCH_LIMITS = 250;
	public static final String BINDEX_PROPERTY_NAME = "bindex";
	private static final String VERSION_FIELD_NAME = "version";
	private static final String SCORE_FIELD_NAME = "score";
	private static final String ID_FIELD_NAME = "id";
	private static final String NAME_FIELD_NAME = "name";
	public static final String MODEL_PROP_NAME = "pick.model";
	public static final String SCORE_PROP_NAME = "pick.score";
	public static final Double DEFAULT_SCORE_VALUE = 0.85;

	private static final String DEFAULT_CLASSIFICATION_INSTRUCTION = "You are a system architect and must generate a\n"
			+ "JSON object with a classification having the following schema:**\n\n"
			+ "```json\n%s\n```\n\n"
			+ "You must analyze the user's request below and provide a JSON array with separate classifications for all **required levels**\n"
			+ "to find libraries that meet these requirements to build the application requested by the user.\n\n"
			+ "**User Request:**\n\n%s";

	private final MongoCollection<Document> collection;
	private final Genai provider;
	private Double score = DEFAULT_SCORE_VALUE;
	private final Map<String, Double> scoreMap = new HashMap<>();
	private Configurator configurator;

	/**
	 * Creates a picker backed by the configured Bindex repository and a named GenAI
	 * provider.
	 *
	 * @param genai the provider identifier used to resolve the GenAI implementation
	 * @param uri the repository URI
	 * @param config the project configuration used for repository and prompt settings
	 */
	public Picker(String genai, String uri, Configurator config) {
		if (genai == null) {
			throw new IllegalArgumentException("genai must not be null");
		}
		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}
		this.configurator = config;
		this.provider = GenaiProviderManager.getProvider(genai, config);
		FunctionToolsLoader.getInstance().applyTools(provider);
		this.collection = BindexRepository.getCollection(config);
	}

	/**
	 * Creates a picker with explicit dependencies.
	 *
	 * @param collection the MongoDB collection used to store and query Bindex documents
	 * @param provider the GenAI provider used for prompt execution and embeddings
	 */
	Picker(MongoCollection<Document> collection, Genai provider) {
		if (collection == null) {
			throw new IllegalArgumentException("collection must not be null");
		}
		if (provider == null) {
			throw new IllegalArgumentException("provider must not be null");
		}
		this.collection = collection;
		this.provider = provider;
	}

	/**
	 * Registers or replaces a Bindex entry in the repository.
	 *
	 * @param bindex the Bindex definition to persist
	 * @return the inserted MongoDB identifier as a string
	 * @throws JsonProcessingException if the Bindex or its classification cannot be
	 *         serialized
	 */
	public String create(Bindex bindex) throws JsonProcessingException {
		if (bindex == null) {
			throw new IllegalArgumentException("bindex must not be null");
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String bindexJson = objectMapper.writeValueAsString(bindex);
			String id = bindex.getId();
			collection.deleteOne(Filters.eq(ID_FIELD_NAME, id));

			Classification classification = bindex.getClassification();
			Set<String> languages = classification.getLanguages().stream().map(Picker::getNormalizedLanguageName).distinct()
					.collect(Collectors.toSet());
			Set<String> integrations = classification.getIntegrations().stream().map(String::toLowerCase).distinct()
					.collect(Collectors.toSet());

			if (languages.isEmpty()) {
				LOGGER.warn("No language defined for: {}.", id);
			}

			Document bindexDocument = new Document(BINDEX_PROPERTY_NAME, bindexJson).append(NAME_FIELD_NAME, bindex.getName())
					.append(VERSION_FIELD_NAME, bindex.getVersion()).append(DOMAINS_PROPERTY_NAME, classification.getDomains())
					.append(LAYERS_PROPERTY_NAME, classification.getLayers()).append(LANGUAGES_PROPERTY_NAME, languages)
					.append(INTEGRATIONS_PROPERTY_NAME, integrations)
					.append(CLASSIFICATION_EMBEDDING_PROPERTY_NAME,
							getEmbeddingBson(bindex.getClassification(), CLASSIFICATION_EMBEDDING_DIMENTIONS))
					.append(ID_FIELD_NAME, bindex.getId());

			InsertOneResult result = collection.insertOne(bindexDocument);
			return result.getInsertedId().toString();
		} catch (MongoCommandException e) {
			String bindexRegPassword = System.getenv(BindexRepository.BINDEX_REG_PASSWORD_PROP_NAME);
			if (bindexRegPassword == null || bindexRegPassword.isEmpty()) {
				LOGGER.error("To register a Bindex, the BINDEX_REG_PASSWORD env property is required.");
			}
			throw e;
		}
	}

	/**
	 * Generates the BSON array representation of the embedding for a classification.
	 *
	 * @param classification the classification to embed
	 * @param dimensions the embedding dimensions requested from the provider
	 * @return the embedding encoded as a BSON array
	 * @throws JsonProcessingException if the classification cannot be serialized
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
	 * Finds the MongoDB registration identifier for the supplied Bindex.
	 *
	 * @param bindex the Bindex to look up
	 * @return the MongoDB object identifier string, or {@code null} if not found
	 */
	public String getRegistredId(Bindex bindex) {
		if (bindex == null) {
			throw new IllegalArgumentException("bindex must not be null");
		}
		Document document = collection.find(new Document(ID_FIELD_NAME, bindex.getId())).first();
		if (document == null) {
			return null;
		}
		return ((ObjectId) document.get("_id")).toString();
	}

	/**
	 * Picks matching Bindex entries for a natural-language query.
	 *
	 * @param query the user request to classify and resolve
	 * @return the list of matching Bindex entries
	 * @throws IOException if classification generation or JSON parsing fails
	 */
	public List<Bindex> pick(String query) throws IOException {
		if (query == null) {
			throw new IllegalArgumentException("query must not be null");
		}
		String classificationStr = getClassification(query);
		if (Strings.CS.startsWith(classificationStr, "```json")) {
			classificationStr = StringUtils.substringBetween(classificationStr, "```json", "```");
		}
		Classification[] classifications = new ObjectMapper().readValue(classificationStr, Classification[].class);
		Collection<String> classificatioResults = new HashSet<>();
		for (Classification classification : classifications) {
			Set<String> languages = classification.getLanguages().stream().map(Picker::getNormalizedLanguageName).distinct()
					.collect(Collectors.toSet());
			List<Layer> layers = classification.getLayers();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Picking: {} ({})", StringUtils.join(layers, ", "), StringUtils.join(languages, ", "));
			}
			String classificationQuery = getClassificationText(classification);
			for (Layer layer : layers) {
				Collection<String> layerResults = getResults(INDEXNAME, CLASSIFICATION_EMBEDDING_PROPERTY_NAME,
						classificationQuery, CLASSIFICATION_EMBEDDING_DIMENTIONS,
						Aggregates.match(Filters.in(LANGUAGES_PROPERTY_NAME, languages)),
						Aggregates.match(Filters.in(LAYERS_PROPERTY_NAME, layer)));
				classificatioResults.addAll(layerResults);
			}
		}
		return classificatioResults.stream().map(this::getBindex).filter(b -> b != null).collect(Collectors.toList());
	}

	/**
	 * Serializes a classification into JSON text for prompt and embedding generation.
	 *
	 * @param classification the classification to serialize
	 * @return the serialized JSON representation
	 * @throws JsonProcessingException if serialization fails
	 */
	private String getClassificationText(Classification classification) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(classification);
	}

	/**
	 * Recursively adds the dependencies of the given Bindex to the provided set.
	 *
	 * @param dependencies the target set that accumulates dependency identifiers
	 * @param bindexId the root Bindex identifier whose dependency graph is traversed
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
	 * Builds a classification prompt from the query and executes it through the
	 * configured provider.
	 *
	 * @param query the natural-language request to classify
	 * @return the raw provider response containing classification JSON
	 * @throws IOException if the schema resource cannot be loaded or parsed
	 */
	private String getClassification(String query) throws IOException {
		URL systemResource = Bindex.class.getResource(BindexRepository.BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, StandardCharsets.UTF_8);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode schemaJson = objectMapper.readTree(schema);
		JsonNode jsonNode = schemaJson.get("properties").get("classification");
		String classificationSchema = objectMapper.writeValueAsString(jsonNode);

		String instructionTemplate = DEFAULT_CLASSIFICATION_INSTRUCTION;
		if (configurator != null) {
			String configuredInstruction = configurator.get(CLASSIFICATION_INSTRUCTION_PROP_NAME);
			if (configuredInstruction != null) {
				instructionTemplate = configuredInstruction;
			}
		}

		String classificationQuery = String.format(instructionTemplate, classificationSchema, query);
		provider.prompt(classificationQuery);
		return provider.perform();
	}

	/**
	 * Retrieves a Bindex by its logical identifier.
	 *
	 * @param id the Bindex identifier
	 * @return the deserialized Bindex, or {@code null} if no entry exists
	 */
	protected Bindex getBindex(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null");
		}
		Document doc = collection.find(Filters.eq(ID_FIELD_NAME, id)).first();
		if (doc == null) {
			return null;
		}
		try {
			return new ObjectMapper().readValue(doc.getString(BINDEX_PROPERTY_NAME), Bindex.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Executes a vector search and returns matching library coordinates in
	 * {@code name:version} form.
	 *
	 * @param indexName the MongoDB vector index name
	 * @param propertyPath the embedded property path used by the vector search
	 * @param query the text to embed and search for
	 * @param dimensions the embedding dimensions requested from the provider
	 * @param bsons optional aggregation stages appended after vector search
	 * @return a collection of unique library coordinates using the preferred version
	 */
	@SuppressWarnings({ "java:S3012" })
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
		pipeline.add(Aggregates.project(Projections.fields(Projections.exclude("_id"), Projections.include(ID_FIELD_NAME),
				Projections.include(NAME_FIELD_NAME), Projections.include(VERSION_FIELD_NAME),
				Projections.metaVectorSearchScore(SCORE_FIELD_NAME))));
		pipeline.add(Aggregates.match(Filters.gte(SCORE_FIELD_NAME, score)));

		List<Document> docs = collection.aggregate(pipeline).into(new ArrayList<>());
		Map<String, String> libraryVersionMap = new HashMap<>();
		for (Document doc : docs) {
			String id = doc.getString(ID_FIELD_NAME);
			String name = doc.getString(NAME_FIELD_NAME);
			String version = doc.getString(VERSION_FIELD_NAME);
			Double docScore = doc.getDouble(SCORE_FIELD_NAME);
			scoreMap.put(id, docScore);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("BindexId: {}: {}", name, docScore);
			}
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
	 * Normalizes a language name for repository matching.
	 *
	 * @param language the language to normalize
	 * @return the normalized lowercase language name without any parenthetical suffix
	 */
	static String getNormalizedLanguageName(Language language) {
		String lang = language.getName().toLowerCase().trim();
		return StringUtils.substringBefore(lang, "(").trim();
	}

	/**
	 * Sets the minimum vector-search score accepted by this picker.
	 *
	 * @param minScore the minimum score threshold
	 */
	public void setScore(Double minScore) {
		this.score = minScore;
	}

	/**
	 * Returns the last recorded score for a Bindex identifier.
	 *
	 * @param id the Bindex identifier
	 * @return the recorded score, or {@code null} if none is available
	 */
	public Double getScore(String id) {
		return scoreMap.get(id);
	}
}
