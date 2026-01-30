package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PythonProjectLayoutTest {

	@Test
	void isPythonProject_whenPyprojectHasNameAndNotPrivate_returnsTrue() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("py-layout-");
		writeFile(tempDir.resolve("pyproject.toml"), "[project]\nname = 'mypkg'\n");

		// Act
		boolean isPython = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertTrue(isPython);
	}

	@Test
	void isPythonProject_whenClassifiersContainPrivate_returnsFalse() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("py-layout-private-");
		writeFile(tempDir.resolve("pyproject.toml"), "[project]\nname = 'mypkg'\nclassifiers = ['Private :: Do Not Publish']\n");

		// Act
		boolean isPython = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(isPython);
	}

	@Test
	void isPythonProject_whenPyprojectMissing_returnsFalse() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("py-layout-missing-");

		// Act
		boolean isPython = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(isPython);
	}

	@Test
	void isPythonProject_whenPyprojectIsInvalidToml_returnsFalse() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("py-layout-invalid-");
		writeFile(tempDir.resolve("pyproject.toml"), "not toml at all = =");

		// Act
		boolean isPython = PythonProjectLayout.isPythonProject(tempDir.toFile());

		// Assert
		assertFalse(isPython);
	}

	private static void writeFile(Path path, String content) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
			fos.write(content.getBytes(StandardCharsets.UTF_8));
		}
	}
}
