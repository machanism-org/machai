package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

class PythonProjectLayoutTest {

	@Test
	void isPythonProject_whenPyprojectHasNameAndNoPrivateClassifier_returnsTrue() {
		// Arrange
		File projectDir = new File("src/test/resources/mockPythonProject");

		// Act
		boolean python = PythonProjectLayout.isPythonProject(projectDir);

		// Assert
		assertTrue(python);
	}

	@Test
	void isPythonProject_whenPyprojectMissing_returnsFalse() {
		// Arrange
		File projectDir = new File("src/test/resources/mockDefaultProject");

		// Act
		boolean python = PythonProjectLayout.isPythonProject(projectDir);

		// Assert
		assertFalse(python);
	}

	@Test
	void getSources_returnsNull() {
		// Arrange
		PythonProjectLayout layout = new PythonProjectLayout();

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNull(sources);
	}

	@Test
	void getDocuments_returnsNull() {
		// Arrange
		PythonProjectLayout layout = new PythonProjectLayout();

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertNull(docs);
	}

	@Test
	void getTests_returnsNull() {
		// Arrange
		PythonProjectLayout layout = new PythonProjectLayout();

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNull(tests);
	}
}
