package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.machai.schema.Language;

class PickerTest {

	@Test
	void getNormalizedLanguageName_trimsLowercasesAndStripsParentheses() {
		// Arrange
		Language language = new Language();
		language.setName("  Java (JVM) ");

		// Act
		String normalized = invokeGetNormalizedLanguageName(language);

		// Assert
		assertEquals("java", normalized);
	}

	private static String invokeGetNormalizedLanguageName(Language language) {
		try {
			Method method = Picker.class.getDeclaredMethod("getNormalizedLanguageName", Language.class);
			method.setAccessible(true);
			return (String) method.invoke(null, language);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
