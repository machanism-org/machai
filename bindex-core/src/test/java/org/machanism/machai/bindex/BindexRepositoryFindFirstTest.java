package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

/**
 * Unit tests for {@link BindexRepository#findFirst(org.bson.conversions.Bson)} and
 * {@link BindexRepository#findFirst(org.bson.Document)}.
 *
 * <p>Mockito inline mocking is enabled in this project, but the MongoDB driver interfaces involved
 * are not compatible with inline mocking on this runtime. Therefore, we use small hand-written fakes
 * based on {@link java.lang.reflect.Proxy}.
 */
class BindexRepositoryFindFirstTest {

	@Test
	void findFirst_withBsonFilter_shouldReturnFirstDocument() {
		// Arrange
		Document expected = new Document("_id", "123");
		MongoCollection<Document> collection = collectionReturningFirst(expected);
		BindexRepository repo = new BindexRepository(collection);

		// Act
		Document actual = repo.findFirst(new F());

		// Assert
		assertSame(expected, actual);
	}

	@Test
	void findFirst_withDocumentFilter_shouldReturnNullWhenEmpty() {
		// Arrange
		MongoCollection<Document> collection = collectionReturningFirst(null);
		BindexRepository repo = new BindexRepository(collection);

		// Act
		Document actual = repo.findFirst(new Document("id", "missing"));

		// Assert
		assertNull(actual);
	}

	private static MongoCollection<Document> collectionReturningFirst(Document first) {
		FindIterable<Document> iterable = findIterableReturningFirst(first);

		@SuppressWarnings("unchecked")
		MongoCollection<Document> collection = (MongoCollection<Document>) java.lang.reflect.Proxy.newProxyInstance(
				MongoCollection.class.getClassLoader(),
				new Class<?>[] { MongoCollection.class },
				(proxy, method, args) -> {
					if (method.getName().equals("find") && method.getReturnType().isAssignableFrom(FindIterable.class)) {
						return iterable;
					}
					throw new UnsupportedOperationException("Unsupported: " + method);
				});
		return collection;
	}

	private static FindIterable<Document> findIterableReturningFirst(Document first) {
		@SuppressWarnings("unchecked")
		FindIterable<Document> iterable = (FindIterable<Document>) java.lang.reflect.Proxy.newProxyInstance(
				FindIterable.class.getClassLoader(),
				new Class<?>[] { FindIterable.class },
				(proxy, method, args) -> {
					if (method.getName().equals("first")) {
						return first;
					}
					// allow fluent calls in case implementation changes later
					if (method.getReturnType().isAssignableFrom(FindIterable.class)) {
						return proxy;
					}
					throw new UnsupportedOperationException("Unsupported: " + method);
				});
		return iterable;
	}

	private static final class F implements Bson {
		@Override
		public <TDocument> org.bson.BsonDocument toBsonDocument(Class<TDocument> documentClass,
				org.bson.codecs.configuration.CodecRegistry codecRegistry) {
			return new org.bson.BsonDocument();
		}
	}
}
