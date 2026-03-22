package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		Files.write(tempDir.resolve("pyproject.toml"),
				("[project]\n" +
						"name = \"myproj\"\n" +
						"classifiers = [\"Development Status :: 4 - Beta\"]\n")
						.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertTrue(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenPyprojectMissing() {
		// Arrange
		// no pyproject.toml

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenClassifiersContainPrivate_caseInsensitive() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("pyproject.toml"),
				("[project]\n" +
						"name = \"myproj\"\n" +
						"classifiers = [\"Private :: Do Not Publish\"]\n")
						.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenProjectNameMissing() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("pyproject.toml"),
				("[project]\n" +
						"classifiers = [\"Development Status :: 4 - Beta\"]\n")
						.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenPyprojectIsInvalidToml() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("pyproject.toml"), "this is not toml =".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void getSources_getDocuments_getTests_shouldReturnEmptyLists() {
		// Arrange
		PythonProjectLayout layout = new PythonProjectLayout();

		// Act / Assert
		assertNotNull(layout.getSources());
		assertNotNull(layout.getDocuments());
		assertNotNull(layout.getTests());
		assertTrue(layout.getSources().isEmpty());
		assertTrue(layout.getDocuments().isEmpty());
		assertTrue(layout.getTests().isEmpty());
	}
}
