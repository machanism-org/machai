package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Language;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

class PickerPickFlowTest {

	@Test
	void pick_shouldParseMarkdownJsonAndReturnResolvedBindexes() throws Exception {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		AggregateIterable<Document> aggregateIterable = mock(AggregateIterable.class);
		FindIterable<Document> firstBindex = mock(FindIterable.class);
		FindIterable<Document> secondBindex = mock(FindIterable.class);
		when(collection.aggregate(any(List.class))).thenReturn(aggregateIterable);
		when(aggregateIterable.into(any())).thenAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			List<Document> out = invocation.getArgument(0);
			out.add(new Document("id", "spring:3.0.0").append("name", "spring").append("version", "3.0.0")
					.append("score", 0.97));
			out.add(new Document("id", "jackson:2.0.0").append("name", "jackson").append("version", "2.0.0")
					.append("score", 0.93));
			return out;
		});
		when(collection.find(any(org.bson.conversions.Bson.class))).thenReturn(firstBindex, secondBindex);
		when(firstBindex.first()).thenReturn(new Document(Picker.BINDEX_PROPERTY_NAME,
				"{\"id\":\"spring:3.0.0\",\"name\":\"spring\",\"version\":\"3.0.0\"}"));
		when(secondBindex.first()).thenReturn(new Document(Picker.BINDEX_PROPERTY_NAME,
				"{\"id\":\"jackson:2.0.0\",\"name\":\"jackson\",\"version\":\"2.0.0\"}"));
		EmbeddingProvider embeddingProvider = mock(EmbeddingProvider.class);
		when(embeddingProvider.embedding(anyString(), anyInt())).thenReturn(Collections.singletonList(0.2d));
		Genai genai = mock(Genai.class);
		doNothing().when(genai).prompt(anyString());
		when(genai.perform()).thenReturn(
				"```json\n[{\"domains\":[\"backend\"],\"layers\":[\"Adapters\"],\"languages\":[{\"name\":\"Java (JVM)\"}],\"integrations\":[]}]\n```");
		Picker picker = new Picker(collection, genai, embeddingProvider);
		picker.setScore(0.0d);

		// Act
		List<Bindex> result = picker.pick("Build a Java backend");

		// Assert
		assertEquals(2, result.size());
		assertEquals("spring:3.0.0", result.get(0).getId());
		assertEquals("jackson:2.0.0", result.get(1).getId());
	}

	@Test
	void pick_shouldReturnEmptyListWhenClassificationProducesNoLayers() throws Exception {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		EmbeddingProvider embeddingProvider = mock(EmbeddingProvider.class);
		Genai genai = mock(Genai.class);
		doNothing().when(genai).prompt(anyString());
		when(genai.perform()).thenReturn(
				"[{\"domains\":[\"backend\"],\"layers\":[],\"languages\":[{\"name\":\"Java\"}],\"integrations\":[]}]");
		Picker picker = new Picker(collection, genai, embeddingProvider);

		// Act
		List<Bindex> result = picker.pick("Build a Java backend");

		// Assert
		assertEquals(Collections.emptyList(), result);
	}

	@Test
	void pick_shouldPropagateInvalidClassificationJson() {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		EmbeddingProvider embeddingProvider = mock(EmbeddingProvider.class);
		Genai genai = mock(Genai.class);
		doNothing().when(genai).prompt(anyString());
		when(genai.perform()).thenReturn("not-json");
		Picker picker = new Picker(collection, genai, embeddingProvider);

		// Act + Assert
		assertThrows(Exception.class, () -> picker.pick("Build a backend"));
	}

	@Test
	void getResults_shouldHandleNullBsonsArray() throws Exception {
		// Arrange
		MongoCollection<Document> collection = mock(MongoCollection.class);
		AggregateIterable<Document> aggregateIterable = mock(AggregateIterable.class);
		when(collection.aggregate(any(List.class))).thenReturn(aggregateIterable);
		when(aggregateIterable.into(any())).thenAnswer(invocation -> invocation.getArgument(0));
		EmbeddingProvider embeddingProvider = mock(EmbeddingProvider.class);
		when(embeddingProvider.embedding(anyString(), anyInt())).thenReturn(Collections.singletonList(0.1d));
		Picker picker = new Picker(collection, mock(Genai.class), embeddingProvider);

		Method method = Picker.class.getDeclaredMethod("getResults", String.class, String.class, String.class,
				int.class, org.bson.conversions.Bson[].class);
		method.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		List<String> result = new ArrayList<>(
				(java.util.Collection<String>) method.invoke(picker, "idx", "path", "q", 1, (Object) null));

		// Assert
		assertEquals(Collections.emptyList(), result);
	}

	@Test
	void getClassification_shouldUseDefaultInstructionWhenConfiguratorMissing() throws Exception {
		// Arrange
		Genai provider = mock(Genai.class);
		List<String> prompts = new ArrayList<>();
		org.mockito.Mockito.doAnswer(invocation -> {
			prompts.add(invocation.getArgument(0));
			return null;
		}).when(provider).prompt(anyString());
		when(provider.perform()).thenReturn("[]");
		Picker picker = new Picker(mock(MongoCollection.class), provider, mock(EmbeddingProvider.class));
		Method method = Picker.class.getDeclaredMethod("getClassification", String.class);
		method.setAccessible(true);

		// Act
		String result = (String) method.invoke(picker, "Need libraries");

		// Assert
		assertEquals("[]", result);
		org.junit.jupiter.api.Assertions.assertTrue(prompts.get(0).contains("Need libraries"));
	}

	@Test
	void getNormalizedLanguageName_shouldKeepTextWithoutParentheses() {
		// Arrange
		Language language = new Language();
		language.setName(" TypeScript ");

		// Act
		String result = Picker.getNormalizedLanguageName(language);

		// Assert
		assertEquals("typescript", result);
	}
}
