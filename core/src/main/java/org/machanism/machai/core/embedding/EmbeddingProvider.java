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
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

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

	public BsonValue create(String text, List<Double> embedding) {
		BsonArray bsonArray = new BsonArray(
				embedding.stream()
						.map(BsonDouble::new)
						.collect(Collectors.toList()));

		Document doc = new Document(BINDEX_PROPERTY_NAME, text).append("embedding", bsonArray);
		InsertOneResult result = collection.insertOne(doc);
		BsonValue insertedId = result.getInsertedId();
		System.out.println("insertedId: " + insertedId);

		return insertedId;
	}

	public List<Double> getEmbedding(String text) {
		Response<Embedding> response = embeddingModel.embed(text);
		return response.content().vectorAsList().stream()
				.map(Double::new)
				.collect(Collectors.toList());
	}

	public void search(String query) {
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
			System.out.println("No results found.");
		} else {
			results.forEach(doc -> {
				System.out.println("Bindex: " + doc.getString(BINDEX_PROPERTY_NAME));
				System.out.println("Score: " + doc.getDouble("score"));
			});
		}
	}
}