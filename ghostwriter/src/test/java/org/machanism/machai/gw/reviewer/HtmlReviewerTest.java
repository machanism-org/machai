package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HtmlReviewerTest {

	@TempDir
	Path tempDir;

	@Test
	void getSupportedFileExtensions_returnsHtmlHtmXml() {
		// Arrange
		HtmlReviewer reviewer = new HtmlReviewer();

		// Act + Assert
		assertArrayEquals(new String[] { "html", "htm", "xml" }, reviewer.getSupportedFileExtensions());
	}

	@Test
	void perform_returnsNullWhenNoGuidanceCommentPresent() throws IOException {
		// Arrange
		HtmlReviewer reviewer = new HtmlReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("index.html");
		Files.write(file, "<html></html>".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void perform_formatsHtmlWhenGuidanceCommentPresent() throws IOException {
		// Arrange
		HtmlReviewer reviewer = new HtmlReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("web").resolve("index.html");
		Files.createDirectories(file.getParent());
		String content = "<!-- @guidance: include -->\n<html><body>Hi</body></html>\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}
}
