package org.machanism.machai.assembly.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * Smoke tests for package-level metadata.
 */
class PackageInfoTest {

	@Test
	void packageExists_andCanBeLoaded() {
		// Arrange + Act + Assert
		assertDoesNotThrow(() -> Class.forName("org.machanism.machai.assembly.maven.Assembly"));
	}
}
