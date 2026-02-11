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
	void isPythonProject_shouldReturnTrueWhenPyprojectHasNameAndNotPrivate() throws IOException {
		// Arrange
		String pyproject = "[project]\n" +
				"name = \"demo\"\n" +
				"classifiers = [\"Development Status :: 4 - Beta\"]\n";
		Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertTrue(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenClassifierContainsPrivate() throws IOException {
		// Arrange
		String pyproject = "[project]\n" +
				"name = \"demo\"\n" +
				"classifiers = [\"Private :: Do Not Publish\"]\n";
		Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenNameMissing() throws IOException {
		// Arrange
		String pyproject = "[project]\n" +
				"classifiers = [\"Development Status :: 4 - Beta\"]\n";
		Files.write(tempDir.resolve("pyproject.toml"), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenPyprojectCannotBeParsed() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("pyproject.toml"), "not=toml=[".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenPyprojectNotPresent() {
		// Arrange
		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void notImplementedMethods_shouldReturnNull() {
		// Arrange
		PythonProjectLayout layout = (PythonProjectLayout) new PythonProjectLayout().projectDir(tempDir.toFile());

		// Act & Assert
		assertNull(layout.getSources());
		assertNull(layout.getDocuments());
		assertNull(layout.getTests());
	}
}
