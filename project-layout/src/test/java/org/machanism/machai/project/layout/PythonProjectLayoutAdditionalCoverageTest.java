package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PythonProjectLayoutAdditionalCoverageTest {

	@TempDir
	Path tempDir;

	@Test
	void isPythonProject_shouldReturnTrueWhenClassifiersDoNotContainPrivate() throws Exception {
		// Arrange
		Files.write(tempDir.resolve("pyproject.toml"),
				("[project]\n" + "name = \"myproj\"\n" + "classifiers = [\"Public\", \"Beta\"]\n")
						.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertTrue(result);
	}
}
