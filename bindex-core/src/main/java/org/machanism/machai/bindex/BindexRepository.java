package org.machanism.machai.bindex;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

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
 * @since 0.0.2
 */
public class BindexRepository {
	/** MongoDB field name used to store the serialized Bindex JSON payload. */
	public static final String BINDEX_PROPERTY_NAME = "bindex";

	public static final String DB_URL = "mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0";
	private static final String PUBLILC_USER_NAME = "user";
	private static final String REGISTER_USER_NAME = "machanismorg_db_user";
	public static final String BINDEX_REG_PASSWORD_PROP_NAME = "BINDEX_REG_PASSWORD";

	private static final String INSTANCENAME = "machanism";
	private static final String CONNECTION = "bindex";

	private final MongoCollection<Document> collection;

	private final MongoClient mongoClient;

	/**
	 * Creates a repository instance backed by a MongoDB collection.
	 *
	 * @param config configurator used to resolve {@code BINDEX_REPO_URL}
	 * @throws IllegalArgumentException if {@code config} is {@code null}
	 */
	public BindexRepository(Configurator config) {
		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}

		// Sonar java:S2095 - keep MongoClient as an instance field and close it when the repository is closed.
		this.mongoClient = createMongoClient(config);
		MongoDatabase database = mongoClient.getDatabase(INSTANCENAME);
		this.collection = database.getCollection(CONNECTION);
	}

	// Sonar java:S2095 - caller (repository) owns the created client and must close it.
	private static MongoClient createMongoClient(Configurator config) {
		String password = config.get(BINDEX_REG_PASSWORD_PROP_NAME, null);

		String username = password == null ? PUBLILC_USER_NAME : REGISTER_USER_NAME;
		password = password == null ? PUBLILC_USER_NAME : password;

		String url = config.get("BINDEX_REPO_URL", DB_URL);
		url = StringUtils.replace(url, "://", "://" + username + ":" + password + "@");
		return MongoClients.create(url);
	}

	/**
	 * Provides direct access to the underlying MongoDB collection.
	 *
	 * <p>
	 * Used by components such as {@link Picker} which operate on aggregation
	 * pipelines and need the raw {@link MongoCollection}.
	 *
	 * @param config configurator (kept for backward compatibility with callers)
	 * @return MongoDB collection handle
	 */
	public static MongoCollection<Document> getCollection(Configurator config) {
		// Sonar java:S1135 - required bridge method for legacy callers.
		return new BindexRepository(config).collection;
	}

	/**
	 * Creates a repository using an existing collection.
	 *
	 * <p>
	 * Package-private constructor used for tests to avoid MongoDB driver
	 * initialization.
	 */
	BindexRepository(MongoCollection<Document> collection) {
		if (collection == null) {
			throw new IllegalArgumentException("collection must not be null");
		}
		this.collection = collection;
		this.mongoClient = null;
	}

	/**
	 * Closes the underlying {@link MongoClient} if this repository created it.
	 *
	 * <p>
	 * This allows callers to use try-with-resources:
	 *
	 * <pre>
	 * try (BindexRepository repo = new BindexRepository(config)) { ... }
	 * </pre>
	 */
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
		}
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
		Document document = findFirst(query);
		if (document == null) {
			return null;
		}
		return ((ObjectId) document.get("_id")).toString();
	}

	/**
	 * Retrieves a {@link Bindex} instance from the database by its Bindex id.
	 *
	 * @param id Bindex id (the {@code id} field in the stored document)
	 * @return parsed {@link Bindex}, or {@code null} if not present
	 * @throws IllegalArgumentException if {@code id} is {@code null} or the stored
	 *                                  JSON cannot be parsed
	 */
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
	 * Single overridable seam for querying the first matching document.
	 */
	Document findFirst(Bson filter) {
		FindIterable<Document> find = collection.find(filter);
		return find.first();
	}

	Document findFirst(Document filter) {
		FindIterable<Document> find = collection.find(filter);
		return find.first();
	}

}
