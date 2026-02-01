package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PythonReviewerTest {

	@TempDir
	File tempDir;

	@Test
	void getSupportedFileExtensions_returnsPy() {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();

		// Act
		String[] extensions = reviewer.getSupportedFileExtensions();

		// Assert
		assertEquals(1, extensions.length);
		assertEquals("py", extensions[0]);
	}

	@Test
	void perform_returnsNull_whenNoGuidance() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();
		File pyFile = new File(tempDir, "a.py");
		Files.writeString(pyFile.toPath(), "print('hi')\n", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, pyFile);

		// Assert
		assertNull(result);
	}

	@Test
	void perform_extractsGuidance_fromHashComment() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();
		File pyFile = new File(tempDir, "a.py");
		String content = "# @guidance: do thing\nprint('hi')\n";
		Files.writeString(pyFile.toPath(), content, StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, pyFile);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("a.py"));
		assertEquals(true, result.contains("do thing"));
	}

	@Test
	void perform_extractsGuidance_fromTripleQuotedBlock() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();
		File pyFile = new File(tempDir, "a.py");
		String content = "\"\"\"\n@guidance: block\n\"\"\"\nprint('hi')\n";
		Files.writeString(pyFile.toPath(), content, StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, pyFile);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("block"));
	}
}
