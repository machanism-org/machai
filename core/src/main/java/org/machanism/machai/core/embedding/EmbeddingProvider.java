package org.machanism.machai.core.embedding;

import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.search.FieldSearchPath;
import com.mongodb.client.result.InsertOneResult;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;

public class EmbeddingProvider implements Closeable {
	private static Logger logger = LoggerFactory.getLogger(EmbeddingProvider.class);

	private static final String BINDEX_PROPERTY_NAME = "bindex";

	private OpenAiEmbeddingModel embeddingModel;
	private MongoCollection<Document> collection;
	private MongoClient mongoClient;

	public EmbeddingProvider(String instanceName, String connection) {

		String uri = System.getenv("MONGODB_URI");
		if (uri == null || uri.isEmpty()) {
			throw new RuntimeException("MONGODB_URI env variable is not set or is empty.");
		}

		String apiKey = System.getenv("OPENAI_API_KEY");
		if (apiKey == null || apiKey.isEmpty()) {
			throw new IllegalStateException("OPEN_AI_API_KEY env variable is not set or is empty.");
		}

		embeddingModel = OpenAiEmbeddingModel.builder()
				.apiKey(apiKey)
				.modelName("text-embedding-3-small")
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

	public String create(BIndex bindex, List<Double> embedding) throws JsonProcessingException {

		Document query = new Document("id", bindex.getId());
		FindIterable<Document> find = collection.find(query);
		Document first = find.first();
		String _id;
		if (first == null) {
			BsonArray bsonArray = new BsonArray(
					embedding.stream()
							.map(BsonDouble::new)
							.collect(Collectors.toList()));

			String bindexStr = new ObjectMapper().writeValueAsString(bindex);
			Document doc = new Document(BINDEX_PROPERTY_NAME, bindexStr).append("id", bindex.getId()).append(
					"embedding",
					bsonArray);

			InsertOneResult result = collection.insertOne(doc);
			_id = result.getInsertedId().toString();
			logger.info("BIndex registered: " + bindex.getId() + ", _id: " + _id);
		} else {
			_id = ((ObjectId) first.get("_id")).toString();
			logger.info("BIndex already exists in the register: " + bindex.getId() + ", _id: " + _id);
		}

		return _id;
	}

	public List<Double> getEmbedding(String text) {
		Response<Embedding> response = embeddingModel.embed(text);
		return response.content().vectorAsList().stream()
				.map(Double::new)
				.collect(Collectors.toList());
	}

	public List<String> search(String query) {
		List<String> arrayList = new ArrayList<>();
		List<Double> embedding = getEmbedding(query);

		String indexName = "vector_index";
		FieldSearchPath fieldSearchPath = fieldPath("embedding");
		int limit = 5;
		List<Bson> pipeline = java.util.Arrays.asList(
				com.mongodb.client.model.Aggregates.vectorSearch(
						fieldSearchPath,
						embedding,
						indexName,
						limit,
						exactVectorSearchOptions()),
				com.mongodb.client.model.Aggregates.project(
						com.mongodb.client.model.Projections.fields(com.mongodb.client.model.Projections.exclude("_id"),
								com.mongodb.client.model.Projections.include(BINDEX_PROPERTY_NAME),
								com.mongodb.client.model.Projections.metaVectorSearchScore("score"))));

		List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());
		if (results.isEmpty()) {
			logger.info("No results found.");
		} else {
			results.forEach(doc -> {
				String bindex = doc.getString(BINDEX_PROPERTY_NAME);
				arrayList.add(bindex);
				logger.info("Bindex: " + bindex);
				logger.info("Score: " + doc.getDouble("score"));
			});
		}

		return arrayList;
	}
}