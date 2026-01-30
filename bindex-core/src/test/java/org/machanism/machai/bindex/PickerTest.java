package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.machanism.machai.schema.Language;

class PickerTest {

	@Test
	void getNormalizedLanguageName_stripsParenContent_trimsAndLowercases() {
		// Arrange
		Language language = new Language();
		language.setName("  Java (JVM) ");

		// Act
		String normalized = Picker.getNormalizedLanguageName(language);

		// Assert
		assertEquals("java", normalized);
	}

	@Test
	void getNormalizedLanguageName_whenNoParen_returnsLowercasedTrimmed() {
		// Arrange
		Language language = new Language();
		language.setName("  PyThOn  ");

		// Act
		String normalized = Picker.getNormalizedLanguageName(language);

		// Assert
		assertEquals("python", normalized);
	}
}
