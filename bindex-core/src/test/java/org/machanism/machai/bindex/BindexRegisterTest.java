package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class BindexRegisterTest {

	@Test
	void constructor_throwsOnNullGenai() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new BindexRegister(null, null, config));

		// Assert
		assertEquals("genai must not be null", ex.getMessage());
	}

	@Test
	void constructor_throwsOnNullConf() {
		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new BindexRegister("openai", null, null));

		// Assert
		assertEquals("conf must not be null", ex.getMessage());
	}

	@Test
	void update_setsFlagAndReturnsThis_withoutTouchingExternalSystems() throws Exception {
		// Arrange
		BindexRegister register = newInstanceWithoutConstructor();

		// Act
		BindexRegister returned = register.update(true);

		// Assert
		assertEquals(register, returned);
		java.lang.reflect.Field updateField = BindexRegister.class.getDeclaredField("update");
		updateField.setAccessible(true);
		assertEquals(true, updateField.getBoolean(register));
	}

	@Test
	void processFolder_throwsOnNullProjectLayout_withoutTouchingExternalSystems() throws Exception {
		// Arrange
		BindexRegister register = newInstanceWithoutConstructor();

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> register.processFolder(null));

		// Assert
		assertEquals("projectLayout must not be null", ex.getMessage());
	}

	private static BindexRegister newInstanceWithoutConstructor() {
		try {
			java.lang.reflect.Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
			return (BindexRegister) unsafe.allocateInstance(BindexRegister.class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
