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

class HtmlReviewerTest {

	@TempDir
	File tempDir;

	@Test
	void getSupportedFileExtensions_returnsExpected() {
		// Arrange
		HtmlReviewer reviewer = new HtmlReviewer();

		// Act
		String[] extensions = reviewer.getSupportedFileExtensions();

		// Assert
		assertEquals(3, extensions.length);
		assertEquals("html", extensions[0]);
		assertEquals("htm", extensions[1]);
		assertEquals("xml", extensions[2]);
	}

	@Test
	void perform_returnsNull_whenNoGuidance() throws IOException {
		// Arrange
		HtmlReviewer reviewer = new HtmlReviewer();
		File htmlFile = new File(tempDir, "index.html");
		Files.writeString(htmlFile.toPath(), "<html><body>No guidance here.</body></html>", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, htmlFile);

		// Assert
		assertNull(result);
	}

	@Test
	void perform_extractsGuidance_fromHtmlComment() throws IOException {
		// Arrange
		HtmlReviewer reviewer = new HtmlReviewer();
		File htmlFile = new File(tempDir, "guide.html");
		String content = "<!-- @guidance: HTML specific guidance -->\n<html></html>";
		Files.writeString(htmlFile.toPath(), content, StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, htmlFile);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("HTML specific guidance"));
		assertEquals(true, result.contains("guide.html"));
		assertEquals(true, result.contains(content));
	}
}
