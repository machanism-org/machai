package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BindexRegister} that focus on argument validation.
 */
class BindexRegisterProcessFolderTest {

	@Test
	void processFolder_shouldThrowWhenProjectLayoutIsNull() throws Exception {
		// Arrange
		BindexRegister register = allocateWithoutConstructor(BindexRegister.class);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> register.processFolder(null));
	}

	@Test
	void update_shouldReturnSameInstanceAndSetFlag() throws Exception {
		// Arrange
		BindexRegister register = allocateWithoutConstructor(BindexRegister.class);

		// Act
		BindexRegister chained = register.update(true);

		// Assert
		assertSame(register, chained);
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
