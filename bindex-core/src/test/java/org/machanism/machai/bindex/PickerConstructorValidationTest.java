package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Focused tests for {@link Picker} constructor validation.
 */
class PickerConstructorValidationTest {

	@Test
	void constructor_shouldThrowWhenGenAiIsNull() {
		// Arrange
		Configurator config = null;

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Picker(null, null, config));
		assertEquals("genai must not be null", ex.getMessage());
	}

	@Test
	void constructor_shouldThrowWhenConfiguratorIsNull() {
		// Arrange
		String genai = "openai";

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Picker(genai, null, null));
		assertEquals("config must not be null", ex.getMessage());
	}
}
