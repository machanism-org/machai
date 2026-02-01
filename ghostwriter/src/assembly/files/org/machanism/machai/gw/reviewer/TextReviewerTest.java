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

class TextReviewerTest {

	@TempDir
	File tempDir;

	@Test
	void getSupportedFileExtensions_returnsTxt() {
		// Arrange
		TextReviewer reviewer = new TextReviewer();

		// Act
		String[] extensions = reviewer.getSupportedFileExtensions();

		// Assert
		assertEquals(1, extensions.length);
		assertEquals("txt", extensions[0]);
	}

	@Test
	void perform_returnsNull_whenNotGuidanceFileName() throws IOException {
		// Arrange
		TextReviewer reviewer = new TextReviewer();
		File txt = new File(tempDir, "notes.txt");
		Files.writeString(txt.toPath(), "something", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, txt);

		// Assert
		assertNull(result);
	}

	@Test
	void perform_returnsFormattedPrompt_whenGuidanceFileNameMatches() throws IOException {
		// Arrange
		TextReviewer reviewer = new TextReviewer();
		File txt = new File(tempDir, "@guidance.txt");
		Files.writeString(txt.toPath(), "do this", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, txt);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("do this"));
	}

	@Test
	void getPrompt_returnsInputUnchanged_whenBlank() {
		// Arrange
		TextReviewer reviewer = new TextReviewer();
		File guidance = new File(tempDir, "@guidance.txt");

		// Act
		String result = reviewer.getPrompt(tempDir, guidance, "  ");

		// Assert
		assertEquals("  ", result);
	}
}
