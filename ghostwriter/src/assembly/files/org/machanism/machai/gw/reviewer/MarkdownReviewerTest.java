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

class MarkdownReviewerTest {

	@TempDir
	File tempDir;

	@Test
	void getSupportedFileExtensions_returnsMd() {
		// Arrange
		MarkdownReviewer reviewer = new MarkdownReviewer();

		// Act
		String[] extensions = reviewer.getSupportedFileExtensions();

		// Assert
		assertEquals(1, extensions.length);
		assertEquals("md", extensions[0]);
	}

	@Test
	void perform_returnsNull_whenNoGuidance() throws IOException {
		// Arrange
		MarkdownReviewer reviewer = new MarkdownReviewer();
		File mdFile = new File(tempDir, "readme.md");
		Files.writeString(mdFile.toPath(), "# Title\nNo guidance\n", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, mdFile);

		// Assert
		assertNull(result);
	}

	@Test
	void perform_returnsPrompt_whenGuidanceCommentPresent() throws IOException {
		// Arrange
		MarkdownReviewer reviewer = new MarkdownReviewer();
		File mdFile = new File(tempDir, "readme.md");
		String content = "<!-- @guidance: Please improve docs -->\n# Title\nBody\n";
		Files.writeString(mdFile.toPath(), content, StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, mdFile);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("readme.md"));
		assertEquals(true, result.contains(content));
	}
}
