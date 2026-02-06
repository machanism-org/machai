package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TestDependenciesSanityTest {

	@Test
	void junitIsOnClasspath() {
		// Arrange
		// Act
		Class<?> junitTest = Test.class;

		// Assert
		assertNotNull(junitTest);
	}
}
