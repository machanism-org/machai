package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;

class BindexRepositoryCloseTest {

	@Test
	void close_whenMongoClientIsNull_doesNothing() {
		BindexRepository repo = new BindexRepository(mock(com.mongodb.client.MongoCollection.class));

		assertDoesNotThrow(repo::close);
	}

	@Test
	void close_whenMongoClientExists_closesClient() throws Exception {
		BindexRepository repo = new BindexRepository(mock(com.mongodb.client.MongoCollection.class));
		MongoClient mongoClient = mock(MongoClient.class);
		Field field = BindexRepository.class.getDeclaredField("mongoClient");
		field.setAccessible(true);
		field.set(repo, mongoClient);

		repo.close();

		verify(mongoClient, times(1)).close();
	}
}
