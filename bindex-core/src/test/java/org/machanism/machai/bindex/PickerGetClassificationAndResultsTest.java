package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

class PickerGetClassificationAndResultsTest {

	@Test
	void getClassification_shouldReturnText_fromProvider() throws Exception {
		// Arrange
		Genai genai = mock(Genai.class);
		when(genai.perform()).thenReturn("[{\"domains\":[\"java\"],\"layers\":[\"backend\"],\"languages\":[],\"integrations\":[]}]");
		Picker picker = new Picker(mock(MongoCollection.class), genai);
		Configurator configurator = mock(Configurator.class);
		when(configurator.get("picker.classificationInstruction")).thenReturn("{1}");
		setField(picker, "configurator", configurator);

		Method method = Picker.class.getDeclaredMethod("getClassification", String.class);
		method.setAccessible(true);

		// Act
		String result = (String) method.invoke(picker, "Find libs");

		// Assert
		assertNotNull(result);
		org.junit.jupiter.api.Assertions.assertTrue(result.startsWith("["));
	}

	@Test
	void getResults_shouldBuildDescriptions_andReturnCollection() throws Exception {
		// Arrange
		@SuppressWarnings("unchecked")
		MongoCollection<Document> collection = mock(MongoCollection.class);
		AggregateIterable<Document> aggregateIterable = mock(AggregateIterable.class);
		when(collection.aggregate(any())).thenReturn(aggregateIterable);

		Document d1 = new Document("id", "id1").append("name", "n1").append("version", "1")
				.append("description", "desc1")
				.append("score", 0.9);
		Document d2 = new Document("id", "id2").append("name", "n2").append("version", "2")
				.append("description", "desc2")
				.append("score", 0.1);
		when(aggregateIterable.into(any())).thenAnswer(inv -> {
			@SuppressWarnings("unchecked")
			java.util.Collection<Document> out = (java.util.Collection<Document>) inv.getArgument(0);
			out.add(d1);
			out.add(d2);
			return out;
		});

		Genai genai = mock(Genai.class);
		when(genai.embedding(anyString(), anyInt())).thenReturn(Arrays.asList(0.1, 0.2));
		Picker picker = new Picker(collection, genai);
		picker.setScore(0.0);

		Method method = Picker.class.getDeclaredMethod("getResults", String.class, String.class, String.class, int.class,
				org.bson.conversions.Bson[].class);
		method.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Collection<String> results = (Collection<String>) method.invoke(picker, "idx", "path", "q", 2,
				new org.bson.conversions.Bson[0]);

		// Assert
		assertEquals(2, results.size());
		String joined = String.join("\n", results);
		org.junit.jupiter.api.Assertions.assertTrue(joined.contains("n1:1"));
		org.junit.jupiter.api.Assertions.assertTrue(joined.contains("n2:2"));
		assertEquals(Double.valueOf(0.9), picker.getScore("id1"));
	}

	@Test
	void getResults_shouldThrow_whenCollectionAggregateThrows() throws Exception {
		// Arrange
		@SuppressWarnings("unchecked")
		MongoCollection<Document> collection = mock(MongoCollection.class);
		when(collection.aggregate(any())).thenThrow(new RuntimeException("boom"));

		Genai genai = mock(Genai.class);
		when(genai.embedding(anyString(), anyInt())).thenReturn(Collections.singletonList(0.1));
		Picker picker = new Picker(collection, genai);

		Method method = Picker.class.getDeclaredMethod("getResults", String.class, String.class, String.class, int.class,
				org.bson.conversions.Bson[].class);
		method.setAccessible(true);

		// Act + Assert
		assertThrows(java.lang.reflect.InvocationTargetException.class, () -> method.invoke(picker, "idx", "path", "q", 1,
				new org.bson.conversions.Bson[0]));
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
