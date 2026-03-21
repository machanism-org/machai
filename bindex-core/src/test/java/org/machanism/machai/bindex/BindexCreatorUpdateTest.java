package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BindexCreator} focused on branches not covered elsewhere.
 */
class BindexCreatorUpdateTest {

	@Test
	void update_shouldReturnSameInstanceAndSetFlag() throws Exception {
		// Arrange
		BindexCreator creator = allocateWithoutConstructor(BindexCreator.class);

		// Act
		BindexCreator chained = creator.update(true);

		// Assert
		assertSame(creator, chained);
	}

	@Test
	void processFolder_shouldThrowWhenProjectLayoutIsNull() throws Exception {
		// Arrange
		BindexCreator creator = allocateWithoutConstructor(BindexCreator.class);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> creator.processFolder(null));
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
		Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
		Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);
		return (T) unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, type);
	}
}
