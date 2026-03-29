package org.machanism.machai.bindex;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class BindexRepositoryCloseTest {

	@Test
	void close_whenMongoClientIsNull_doesNothing() {
		// Arrange
		BindexRepository repo = new BindexRepository(mock(com.mongodb.client.MongoCollection.class));

		// Act
		repo.close();

		// Assert
		// no exception
	}
}
