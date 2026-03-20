package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

class MachaiCLITest {

	@Test
	void constructor_shouldBeAccessibleAndCreateInstance() throws Exception {
		// Arrange
		Constructor<MachaiCLI> ctor = MachaiCLI.class.getDeclaredConstructor();
		ctor.setAccessible(true);

		// Act
		MachaiCLI instance = ctor.newInstance();

		// Assert
		assertEquals(MachaiCLI.class, instance.getClass());
	}

	@Test
	void loadSystemProperties_whenNoConfigAndNoFile_shouldNotThrow() {
		// Arrange
		System.clearProperty("config");

		// Act + Assert
		assertDoesNotThrow(() -> {
			var m = MachaiCLI.class.getDeclaredMethod("loadSystemProperties");
			m.setAccessible(true);
			m.invoke(null);
		});
	}
}
