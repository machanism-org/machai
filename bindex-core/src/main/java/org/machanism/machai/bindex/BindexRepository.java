package org.machanism.machai.bindex;

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
 * MongoDB-backed repository for persisting and retrieving {@link Bindex} documents.
 *
 * <p>The repository stores the serialized Bindex JSON in a dedicated field (see
 * {@link #BINDEX_PROPERTY_NAME}) and provides helper operations commonly needed by higher-level
 * components such as {@link Picker} and tool integrations.
 *
 * <p>Connection details are resolved from configuration/environment:
 * <ul>
 *   <li>When {@code BINDEX_REPO_URL} is configured, it is used as the MongoDB connection URI.</li>
 *   <li>Otherwise a default cluster URI is used, with credentials optionally sourced from
 *       {@code BINDEX_REG_PASSWORD}.</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class BindexRepository {
	/** MongoDB field name used to store the serialized Bindex JSON payload. */
	public static final String BINDEX_PROPERTY_NAME = "bindex";

	private static final String INSTANCE_NAME = "machanism";
	private static final String COLLECTION_NAME = "bindex";

	private static final String DEFAULT_CLUSTER_HOST = "cluster0.hivfnpr.mongodb.net/?appName=Cluster0";

	private final MongoCollection<Document> collection;
	protected final MongoClient mongoClient;

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

		String uri = config.get("BINDEX_REPO_URL", null);
		if (uri == null) {
			String bindexRegPassword = System.getenv("BINDEX_REG_PASSWORD");
			if (bindexRegPassword == null || bindexRegPassword.isEmpty()) {
				uri = "mongodb+srv://user:user@" + DEFAULT_CLUSTER_HOST;
			} else {
				uri = "mongodb+srv://machanismorg_db_user:" + bindexRegPassword + "@" + DEFAULT_CLUSTER_HOST;
			}
		}

		mongoClient = MongoClients.create(uri);
		MongoDatabase database = mongoClient.getDatabase(INSTANCE_NAME);
		collection = database.getCollection(COLLECTION_NAME);
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
	 * @throws IllegalArgumentException if {@code id} is {@code null} or the stored JSON cannot be parsed
	 */
	public Bindex getBindex(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null");
		}

		Document doc = collection.find(Filters.eq("id", id)).first();
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

}
