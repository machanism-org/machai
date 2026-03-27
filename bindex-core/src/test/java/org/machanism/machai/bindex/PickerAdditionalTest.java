package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

/**
 * Additional tests to improve coverage for {@link Picker} without requiring a
 * real MongoDB or GenAI backend.
 */
class PickerAdditionalTest {

	@Test
	void getEmbeddingBson_whenClassificationNull_throws() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		Genai provider = mock(Genai.class);
		Picker picker = new Picker(collection, provider);

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> picker.getEmbeddingBson(null, 1));
		assertEquals("classification must not be null", ex.getMessage());
	}

	@Test
	void getEmbeddingBson_whenDimensionsNonPositive_throws() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		Genai provider = mock(Genai.class);
		Picker picker = new Picker(collection, provider);
		Classification classification = new Classification();

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> picker.getEmbeddingBson(classification, 0));
		assertEquals("dimensions must be > 0", ex.getMessage());
	}

	@Test
	void getRegistredId_whenBindexNull_throws() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		Genai provider = mock(Genai.class);
		Picker picker = new Picker(collection, provider);

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> picker.getRegistredId(null));
		assertEquals("bindex must not be null", ex.getMessage());
	}

	@Test
	void getRegistredId_whenDocumentNotFound_returnsNull() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		Genai provider = mock(Genai.class);
		Picker picker = new Picker(collection, provider);

		Bindex bindex = new Bindex();
		bindex.setId("lib:1.0");

		FindIterable<Document> iterable = proxyOf(FindIterable.class, (p, method, args) -> {
			if ("first".equals(method.getName())) {
				return null;
			}
			throw new UnsupportedOperationException("Unexpected method: " + method);
		});

		when(collection.find(any(Document.class))).thenReturn(iterable);

		// Act
		String id = picker.getRegistredId(bindex);

		// Assert
		assertNull(id);
		verify(collection).find(any(Document.class));
	}

	@Test
	void addDependencies_whenBindexNotFound_doesNotAddAnything() {
		// Arrange
		MongoCollection<Document> collection = proxyOf(MongoCollection.class, (p, method, args) -> {
			if ("find".equals(method.getName())) {
				return proxyOf(FindIterable.class, (p2, m2, a2) -> {
					if ("first".equals(m2.getName())) {
						return null;
					}
					throw new UnsupportedOperationException("Unexpected method: " + m2);
				});
			}
			throw new UnsupportedOperationException("Unexpected method: " + method);
		});
		Genai provider = mock(Genai.class);
		Picker picker = new Picker(collection, provider);

		Set<String> deps = new HashSet<>();

		// Act
		picker.addDependencies(deps, "missing");

		// Assert
		assertTrue(deps.isEmpty());
	}

	@Test
	void addDependencies_whenGraphHasCycle_doesNotRecurseInfinitely() {
		// Arrange
		Document docA = new Document(Picker.BINDEX_PROPERTY_NAME, "{\"id\":\"A\",\"dependencies\":[\"B\"]}");
		Document docB = new Document(Picker.BINDEX_PROPERTY_NAME, "{\"id\":\"B\",\"dependencies\":[\"A\"]}");

		MongoCollection<Document> collection = proxyOf(MongoCollection.class, (p, method, args) -> {
			if ("find".equals(method.getName())) {
				Bson filter = (Bson) args[0];
				String asString = String.valueOf(filter);
				Document toReturn = asString.contains("A") ? docA : asString.contains("B") ? docB : null;
				return proxyOf(FindIterable.class, (p2, m2, a2) -> {
					if ("first".equals(m2.getName())) {
						return toReturn;
					}
					throw new UnsupportedOperationException("Unexpected method: " + m2);
				});
			}
			throw new UnsupportedOperationException("Unexpected method: " + method);
		});

		Genai provider = mock(Genai.class);
		Picker picker = new Picker(collection, provider);

		Set<String> deps = new HashSet<>();

		// Act
		picker.addDependencies(deps, "A");

		// Assert
		assertEquals(new HashSet<>(Arrays.asList("A", "B")), deps);
	}

	@Test
	void getNormalizedLanguageName_trimsLowercasesAndStripsParentheses() {
		// Arrange
		Language lang = new Language();
		lang.setName("  Java (JVM) ");

		// Act
		String normalized = Picker.getNormalizedLanguageName(lang);

		// Assert
		assertEquals("java", normalized);
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxyOf(Class<T> type, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
	}
}
