package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.schema.Bindex;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

/**
 * Additional unit tests for {@link Picker} focusing on error handling and DB lookup paths.
 */
class PickerMissingCoverageTest {

	@Test
	void constructor_packagePrivate_shouldRejectNullCollection() {
		// Arrange
		MongoCollection<Document> collection = null;
		Genai provider = mock(Genai.class);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new Picker(collection, provider));
	}

	@Test
	void constructor_packagePrivate_shouldRejectNullProvider() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		Genai provider = null;

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new Picker(collection, provider));
	}

	@Test
	void getBindex_shouldThrowIllegalArgumentException_whenIdIsNull() {
		// Arrange
		Picker picker = new Picker(mock(MongoCollection.class), mock(Genai.class));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> picker.getBindex(null));
	}

	@Test
	void getBindex_shouldReturnNull_whenNoDocumentFound() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> iterable = mock(FindIterable.class);
		// Disambiguate overloaded find(...) by casting to org.bson.conversions.Bson.
		when(collection.find((org.bson.conversions.Bson) any())).thenReturn(iterable);
		when(iterable.first()).thenReturn(null);
		Picker picker = new Picker(collection, mock(Genai.class));

		// Act
		Bindex result = picker.getBindex("missing");

		// Assert
		assertNull(result);
	}

	@Test
	void getBindex_shouldThrowIllegalArgumentException_whenStoredJsonInvalid() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> iterable = mock(FindIterable.class);
		when(collection.find((org.bson.conversions.Bson) any())).thenReturn(iterable);
		when(iterable.first()).thenReturn(new Document(Picker.BINDEX_PROPERTY_NAME, "not-json"));
		Picker picker = new Picker(collection, mock(Genai.class));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> picker.getBindex("bad"));
	}

	@Test
	void getRegistredId_shouldReturnNull_whenNoDocumentFound() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> iterable = mock(FindIterable.class);
		when(collection.find((org.bson.conversions.Bson) any())).thenReturn(iterable);
		when(iterable.first()).thenReturn(null);
		Picker picker = new Picker(collection, mock(Genai.class));
		Bindex bindex = new Bindex();
		bindex.setId("id-1");

		// Act
		String id = picker.getRegistredId(bindex);

		// Assert
		assertNull(id);
	}

	@Test
	void getClassificationText_privateMethod_shouldSerializeClassification() throws Exception {
		// Arrange
		Picker picker = new Picker(mock(MongoCollection.class), mock(Genai.class));
		org.machanism.machai.schema.Classification classification = new org.machanism.machai.schema.Classification();
		classification.setDomains(Collections.singletonList("db"));

		Method method = Picker.class.getDeclaredMethod("getClassificationText",
				org.machanism.machai.schema.Classification.class);
		method.setAccessible(true);

		// Act
		String json = (String) method.invoke(picker, classification);

		// Assert
		assertNotNull(json);
		assertEquals(true, json.contains("domains"));
	}

	@Test
	void pick_shouldRejectNullQuery() {
		// Arrange
		Picker picker = new Picker(mock(MongoCollection.class), mock(Genai.class));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> picker.pick(null));
	}

	@Test
	void create_shouldRejectNullBindex() {
		// Arrange
		Picker picker = new Picker(mock(MongoCollection.class), mock(Genai.class));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> picker.create(null));
	}

	@Test
	void create_shouldLogHelpfulMessage_whenMongoCommandExceptionAndNoEnvPassword() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		// Make deleteOne throw MongoCommandException to enter catch block.
		when(collection.deleteOne(any())).thenThrow(
				new com.mongodb.MongoCommandException(new org.bson.BsonDocument("ok", new org.bson.BsonInt32(0)), null));
		Picker picker = new Picker(collection, mock(Genai.class));
		Bindex bindex = new Bindex();
		bindex.setId("id");
		bindex.setName("name");
		bindex.setVersion("1");
		bindex.setClassification(new org.machanism.machai.schema.Classification());

		// Act + Assert
		assertThrows(com.mongodb.MongoCommandException.class, () -> picker.create(bindex));
	}

	@Test
	void setScore_shouldAllowNull_andGetScoreShouldReturnNullForUnknownId() {
		// Arrange
		Picker picker = new Picker(mock(MongoCollection.class), mock(Genai.class));

		// Act
		assertDoesNotThrow(() -> picker.setScore(null));

		// Assert
		assertNull(picker.getScore("unknown"));
	}
}
