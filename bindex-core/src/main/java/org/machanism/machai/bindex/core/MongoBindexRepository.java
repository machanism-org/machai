package org.machanism.machai.bindex.core;

import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
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
 * MongoDB-backed repository for persisting and retrieving {@link Bindex}
 * documents.
 *
 * <p>
 * The repository stores the serialized Bindex JSON in a dedicated field (see
 * {@link #BINDEX_PROPERTY_NAME}) and provides helper operations commonly needed
 * by higher-level components such as {@link Picker} and tool integrations.
 *
 * <p>
 * Connection details are resolved from configuration/environment:
 * <ul>
 * <li>When {@code BINDEX_REPO_URL} is configured, it is used as the MongoDB
 * connection URI.</li>
 * <li>Otherwise a default cluster URI is used, with credentials optionally
 * sourced from {@code BINDEX_REG_PASSWORD}.</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public class MongoBindexRepository implements BindexRepository {

	private final Logger logger = LoggerFactory.getLogger(MongoBindexRepository.class);

	private MongoClient mongoClient;

	/** MongoDB field name used to store the serialized Bindex JSON payload. */
	public static final String BINDEX_PROPERTY_NAME = "bindex";

	private static final String CLASSIFICATION_EMBEDDING_PROP_NAME = "classification_embedding";

	private static final String LANGUAGES_PROP_NAME = "languages";
	private static final String DOMAINS_PROP_NAME = "domains";
	private static final String LAYERS_PROP_NAME = "layers";
	private static final String INTEGRATIONS_PROP_NAME = "integrations";
	private static final String BINDEX_PROP_NAME = "bindex";

	private static final String ID_FIELD_NAME = "id";
	private static final String NAME_FIELD_NAME = "name";
	private static final String VERSION_FIELD_NAME = "version";
	private static final String DESCRIPTION_FIELD_NAME = "description";
	private static final String SCORE_FIELD_NAME = "score";
	private static final String INDEXNAME = "vector_index";

	/** Default MongoDB connection URL used when no repository URL is configured. */
	public static final String DB_URL = "mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0";
	private static final String PUBLILC_USER_NAME = "user";
	private static final String REGISTER_USER_NAME = "machanismorg_db_user";

	private static final String INSTANCENAME = "machanism";
	private static final String CONNECTION = "bindex";

	private static final String BINDEX_USER_PROP_NAME = "BINDEX_USER";
	/** Configuration property name for the MongoDB password. */
	public static final String BINDEX_PASSWORD_PROP_NAME = "BINDEX_PASSWORD";
	private static final String BINDEX_REPO_URL_PROP_NAME = "BINDEX_REPO_URL";

	private static final double DEFAULT_SCORE_VALUE = 0.85;

	private Configurator config;
	private final MongoCollection<Document> collection;

	/**
	 * Creates a repository instance backed by a MongoDB collection.
	 *
	 * @param config configurator used to resolve {@code BINDEX_REPO_URL}
	 * @throws IllegalArgumentException if {@code config} is {@code null}
	 */
	public MongoBindexRepository(Configurator config) {
		this.config = config;
		createMongoClient();
		MongoDatabase database = mongoClient.getDatabase(INSTANCENAME);
		this.collection = database.getCollection(CONNECTION);
	}

	/**
	 * Initializes the internal {@link MongoClient} instance.
	 * <p>
	 * Resolves connection details from the provided {@link Configurator}, building
	 * a authenticated or public URL to connect to the MongoDB instance.
	 */
	private void createMongoClient() {
		if (mongoClient == null) {
			String url = config.get(BINDEX_REPO_URL_PROP_NAME, DB_URL);

			String username = config.get(BINDEX_USER_PROP_NAME, null);
			String password = config.get(BINDEX_PASSWORD_PROP_NAME, null);

			if (DB_URL.equals(url) && username == null) {
				if (password == null) {
					username = PUBLILC_USER_NAME;
					password = PUBLILC_USER_NAME;
				} else {
					username = REGISTER_USER_NAME;
					password = config.get(BINDEX_PASSWORD_PROP_NAME, null);
				}
			}

			if (username != null) {
				url = Strings.CS.replace(url, "://", "://" + username + ":" + password + "@");
			}

			mongoClient = MongoClients.create(url);
		}
	}

	/**
	 * Provides direct access to the underlying MongoDB collection.
	 * <p>
	 * Used by components such as {@link Picker} which operate on aggregation
	 * pipelines and need the raw {@link MongoCollection}.
	 *
	 * @return the MongoDB collection handle
	 */
	public MongoCollection<Document> getCollection() {
		return collection;
	}

	/**
	 * Closes the underlying {@link MongoClient} if this repository created it.
	 * <p>
	 * This allows callers to use try-with-resources:
	 * <pre>
	 * try (MongoBindexRepository repo = new MongoBindexRepository(config)) { ... }
	 * </pre>
	 */
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	/**
	 * Retrieves a {@link Bindex} instance from the database by its Bindex id.
	 *
	 * @param id Bindex id (the {@code id} field in the stored document)
	 * @return parsed {@link Bindex}, or {@code null} if not present
	 * @throws IllegalArgumentException if {@code id} is {@code null} or the stored
	 *                                  JSON cannot be parsed
	 */
	@Override
	public Bindex getBindex(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null");
		}

		Document doc = findFirst(Filters.eq("id", id));
		if (doc == null) {
			return null;
		}

		String bindexStr = doc.getString(BINDEX_PROPERTY_NAME);
		try {
			return new ObjectMapper().readValue(bindexStr, Bindex.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Deletes a Bindex document from the database.
	 *
	 * @param bindex Bindex to delete (by {@link Bindex#getId()})
	 * @return the deleted Bindex id
	 * @throws IllegalArgumentException if {@code bindex} is {@code null}
	 */
	public String deleteBindex(Bindex bindex) {
		if (bindex == null) {
			throw new IllegalArgumentException("bindex must not be null");
		}
		String id = bindex.getId();
		Bson filter = Filters.eq("id", id);
		collection.deleteOne(filter);
		return id;
	}

	/**
	 * Querying the first matching document based on a Bson filter.
	 * 
	 * @param filter the Bson filter to match
	 * @return the first matching {@link Document}, or {@code null} if none match
	 */
	Document findFirst(Bson filter) {
		FindIterable<Document> find = collection.find(filter);
		return find.first();
	}

	/**
	 * Querying the first matching document based on a raw Document filter.
	 * 
	 * @param filter the Document filter to match
	 * @return the first matching {@link Document}, or {@code null} if none match
	 */
	Document findFirst(Document filter) {
		FindIterable<Document> find = collection.find(filter);
		return find.first();
	}

	/**
	 * Registers or replaces a Bindex entry in the repository.
	 *
	 * @param bindex    the Bindex definition to persist
	 * @param embedding the embedding vector associated with the Bindex entry
	 * @return the inserted MongoDB identifier as a string
	 * @throws IllegalArgumentException if the Bindex or its classification cannot
	 *                                  be serialized
	 */
	@Override
	public String save(Bindex bindex, List<Double> embedding) {
		if (bindex == null) {
			throw new IllegalArgumentException("bindex must not be null");
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String bindexJson = objectMapper.writeValueAsString(bindex);
			String id = bindex.getId();
			collection.deleteOne(Filters.eq(ID_FIELD_NAME, id));

			Classification classification = bindex.getClassification();
			Set<String> languages = classification.getLanguages().stream().map(Picker::getNormalizedLanguageName)
					.distinct()
					.collect(Collectors.toSet());
			Set<String> integrations = classification.getIntegrations().stream().map(String::toLowerCase).distinct()
					.collect(Collectors.toSet());

			if (languages.isEmpty()) {
				logger.warn("No language defined for: {}.", id);
			}

			Document bindexDocument = new Document(BINDEX_PROP_NAME, bindexJson)
					.append(NAME_FIELD_NAME, bindex.getName())
					.append(VERSION_FIELD_NAME, bindex.getVersion())
					.append(DESCRIPTION_FIELD_NAME, bindex.getDescription())
					.append(DOMAINS_PROP_NAME, classification.getDomains())
					.append(LAYERS_PROP_NAME, classification.getLayers()).append(LANGUAGES_PROP_NAME, languages)
					.append(INTEGRATIONS_PROP_NAME, integrations)
					.append(CLASSIFICATION_EMBEDDING_PROP_NAME,
							new BsonArray(embedding.stream().map(BsonDouble::new).collect(Collectors.toList())))
					.append(ID_FIELD_NAME, bindex.getId());

			InsertOneResult result = collection.insertOne(bindexDocument);
			return result.getInsertedId().asObjectId().getValue().toString();

		} catch (MongoCommandException e) {
			String bindexRegPassword = System.getenv(MongoBindexRepository.BINDEX_PASSWORD_PROP_NAME);
			if (bindexRegPassword == null || bindexRegPassword.isEmpty()) {
				logger.error("To register a Bindex, the BINDEX_REG_PASSWORD env property is required.");
			}
			throw e;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Finds the MongoDB registration identifier for the supplied Bindex.
	 *
	 * @param bindex the Bindex to look up
	 * @return the MongoDB object identifier string, or {@code null} if not found
	 * @throws IllegalArgumentException if {@code bindex} is {@code null}
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
	 * @param classifications    an array of {@link Classification} filters to restrict search scope
	 * @param embedding          the embedding vector for semantic search
	 * @param vectorSearchLimits the maximum number of results to return from vector search
	 * @param score              the minimum relevance score threshold for recommended entries
	 * @param config             the configuration object
	 * @return the list of matching Bindex entries
	 */
	@Override
	public Collection<BindexInfo> find(Classification[] classifications, List<Double> embedding, long vectorSearchLimits,
			Double score, Configurator config) {

		Map<String, BindexInfo> results = new LinkedHashMap<>();

		for (Classification classification : classifications) {
			Set<String> languages = classification.getLanguages().stream().map(Picker::getNormalizedLanguageName)
					.distinct()
					.collect(Collectors.toSet());
			List<Layer> layers = classification.getLayers();
			if (logger.isInfoEnabled()) {
				logger.info("Picking: {} ({})", StringUtils.join(layers, ", "), StringUtils.join(languages, ", "));
			}

			for (Layer layer : layers) {
				score = score == null ? DEFAULT_SCORE_VALUE : score;
				Map<String, BindexInfo> layerResults = getResults(embedding, score, vectorSearchLimits,
						Aggregates.match(Filters.in(LANGUAGES_PROP_NAME, languages)),
						Aggregates.match(Filters.in(LAYERS_PROP_NAME, layer)));

				results.putAll(layerResults);
			}
		}

		return results.values();
	}

	/**
	 * Executes a vector search and returns matching library coordinates in
	 * mapped structure.
	 * 
	 * @param embedding          the query vector embedding to match against
	 * @param score              the minimum vector search score to filter results
	 * @param vectorSearchLimits the maximum size limit of matching elements
	 * @param bsons              optional aggregation stages appended after vector search
	 * @return a map of unique library coordinates using the preferred version
	 */
	private Map<String, BindexInfo> getResults(List<Double> embedding, Double score, long vectorSearchLimits,
			Bson... bsons) {
		List<Bson> pipeline = new ArrayList<>();
		pipeline.add(Aggregates.vectorSearch(fieldPath(CLASSIFICATION_EMBEDDING_PROP_NAME), embedding, INDEXNAME,
				vectorSearchLimits,
				exactVectorSearchOptions()));
		if (bsons != null) {
			for (Bson bson : bsons) {
				pipeline.add(bson);
			}
		}
		pipeline.add(
				Aggregates.project(Projections.fields(Projections.exclude("_id"), Projections.include(ID_FIELD_NAME),
						Projections.include(NAME_FIELD_NAME), Projections.include(VERSION_FIELD_NAME),
						Projections.metaVectorSearchScore(SCORE_FIELD_NAME))));
		pipeline.add(Aggregates.match(Filters.gte(SCORE_FIELD_NAME, score)));

		List<Document> docs = collection.aggregate(pipeline).into(new ArrayList<>());

		Map<String, BindexInfo> libraryVersionMap = new HashMap<>();

		for (Document doc : docs) {
			String id = doc.getString(ID_FIELD_NAME);
			String name = doc.getString(NAME_FIELD_NAME);
			String version = doc.getString(VERSION_FIELD_NAME);
			String description = doc.getString(DESCRIPTION_FIELD_NAME);
			Double docScore = doc.getDouble(SCORE_FIELD_NAME);

			BindexInfo record;

			if (logger.isDebugEnabled()) {
				logger.debug("BindexId: {}: {}", id, docScore);
			}

			if (libraryVersionMap.containsKey(name)) {
				record = libraryVersionMap.get(name);
				String existsVersion = (String) record.getVersion();

				ComparableVersion v1 = new ComparableVersion(existsVersion);
				ComparableVersion v2 = new ComparableVersion(version);
				if (v1.compareTo(v2) < 0) {
					record.setId(id);
					record.setVersion(version);
					record.setDescription(description);
				}
			} else {
				record = new BindexInfo();
				record.setScore(docScore);
				record.setVersion(version);
				record.setDescription(description);
				record.setId(id);
			}

			libraryVersionMap.put(name, record);
		}

		List<Map.Entry<String, BindexInfo>> sortedEntries = new ArrayList<>(libraryVersionMap.entrySet());
		sortedEntries.sort((e1, e2) -> {
			Double score1 = (Double) e1.getValue().getScore();
			Double score2 = (Double) e2.getValue().getScore();
			return score2.compareTo(score1);
		});

		Map<String, BindexInfo> sortedMap = new LinkedHashMap<>();
		for (Map.Entry<String, BindexInfo> entry : sortedEntries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

}