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

class MarkdownReviewerTest {

	@TempDir
	Path tempDir;

	@Test
	void getSupportedFileExtensions_returnsMd() {
		// Arrange
		MarkdownReviewer reviewer = new MarkdownReviewer();

		// Act
		String[] result = reviewer.getSupportedFileExtensions();

		// Assert
		assertArrayEquals(new String[] { "md" }, result);
	}

	@Test
	void perform_returnsNullWhenNoGuidanceTag() throws IOException {
		// Arrange
		MarkdownReviewer reviewer = new MarkdownReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("README.md");
		Files.write(file, "# Title".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void perform_formatsMarkdownWhenGuidanceCommentPresent() throws IOException {
		// Arrange
		MarkdownReviewer reviewer = new MarkdownReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("docs").resolve("guide.md");
		Files.createDirectories(file.getParent());
		String content = "<!-- @guidance: explain -->\n# Guide\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}

	@Test
	void perform_formatsMarkdownWhenCommentIsNotClosedAtEndOfFile() throws IOException {
		// Arrange
		MarkdownReviewer reviewer = new MarkdownReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("docs").resolve("guide.md");
		Files.createDirectories(file.getParent());
		String content = "<!-- @guidance: explain\n# Guide\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}
}
