package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Unit tests for {@link BindexRegister}.
 *
 * <p>Note: end-to-end testing of {@link BindexRegister#processFolder(org.machanism.machai.project.layout.ProjectLayout)}
 * is already covered in {@link BindexProjectProcessorTest} using real project layouts. On this
 * runtime, Mockito/Unsafe based approaches cannot instantiate or mock {@code ProjectLayout}
 * (it's non-instantiable in the dependency), so we limit this class to constructor/flag logic.
 */
class BindexRegisterTest {

	@Test
	void constructor_shouldRejectNullArguments() {
		// Arrange
		Configurator config = mockConfigurator();

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new BindexRegister(null, null, config));
		assertThrows(IllegalArgumentException.class, () -> new BindexRegister("openai", null, null));
	}

	@Test
	void update_shouldSetFlagAndReturnSameInstance() throws Exception {
		// Arrange
		BindexRegister register = (BindexRegister) UnsafeAllocator.allocateWithoutConstructor(BindexRegister.class);

		// Act
		BindexRegister returned = register.update(true);

		// Assert
		assertSame(register, returned);
		java.lang.reflect.Field updateField = BindexRegister.class.getDeclaredField("update");
		updateField.setAccessible(true);
		assertTrue((boolean) updateField.get(register));
	}

	private static Configurator mockConfigurator() {
		// Configurator is an interface from core-commons and is mockable in existing tests.
		return org.mockito.Mockito.mock(Configurator.class);
	}

	/**
	 * Allocate instances without running constructors.
	 */
	private static final class UnsafeAllocator {
		static Object allocateWithoutConstructor(Class<?> type) {
			try {
				Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
				java.lang.reflect.Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
				theUnsafe.setAccessible(true);
				Object unsafe = theUnsafe.get(null);
				return unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, type);
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
	}
}
