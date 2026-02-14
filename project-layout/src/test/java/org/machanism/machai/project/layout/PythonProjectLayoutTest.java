package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PythonProjectLayoutTest {

	@TempDir
	File tempDir;

	@Test
	void isPythonProject_whenValidPyProjectAndNotPrivate_returnsTrue() throws Exception {
		// Arrange
		String toml = "[project]\n" + "name = \"demo\"\n" + "classifiers = [\"License :: OSI Approved\"]\n";
		Files.write(new File(tempDir, "pyproject.toml").toPath(), toml.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir);

		// Assert
		assertTrue(result);
	}

	@Test
	void isPythonProject_whenPrivateClassifierPresent_returnsFalse() throws Exception {
		// Arrange
		String toml = "[project]\n" + "name = \"demo\"\n" + "classifiers = [\"Private :: Do Not Publish\"]\n";
		Files.write(new File(tempDir, "pyproject.toml").toPath(), toml.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir);

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_whenMissingName_returnsFalse() throws Exception {
		// Arrange
		String toml = "[project]\n" + "classifiers = [\"License :: OSI Approved\"]\n";
		Files.write(new File(tempDir, "pyproject.toml").toPath(), toml.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir);

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_whenTomlUnreadable_returnsFalse() throws Exception {
		// Arrange
		Files.createDirectories(new File(tempDir, "pyproject.toml").toPath());

		// Act
		boolean result = PythonProjectLayout.isPythonProject(tempDir);

		// Assert
		assertFalse(result);
	}
}
