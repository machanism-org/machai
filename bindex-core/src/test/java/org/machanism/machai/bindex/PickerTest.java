package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Language;

class PickerTest {

	@Test
	void constructor_acceptsNullUri() {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);

		// Act + Assert
		assertDoesNotThrow(() -> new Picker(provider, null));
	}

	@Test
	void getNormalizedLanguageName_lowerCasesTrimsAndRemovesParentheses() {
		// Arrange
		Language lang = mock(Language.class);
		when(lang.getName()).thenReturn("  JavaScript (Node.js)  ");

		// Act
		String normalized = Picker.getNormalizedLanguageName(lang);

		// Assert
		assertEquals("javascript", normalized);
	}

	@Test
	void getNormalizedLanguageName_whenNoParentheses_returnsLowerCasedTrimmed() {
		// Arrange
		Language lang = mock(Language.class);
		when(lang.getName()).thenReturn("  Kotlin  ");

		// Act
		String normalized = Picker.getNormalizedLanguageName(lang);

		// Assert
		assertEquals("kotlin", normalized);
	}

	@Test
	void getScore_returnsValueFromInternalScoreMap() throws Exception {
		// Arrange
		Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017");
		Field f = Picker.class.getDeclaredField("scoreMap");
		f.setAccessible(true);
		Map<String, Double> map = new HashMap<>();
		map.put("id", 0.91);
		f.set(picker, map);

		// Act
		Double score = picker.getScore("id");

		// Assert
		assertEquals(0.91, score);
	}

	@Test
	void getScore_returnsNullWhenIdNotPresent() {
		// Arrange
		Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017");

		// Act
		Double score = picker.getScore("missing");

		// Assert
		assertNull(score);
	}

	@Test
	void addDependencies_doesNotRecurseInfinitelyOnCycles() {
		// Arrange
		Bindex a = mock(Bindex.class);
		when(a.getId()).thenReturn("a");
		when(a.getDependencies()).thenReturn(java.util.List.of("b"));

		Bindex b = mock(Bindex.class);
		when(b.getId()).thenReturn("b");
		when(b.getDependencies()).thenReturn(java.util.List.of("a"));

		Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017") {
			@Override
			protected Bindex getBindex(String id) {
				if ("a".equals(id)) {
					return a;
				}
				if ("b".equals(id)) {
					return b;
				}
				return null;
			}
		};

		Set<String> deps = new LinkedHashSet<>();

		// Act
		picker.addDependencies(deps, "a");

		// Assert
		assertEquals(Set.of("a", "b"), deps);
	}

	@Test
	void addDependencies_handlesMissingBindexGracefully() {
		// Arrange
		Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017") {
			@Override
			protected Bindex getBindex(String id) {
				return null;
			}
		};
		Set<String> deps = new LinkedHashSet<>();

		// Act
		picker.addDependencies(deps, "missing");

		// Assert
		assertEquals(Set.of(), deps);
	}

	@Test
	void setEmbeddingModelName_updatesEmbeddingModelName() {
		// Arrange
		Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017");

		// Act
		picker.setEmbeddingModelName("model-x");

		// Assert
		assertEquals("model-x", picker.getEmbeddingModelName());
	}
}
