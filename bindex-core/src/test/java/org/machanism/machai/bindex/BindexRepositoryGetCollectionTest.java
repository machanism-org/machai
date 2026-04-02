package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Tests for {@link BindexRepository#getCollection(Configurator)}.
 */
class BindexRepositoryGetCollectionTest {

	@Test
	void getCollection_shouldThrowIllegalArgumentException_whenConfigIsNull() {
		// Arrange
		Configurator config = null;

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> BindexRepository.getCollection(config));
	}

	@Test
	void getCollection_shouldReturnCollection_whenConfigProvided() {
		// Arrange
		Configurator config = mock(Configurator.class);
		org.mockito.Mockito.when(config.get(org.mockito.Mockito.eq("BINDEX_REPO_URL"), anyString()))
				.thenReturn("mongodb://localhost");
		org.mockito.Mockito.when(config.get(org.mockito.Mockito.eq(BindexRepository.BINDEX_REG_PASSWORD_PROP_NAME),
				org.mockito.Mockito.isNull()))
				.thenReturn(null);

		@SuppressWarnings("unchecked")
		MongoCollection<Document> expectedCollection = mock(MongoCollection.class);
		MongoDatabase db = mock(MongoDatabase.class);
		MongoClient client = mock(MongoClient.class);
		org.mockito.Mockito.when(client.getDatabase(anyString())).thenReturn(db);
		org.mockito.Mockito.when(db.getCollection(anyString())).thenReturn(expectedCollection);

		try (org.mockito.MockedStatic<com.mongodb.client.MongoClients> mongoClients = org.mockito.Mockito
				.mockStatic(com.mongodb.client.MongoClients.class)) {
			mongoClients.when(() -> com.mongodb.client.MongoClients.create(anyString())).thenReturn(client);

			// Act
			MongoCollection<Document> collection = BindexRepository.getCollection(config);

			// Assert
			assertNotNull(collection);
		}
	}
}
