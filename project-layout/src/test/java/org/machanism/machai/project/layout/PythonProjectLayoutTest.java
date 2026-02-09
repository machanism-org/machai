package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PythonProjectLayoutTest {

	@TempDir
	Path tempDir;

	@Test
	void isPythonProject_shouldReturnFalseWhenNoPyprojectToml() {
		// Arrange
		Path projectDir = tempDir.resolve("repo");
		assertDoesNotThrow(() -> Files.createDirectories(projectDir));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(projectDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnTrueWhenProjectNamePresentAndNotPrivate() throws IOException {
		// Arrange
		Path projectDir = tempDir.resolve("repo");
		Files.createDirectories(projectDir);
		String toml = "[project]\nname = \"my-lib\"\nclassifiers = [\"Development Status :: 4 - Beta\"]\n";
		Files.write(projectDir.resolve("pyproject.toml"), toml.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(projectDir.toFile());

		// Assert
		assertTrue(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenClassifierContainsPrivate_caseInsensitive() throws IOException {
		// Arrange
		Path projectDir = tempDir.resolve("repo");
		Files.createDirectories(projectDir);
		String toml = "[project]\nname = \"my-lib\"\nclassifiers = [\"Private :: Internal\"]\n";
		Files.write(projectDir.resolve("pyproject.toml"), toml.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(projectDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenPyprojectIsUnreadableOrInvalid() throws IOException {
		// Arrange
		Path projectDir = tempDir.resolve("repo");
		Files.createDirectories(projectDir);
		Files.write(projectDir.resolve("pyproject.toml"), "not toml".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(projectDir.toFile());

		// Assert
		assertFalse(result);
	}
}
