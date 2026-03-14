package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.machanism.machai.schema.Bindex;

import com.mongodb.client.MongoCollection;

/**
 * Unit tests for {@link BindexRepository}.
 */
class BindexRepositoryTest {

	@Test
	void testOnlyConstructor_whenCollectionNull_throwsIllegalArgumentException() {
		// Arrange
		MongoCollection<Document> collection = null;

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new BindexRepository(collection));
	}

	@Test
	void getRegistredId_whenBindexNull_throwsIllegalArgumentException() {
		// Arrange
		BindexRepository repo = newRepoReturningFirst(null);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> repo.getRegistredId(null));
	}

	@Test
	void getRegistredId_whenNoDocumentFound_returnsNull() {
		// Arrange
		BindexRepository repo = newRepoReturningFirst(null);
		Bindex bindex = new Bindex();
		bindex.setId("abc");

		// Act
		String id = repo.getRegistredId(bindex);

		// Assert
		assertNull(id);
	}

	@Test
	void getRegistredId_whenDocumentFound_returnsObjectIdAsString() {
		// Arrange
		ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");
		Document doc = new Document("_id", objectId);
		BindexRepository repo = newRepoReturningFirst(doc);

		Bindex bindex = new Bindex();
		bindex.setId("abc");

		// Act
		String id = repo.getRegistredId(bindex);

		// Assert
		assertEquals(objectId.toString(), id);
	}

	@Test
	void getBindex_whenIdNull_throwsIllegalArgumentException() {
		// Arrange
		BindexRepository repo = newRepoReturningFirst(null);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> repo.getBindex(null));
	}

	@Test
	void getBindex_whenDocMissing_returnsNull() {
		// Arrange
		BindexRepository repo = newRepoReturningFirst(null);

		// Act
		Bindex bindex = repo.getBindex("missing");

		// Assert
		assertNull(bindex);
	}

	@Test
	void getBindex_whenJsonInvalid_throwsIllegalArgumentException() {
		// Arrange
		Document doc = new Document(BindexRepository.BINDEX_PROPERTY_NAME, "not-json");
		BindexRepository repo = newRepoReturningFirst(doc);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> repo.getBindex("abc"));
	}

	@Test
	void getBindex_whenJsonValid_parsesAndReturnsBindex() {
		// Arrange
		String json = "{\"id\":\"id-1\",\"name\":\"MyLib\",\"version\":\"1.0.0\"}";
		Document doc = new Document(BindexRepository.BINDEX_PROPERTY_NAME, json);
		BindexRepository repo = newRepoReturningFirst(doc);

		// Act
		Bindex bindex = repo.getBindex("id-1");

		// Assert
		assertNotNull(bindex);
		assertEquals("id-1", bindex.getId());
		assertEquals("MyLib", bindex.getName());
		assertEquals("1.0.0", bindex.getVersion());
	}

	@Test
	void deleteBindex_whenBindexNull_throwsIllegalArgumentException() {
		// Arrange
		BindexRepository repo = newRepoReturningFirst(null);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> repo.deleteBindex(null));
	}

	@Test
	void deleteBindex_deletesByIdAndReturnsId() {
		// Arrange
		@SuppressWarnings("unchecked")
		MongoCollection<Document> collection = mock(MongoCollection.class);
		BindexRepository repo = new BindexRepository(collection) {
			@Override
			Document findFirst(Bson filter) {
				return null;
			}

			@Override
			Document findFirst(Document filter) {
				return null;
			}
		};

		Bindex bindex = new Bindex();
		bindex.setId("abc");

		// Act
		String deleted = repo.deleteBindex(bindex);

		// Assert
		assertEquals("abc", deleted);
		verify(collection).deleteOne(any(Bson.class));
	}

	private static BindexRepository newRepoReturningFirst(Document first) {
		@SuppressWarnings("unchecked")
		MongoCollection<Document> collection = mock(MongoCollection.class);
		return new BindexRepository(collection) {
			@Override
			Document findFirst(Bson filter) {
				return first;
			}

			@Override
			Document findFirst(Document filter) {
				return first;
			}
		};
	}
}
