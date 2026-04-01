package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Focused unit tests for {@link Picker} that do not require MongoDB connectivity.
 *
 * <p>
 * These tests validate constructor argument handling and simple property
 * accessors.
 */
class PickerConstructorAndModelTest {

	@Test
	void constructor_shouldThrowWhenGenaiIsNull() {
		// Arrange
		String genai = null;

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Picker(genai, null, null));
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
