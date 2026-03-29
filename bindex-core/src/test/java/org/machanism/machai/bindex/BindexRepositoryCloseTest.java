package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class BindexRepositoryCloseTest {

	@Test
	void close_whenMongoClientIsNull_doesNothing() {
		// Arrange
		BindexRepository repo = new BindexRepository(mock(com.mongodb.client.MongoCollection.class));

		// Act + Assert
		// Sonar java:S2699 - add assertion to ensure close() is safe when mongoClient is null.
		assertDoesNotThrow(repo::close);
	}
}
