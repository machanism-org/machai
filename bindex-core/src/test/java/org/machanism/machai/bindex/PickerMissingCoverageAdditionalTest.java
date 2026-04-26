package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.schema.Bindex;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

class PickerMissingCoverageAdditionalTest {

	@Test
	void addDependencies_shouldIgnoreAlreadyVisitedDependencies() {
		Picker picker = new Picker(mock(MongoCollection.class), mock(Genai.class)) {
			@Override
			protected Bindex getBindex(String id) {
				if ("root".equals(id)) {
					Bindex bindex = new Bindex();
					bindex.setId("root");
					bindex.setDependencies(Collections.singletonList("child"));
					return bindex;
				}
				if ("child".equals(id)) {
					Bindex bindex = new Bindex();
					bindex.setId("child");
					bindex.setDependencies(Collections.singletonList("root"));
					return bindex;
				}
				return null;
			}
		};
		Set<String> dependencies = new HashSet<>();

		picker.addDependencies(dependencies, "root");

		assertEquals(new HashSet<>(java.util.Arrays.asList("root", "child")), dependencies);
	}

	@Test
	void getBindex_shouldThrowIllegalArgumentExceptionForInvalidStoredJson() {
		MongoCollection<Document> collection = mock(MongoCollection.class);
		FindIterable<Document> iterable = mock(FindIterable.class);
		when(collection.find(any(org.bson.conversions.Bson.class))).thenReturn(iterable);
		when(iterable.first()).thenReturn(new Document(Picker.BINDEX_PROPERTY_NAME, "not-json"));
		Picker picker = new Picker(collection, mock(Genai.class));

		assertThrows(IllegalArgumentException.class, () -> picker.getBindex("broken"));
	}

	@Test
	void getClassificationText_shouldSerializeClassification() throws Exception {
		Picker picker = new Picker(mock(MongoCollection.class), mock(Genai.class));
		org.machanism.machai.schema.Classification classification = new org.machanism.machai.schema.Classification();
		classification.setDomains(Collections.singletonList("domain"));
		classification.setLayers(Collections.emptyList());
		classification.setLanguages(Collections.emptyList());
		classification.setIntegrations(Collections.emptyList());
		Method method = Picker.class.getDeclaredMethod("getClassificationText", org.machanism.machai.schema.Classification.class);
		method.setAccessible(true);

		String json = (String) method.invoke(picker, classification);

		assertEquals(true, json.contains("domain"));
	}
}
