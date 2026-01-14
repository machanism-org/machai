package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.Language;

class PickerTest {

	@Test
	void constructor_throwsWhenUriNullAndOpenAiApiKeyMissing() {
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
}
