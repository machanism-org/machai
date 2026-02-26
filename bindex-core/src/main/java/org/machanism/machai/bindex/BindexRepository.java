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

public class BindexRepository {
	public static final String BINDEX_PROPERTY_NAME = "bindex";

	private static final String INSTANCENAME = "machanism";
	protected static final String CONNECTION = "bindex";
	protected static final String INDEXNAME = "vector_index";

	/** MongoDB field name for classification embeddings. */
	protected static final String CLASSIFICATION_EMBEDDING_PROPERTY_NAME = "classification_embedding";

	/** Result limit for vector search operations. */
	protected static final int VECTOR_SEARCH_LIMITS = 50;

	protected String dbUrl = "cluster0.hivfnpr.mongodb.net/?appName=Cluster0";
	protected String embeddingModelName = "text-embedding-3-small";
	private MongoCollection<Document> collection;
	protected MongoClient mongoClient;

	public BindexRepository(Configurator config) {
		super();
		String uri = config.get("BINDEX_REPO_URL", null);
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
	 * Retrieves a Bindex instance from the database by its ID.
	 *
	 * @param id String identifier
	 * @return Bindex object, or null if not present in the DB
	 */
	public Bindex getBindex(String id) {
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

	public String deleteBindex(Bindex bindex) {
		String id = bindex.getId();
		Bson filter = Filters.eq("id", id);
		collection.deleteOne(filter);
		return id;
	}

}