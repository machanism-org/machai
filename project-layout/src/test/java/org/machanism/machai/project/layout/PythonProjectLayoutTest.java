package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

class PythonProjectLayoutTest {

	@Test
	void isPythonProject_shouldReturnTrueWhenPyprojectHasNameAndNotPrivate() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/python-project-public");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		String pyproject = "[project]\n" + "name = \"demo\"\n" + "classifiers = [\"Programming Language :: Python\"]\n";
		Files.write(new File(dir, "pyproject.toml").toPath(), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(dir);

		// Assert
		assertTrue(result);
	}

	@Test
	void isPythonProject_shouldReturnTrueWhenNoClassifiersProvidedButNamePresent() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/python-project-no-classifiers");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		String pyproject = "[project]\n" + "name = \"demo\"\n";
		Files.write(new File(dir, "pyproject.toml").toPath(), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(dir);

		// Assert
		assertTrue(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenProjectNameMissing() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/python-project-no-name");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		String pyproject = "[project]\n" + "classifiers = [\"Programming Language :: Python\"]\n";
		Files.write(new File(dir, "pyproject.toml").toPath(), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(dir);

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenClassifierContainsPrivate_caseInsensitive() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/python-project-private");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		String pyproject = "[project]\n" + "name = \"demo\"\n" + "classifiers = [\"pRiVaTe :: Do Not Publish\"]\n";
		Files.write(new File(dir, "pyproject.toml").toPath(), pyproject.getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(dir);

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenPyprojectMissing() {
		// Arrange
		File dir = new File("target/test-tmp/python-project-missing");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		// Act
		boolean result = PythonProjectLayout.isPythonProject(dir);

		// Assert
		assertFalse(result);
	}

	@Test
	void isPythonProject_shouldReturnFalseWhenPyprojectInvalidToml() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/python-project-invalid");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Files.write(new File(dir, "pyproject.toml").toPath(), "not-toml".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = PythonProjectLayout.isPythonProject(dir);

		// Assert
		assertFalse(result);
	}

	@Test
	void getSources_documents_tests_shouldReturnNull() {
		// Arrange
		PythonProjectLayout layout = (PythonProjectLayout) new PythonProjectLayout()
				.projectDir(new File("target/test-tmp/python-nulls"));

		// Act + Assert
		assertNull(layout.getSources());
		assertNull(layout.getDocuments());
		assertNull(layout.getTests());
	}
}
