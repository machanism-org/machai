package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class MachaiCLIMainTest {

	@Test
	void mainMethod_shouldExist() throws Exception {
		// Arrange
		Method m = MachaiCLI.class.getDeclaredMethod("main", String[].class);

		// Act + Assert
		assertTrue(m.canAccess(null));
	}
}
