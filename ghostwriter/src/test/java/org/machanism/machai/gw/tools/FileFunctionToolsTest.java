package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileFunctionToolsTest {

	@TempDir
	File tempDir;

	@Test
	void getRelativePath_whenNullDir_thenReturnsNull() {
		// Arrange
		File dir = null;
		File file = new File(".");

		// Act
		String result = FileFunctionTools.getRelativePath(dir, file, true);

		// Assert
		assertNull(result);
	}

	@Test
	void getRelativePath_whenNullFile_thenReturnsNull() {
		// Arrange
		File dir = tempDir;
		File file = null;

		// Act
		String result = FileFunctionTools.getRelativePath(dir, file, true);

		// Assert
		assertNull(result);
	}

	@Test
	void getRelativePath_whenSamePath_returnsDot() {
		// Arrange
		File dir = tempDir;
		File file = tempDir;

		// Act
		String result = FileFunctionTools.getRelativePath(dir, file, true);

		// Assert
		assertEquals(".", result);
	}

	@Test
	void getRelativePath_whenDescendant_addsDotAndNormalizesSlashes() throws Exception {
		// Arrange
		File dir = tempDir;
		File nested = new File(tempDir, "a\\b");
		assertTrue(nested.mkdirs());
		File file = new File(nested, "c.txt");
		Files.write(file.toPath(), "x".getBytes());

		// Act
		String result = FileFunctionTools.getRelativePath(dir, file, true);

		// Assert
		assertEquals("./a/b/c.txt", result);
	}

	@Test
	void getRelativePath_whenDescendantAndNoDot_doesNotAddDot() throws Exception {
		// Arrange
		File dir = tempDir;
		File file = new File(tempDir, "x.txt");
		Files.write(file.toPath(), "x".getBytes());

		// Act
		String result = FileFunctionTools.getRelativePath(dir, file, false);

		// Assert
		assertEquals("x.txt", result);
	}

	@Test
	void getRelativePath_whenNotDescendant_thenReturnsParentRelativePath() {
		// Arrange
		File dir = new File(tempDir, "dir");
		File file = new File(tempDir, "other");

		// Act
		String result = FileFunctionTools.getRelativePath(dir, file, true);

		// Assert
		assertEquals("../other", result);
	}

	@Test
	void getRelativePath_whenEmptyRelativePath_thenReturnsDot() {
		// Arrange
		File dir = tempDir;
		File file = new File(tempDir.getAbsolutePath());

		// Act
		String result = FileFunctionTools.getRelativePath(dir, file, false);

		// Assert
		assertEquals(".", result);
	}
}
