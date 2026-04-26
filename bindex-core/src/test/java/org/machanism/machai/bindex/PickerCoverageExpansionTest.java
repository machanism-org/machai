package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;
import org.machanism.machai.schema.Layer;

import com.mongodb.MongoCommandException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;

class PickerCoverageExpansionTest {

	@Test
	void create_shouldInsertDocumentWithNormalizedFields() throws Exception {
		MongoCollection<Document> collection = mock(MongoCollection.class);
		Genai provider = mock(Genai.class);
		when(provider.embedding(anyString(), anyLong())).thenReturn(Arrays.asList(0.11, 0.22));
		when(collection.insertOne(any(Document.class))).thenReturn(InsertOneResult.acknowledged(new BsonObjectId(new ObjectId())));
		Picker picker = new Picker(collection, provider);

		Bindex bindex = new Bindex();
		bindex.setId("artifact:demo");
		bindex.setName("demo");
		bindex.setVersion("1.0.0");
		bindex.setDescription("demo description");
		bindex.setDependencies(Collections.emptyList());
		Classification classification = new Classification();
		classification.setDomains(Collections.singletonList("framework"));
		classification.setLayers(Collections.singletonList(Layer.ADAPTERS));
		Language language = new Language();
		language.setName(" Java (JVM) ");
		classification.setLanguages(Collections.singletonList(language));
		classification.setIntegrations(Collections.singletonList("REST"));
		bindex.setClassification(classification);

		String id = picker.create(bindex);

		assertNotNull(id);
		org.mockito.ArgumentCaptor<Document> captor = org.mockito.ArgumentCaptor.forClass(Document.class);
		verify(collection).insertOne(captor.capture());
		Document stored = captor.getValue();
		assertEquals("artifact:demo", stored.getString("id"));
		assertEquals("demo", stored.getString("name"));
		assertEquals("1.0.0", stored.getString("version"));
		assertEquals(Collections.singleton("java"), new HashSet<>((Collection<String>) stored.get("languages")));
		assertEquals(Collections.singleton("rest"), new HashSet<>((Collection<String>) stored.get("integrations")));
		assertNotNull(stored.get("classification_embedding"));
	}

	@Test
	void create_shouldRethrowMongoCommandExceptionFromInsert() {
		MongoCollection<Document> collection = mock(MongoCollection.class);
		Genai provider = mock(Genai.class);
		when(provider.embedding(anyString(), anyLong())).thenReturn(Collections.singletonList(0.5));
		when(collection.insertOne(any(Document.class))).thenThrow(new MongoCommandException(new BsonDocument("ok", new BsonInt32(0)), null));
		Picker picker = new Picker(collection, provider);

		Bindex bindex = new Bindex();
		bindex.setId("id");
		bindex.setName("name");
		bindex.setVersion("1");
		Classification classification = new Classification();
		classification.setDomains(Collections.emptyList());
		classification.setLayers(Collections.emptyList());
		classification.setLanguages(Collections.emptyList());
		classification.setIntegrations(Collections.emptyList());
		bindex.setClassification(classification);

		assertThrows(MongoCommandException.class, () -> picker.create(bindex));
	}

	@Test
	void getEmbeddingBson_shouldRequestEmbeddingFromProvider() throws Exception {
		Genai provider = mock(Genai.class);
		when(provider.embedding(anyString(), anyLong())).thenReturn(Arrays.asList(1.5, 2.5));
		Picker picker = new Picker(mock(MongoCollection.class), provider);
		Classification classification = new Classification();
		classification.setDomains(Collections.singletonList("domain"));
		classification.setLayers(Collections.singletonList(Layer.ADAPTERS));
		Language language = new Language();
		language.setName("Java");
		classification.setLanguages(Collections.singletonList(language));
		classification.setIntegrations(Collections.emptyList());

		picker.getEmbeddingBson(classification, 2);

		verify(provider).embedding(anyString(), anyLong());
	}

	@Test
	void pick_shouldParseMarkdownJsonAndReturnOnlyExistingBindexes() throws Exception {
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> findIterable = mock(FindIterable.class);
		when(collection.find(any(Bson.class))).thenReturn(findIterable);
		when(findIterable.first())
				.thenReturn(new Document(Picker.BINDEX_PROPERTY_NAME, "{\"id\":\"lib-a:2.0\",\"name\":\"lib-a\",\"version\":\"2.0\"}"))
				.thenReturn(null);

		AggregateIterable<Document> aggregateIterable = mock(AggregateIterable.class);
		when(collection.aggregate(any(List.class))).thenReturn(aggregateIterable);
		when(aggregateIterable.into(any())).thenAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			Collection<Document> out = invocation.getArgument(0);
			out.add(new Document("id", "lib-a:2.0").append("name", "lib-a").append("version", "2.0").append("score", 0.93));
			out.add(new Document("id", "missing:1.0").append("name", "missing").append("version", "1.0").append("score", 0.91));
			return out;
		});

		Genai provider = mock(Genai.class);
		when(provider.perform()).thenReturn("```json\n[{\"domains\":[\"build\"],\"layers\":[\"Adapters\"],\"languages\":[{\"name\":\"Java\"}],\"integrations\":[]}]\n```");
		when(provider.embedding(anyString(), anyLong())).thenReturn(Arrays.asList(0.1, 0.2));
		Picker picker = new Picker(collection, provider);
		Configurator configurator = mock(Configurator.class);
		when(configurator.get("picker.classificationInstruction")).thenReturn("{1}");
		setField(picker, "configurator", configurator);
		picker.setScore(0.0);

		List<Bindex> result = picker.pick("find build libs");

		assertEquals(1, result.size());
		assertEquals("lib-a:2.0", result.get(0).getId());
		assertEquals(Double.valueOf(0.93), picker.getScore("lib-a:2.0"));
		assertEquals(Double.valueOf(0.91), picker.getScore("missing:1.0"));
	}

	@Test
	void getResults_shouldKeepHighestVersionPerLibraryName() throws Exception {
		MongoCollection<Document> collection = mock(MongoCollection.class);
		AggregateIterable<Document> aggregateIterable = mock(AggregateIterable.class);
		when(collection.aggregate(any(List.class))).thenReturn(aggregateIterable);
		when(aggregateIterable.into(any())).thenAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			Collection<Document> out = invocation.getArgument(0);
			out.add(new Document("id", "lib:1.0").append("name", "lib").append("version", "1.0").append("score", 0.80));
			out.add(new Document("id", "lib:2.0").append("name", "lib").append("version", "2.0").append("score", 0.95));
			return out;
		});
		Genai provider = mock(Genai.class);
		when(provider.embedding(anyString(), anyLong())).thenReturn(Collections.singletonList(0.2));
		Picker picker = new Picker(collection, provider);

		Method method = Picker.class.getDeclaredMethod("getResults", String.class, String.class, String.class, int.class, Bson[].class);
		method.setAccessible(true);

		@SuppressWarnings("unchecked")
		Collection<String> result = (Collection<String>) method.invoke(picker, "idx", "path", "query", 1, new Bson[0]);

		assertEquals(Collections.singletonList("lib:2.0"), new ArrayList<>(result));
	}

	@Test
	void getRegistredId_shouldReturnObjectIdStringWhenDocumentExists() {
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> iterable = mock(FindIterable.class);
		ObjectId objectId = new ObjectId();
		when(collection.find(any(Document.class))).thenReturn(iterable);
		when(iterable.first()).thenReturn(new Document("_id", objectId));
		Picker picker = new Picker(collection, mock(Genai.class));
		Bindex bindex = new Bindex();
		bindex.setId("found");

		String result = picker.getRegistredId(bindex);

		assertEquals(objectId.toString(), result);
	}

	@Test
	void getScore_shouldReturnNullWhenIdUnknown() {
		Picker picker = new Picker(mock(MongoCollection.class), mock(Genai.class));
		Double result = picker.getScore("unknown");
		assertNull(result);
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
