package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Focused unit tests for {@link Picker} that do not require MongoDB connectivity.
 *
 * <p>These tests validate constructor argument handling and simple property accessors.
 */
class PickerConstructorAndModelTest {

	@Test
	void constructor_shouldThrowWhenGenaiIsNull() {
		// Arrange
		String genai = null;

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new Picker(genai, null, null));
	}

	@Test
	void constructor_shouldThrowWhenConfiguratorIsNull() {
		// Arrange
		String genai = "openai";

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new Picker(genai, null, null));
	}

	@Test
	void getEmbeddingModelNameAndSetEmbeddingModelName_shouldRoundTrip() throws Exception {
		// Arrange
		Picker picker = allocateWithoutConstructor(Picker.class);
		// Default field value is set via field initializer; allocateInstance bypasses that.
		setField(picker, "embeddingModelName", "text-embedding-3-small");

		// Act
		picker.setEmbeddingModelName("custom-model");

		// Assert
		assertEquals("custom-model", picker.getEmbeddingModelName());
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
		Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
		Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);
		return (T) unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, type);
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
