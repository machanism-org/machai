package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileFunctionToolsAdditionalTest {

	@TempDir
	File tempDir;

	@Test
	void getRelativePath_whenDirOrFileNull_returnsNull() {
		// Arrange
		File dir = null;
		File file = new File("x");

		// Act
		String result1 = FileFunctionTools.getRelativePath(dir, file, true);
		String result2 = FileFunctionTools.getRelativePath(new File("."), null, true);

		// Assert
		assertNull(result1);
		assertNull(result2);
	}

	@Test
	void getRelativePath_whenSamePath_returnsDot() {
		// Arrange
		File dir = new File(tempDir, "same");
		assertTrue(dir.mkdirs());

		// Act
		String result = FileFunctionTools.getRelativePath(dir, dir, true);

		// Assert
		assertEquals(".", result);
	}

	@Test
	void getRelativePath_whenPathNotDescendant_returnsRelativeWithParentSegments() {
		// Arrange
		File base = new File(tempDir, "base");
		File outside = new File(tempDir, "outside");
		assertTrue(base.mkdirs());
		assertTrue(outside.mkdirs());

		// Act
		String result = FileFunctionTools.getRelativePath(base, outside, true);

		// Assert
		assertEquals("../outside", result);
	}

	@Test
	void getRelativePath_whenAddSingleDotFalse_doesNotPrefix() {
		// Arrange
		File base = new File(tempDir, "base");
		File child = new File(base, "a/b.txt");
		assertTrue(child.getParentFile().mkdirs());

		// Act
		String result = FileFunctionTools.getRelativePath(base, child, false);

		// Assert
		assertEquals("a/b.txt", result);
	}
}
