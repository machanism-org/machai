package org.machanism.machai.bindex.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.Document;
import org.bson.BsonObjectId;
import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;
import org.mockito.MockedStatic;
import org.mockito.ArgumentCaptor;

import com.mongodb.client.FindIterable;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.result.InsertOneResult;

/**
 * Unit tests for repository lifecycle, validation, and document lookup
 * behavior.
 */
class MongoBindexRepositoryTest {

	@Test
	void constructorUsesConfiguredClientAndExposesCollection() {
		// Arrange
		Configurator config = mock(Configurator.class);
		MongoClient client = mock(MongoClient.class);
		MongoDatabase database = mock(MongoDatabase.class);
		MongoCollection<Document> collection = mock(MongoCollection.class);
		when(client.getDatabase("machanism")).thenReturn(database);
		when(database.getCollection("bindex")).thenReturn(collection);
		when(config.get("BINDEX_REPO_URL", MongoBindexRepository.DB_URL)).thenReturn("mongodb://localhost");
		when(config.get("BINDEX_USER", null)).thenReturn(null);
		when(config.get("BINDEX_PASSWORD", null)).thenReturn(null);

		try (MockedStatic<MongoClients> clients = org.mockito.Mockito.mockStatic(MongoClients.class)) {
			clients.when(() -> MongoClients.create("mongodb://localhost")).thenReturn(client);

			// Act
			MongoBindexRepository repository = new MongoBindexRepository(config);

			// Assert
			assertSame(collection, repository.getCollection());
			repository.close();
			verify(client).close();
		}
	}

	@Test
	void findAppliesNormalizedLanguageFilter() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		AggregateIterable<Document> aggregate = mock(AggregateIterable.class);
		when(collection.aggregate(anyList())).thenReturn(aggregate);
		when(aggregate.into(any())).thenAnswer(invocation -> invocation.getArgument(0));
		MongoBindexRepository repository = repositoryWithCollection(collection);
		Classification classification = new Classification();
		Language language = new Language();
		language.setName("Java (21)");
		classification.setLanguages(List.of(language));
		classification.setLayers(null);

		// Act / Assert
		Executable executable = () -> repository.find(new Classification[] { classification }, List.of(0.1), 5, 0.5,
				mock(Configurator.class));
		assertThrows(IllegalArgumentException.class, executable);
		ArgumentCaptor<List<org.bson.conversions.Bson>> pipeline = ArgumentCaptor.forClass(List.class);
		verify(collection).aggregate(pipeline.capture());
		BsonDocument filter = pipeline.getValue().get(1).toBsonDocument(Document.class,
				MongoClientSettings.getDefaultCodecRegistry());
		assertTrue(filter.toJson().contains("languages"));
		assertTrue(filter.toJson().contains("java"));
	}

	@Test
	void findKeepsNewestVersionPerLibraryAndSortsLibrariesByScore() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		AggregateIterable<Document> aggregate = mock(AggregateIterable.class);
		List<Document> documents = List.of(
				searchResult("first", "Alpha", "1.0", "old", 0.70),
				searchResult("second", "Alpha", "2.0", "new", 0.60),
				searchResult("third", "Beta", "1.0", "best", 0.90));
		when(collection.aggregate(anyList())).thenReturn(aggregate);
		when(aggregate.into(any())).thenAnswer(invocation -> {
			List<Document> target = invocation.getArgument(0);
			target.addAll(documents);
			return target;
		});
		MongoBindexRepository repository = repositoryWithCollection(collection);
		Classification classification = new Classification();
		classification.setLanguages(List.of());
		classification.setLayers(null);

		// Act
		List<BindexInfo> found = List.copyOf(repository.find(new Classification[] { classification },
				List.of(0.1, 0.2), 10, 0.5, mock(Configurator.class)));

		// Assert
		assertEquals(2, found.size());
		assertEquals("third", found.get(0).getId());
		assertEquals("second", found.get(1).getId());
		assertEquals("2.0", found.get(1).getVersion());
		assertEquals("new", found.get(1).getDescription());
		assertEquals(0.70, found.get(1).getScore());
	}

	@Test
	void findThrowsHelpfulExceptionWhenVectorSearchHasNoMatches() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		AggregateIterable<Document> aggregate = mock(AggregateIterable.class);
		when(collection.aggregate(anyList())).thenReturn(aggregate);
		when(aggregate.into(any())).thenAnswer(invocation -> invocation.getArgument(0));
		MongoBindexRepository repository = repositoryWithCollection(collection);
		Classification classification = new Classification();
		classification.setLanguages(List.of());
		classification.setLayers(List.of());

		// Act / Assert
		Executable executable = () -> repository.find(new Classification[] { classification }, List.of(0.1), 5, 0.5,
				mock(Configurator.class));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
		assertTrue(exception.getMessage().startsWith("Libraries not found for classifications:"));
	}

	private static Document searchResult(String id, String name, String version, String description, double score) {
		return new Document("id", id).append("name", name).append("version", version)
				.append("description", description).append("score", score);
	}

	@Test
	void nullArgumentsAreRejectedBeforeDatabaseAccess() {
		// Arrange
		MongoBindexRepository repository = repositoryWithMocks();

		// Act / Assert
		assertThrows(IllegalArgumentException.class, () -> repository.getBindex(null));
		assertThrows(IllegalArgumentException.class, () -> repository.deleteBindex(null));
		assertThrows(IllegalArgumentException.class, () -> repository.save(null, null));
		assertThrows(IllegalArgumentException.class, () -> repository.getRegistredId(null));
	}

	@Test
	void getBindexReturnsNullWhenNoDocumentMatches() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> iterable = mock(FindIterable.class);
		when(collection.find(any(org.bson.conversions.Bson.class))).thenReturn(iterable);
		when(iterable.first()).thenReturn(null);
		MongoBindexRepository repository = repositoryWithCollection(collection);

		// Act
		Bindex result = repository.getBindex("not-found");

		// Assert
		assertNull(result);
	}

	@Test
	void findFirstOverloadsReturnTheFirstMatchingDocument() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> iterable = mock(FindIterable.class);
		Document expected = new Document("id", "one");
		when(collection.find(any(org.bson.conversions.Bson.class))).thenReturn(iterable);
		when(collection.find(any(Document.class))).thenReturn(iterable);
		when(iterable.first()).thenReturn(expected);
		MongoBindexRepository repository = repositoryWithCollection(collection);

		// Act
		Document fromBson = repository.findFirst(new Document("id", "one").toBsonDocument());
		Document fromDocument = repository.findFirst(new Document("id", "one"));

		// Assert
		assertEquals(expected, fromBson);
		assertEquals(expected, fromDocument);
	}

	@Test
	void saveReplacesExistingDocumentAndPersistsSearchFields() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		InsertOneResult insertResult = mock(InsertOneResult.class);
		ObjectId objectId = new ObjectId();
		when(collection.insertOne(any(Document.class))).thenReturn(insertResult);
		when(insertResult.getInsertedId()).thenReturn(new BsonObjectId(objectId));
		MongoBindexRepository repository = repositoryWithCollection(collection);
		Bindex bindex = bindex("library-id", "Library", "2.0", "useful library");

		// Act
		String result = repository.save(bindex, List.of(0.25, 0.75));

		// Assert
		ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
		verify(collection).insertOne(document.capture());
		assertEquals(objectId.toString(), result);
		assertEquals("library-id", document.getValue().getString("id"));
		assertEquals("Library", document.getValue().getString("name"));
		assertEquals("useful library", document.getValue().getString("description"));
		assertEquals(java.util.Set.of("mongo"), document.getValue().get("integrations"));
		assertEquals(2, document.getValue().getList("classification_embedding", org.bson.BsonValue.class).size());
		verify(collection).deleteOne(any(org.bson.conversions.Bson.class));
	}

	@Test
	void getBindexParsesStoredJsonAndDeleteAndRegisteredIdUseBindexId() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> iterable = mock(FindIterable.class);
		Bindex bindex = bindex("library-id", "Library", "1.0", "description");
		Document stored = new Document("bindex", "{\"id\":\"library-id\",\"name\":\"Library\"}");
		when(collection.find(any(org.bson.conversions.Bson.class))).thenReturn(iterable);
		when(collection.find(any(Document.class))).thenReturn(iterable);
		when(iterable.first()).thenReturn(stored, new Document("_id", new ObjectId()));
		MongoBindexRepository repository = repositoryWithCollection(collection);

		// Act / Assert
		assertEquals("library-id", repository.getBindex("library-id").getId());
		assertEquals("library-id", repository.deleteBindex(bindex));
		assertEquals(24, repository.getRegistredId(bindex).length());
		verify(collection).deleteOne(any(org.bson.conversions.Bson.class));
	}

	private static Bindex bindex(String id, String name, String version, String description) {
		Language language = new Language();
		language.setName("Java (21)");
		Classification classification = new Classification();
		classification.setLanguages(List.of(language));
		classification.setIntegrations(List.of("Mongo"));
		Bindex bindex = new Bindex();
		bindex.setId(id);
		bindex.setName(name);
		bindex.setVersion(version);
		bindex.setDescription(description);
		bindex.setClassification(classification);
		return bindex;
	}

	private static MongoBindexRepository repositoryWithMocks() {
		return repositoryWithCollection(mock(MongoCollection.class));
	}

	private static MongoBindexRepository repositoryWithCollection(MongoCollection<Document> collection) {
		Configurator config = mock(Configurator.class);
		MongoClient client = mock(MongoClient.class);
		MongoDatabase database = mock(MongoDatabase.class);
		when(client.getDatabase("machanism")).thenReturn(database);
		when(database.getCollection("bindex")).thenReturn(collection);
		when(config.get("BINDEX_REPO_URL", MongoBindexRepository.DB_URL)).thenReturn("mongodb://localhost");
		when(config.get("BINDEX_USER", null)).thenReturn(null);
		when(config.get("BINDEX_PASSWORD", null)).thenReturn(null);
		try (MockedStatic<MongoClients> clients = org.mockito.Mockito.mockStatic(MongoClients.class)) {
			clients.when(() -> MongoClients.create("mongodb://localhost")).thenReturn(client);
			return new MongoBindexRepository(config);
		}
	}
}
