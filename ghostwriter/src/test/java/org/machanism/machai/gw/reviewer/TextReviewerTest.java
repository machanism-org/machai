package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextReviewerTest {

	@TempDir
	Path tempDir;

	@Test
	void getSupportedFileExtensions_returnsTxt() {
		// Arrange
		TextReviewer reviewer = new TextReviewer();

		// Act
		String[] result = reviewer.getSupportedFileExtensions();

		// Assert
		assertArrayEquals(new String[] { "txt" }, result);
	}

	@Test
	void perform_returnsNullWhenNotGuidanceFileName() throws IOException {
		// Arrange
		TextReviewer reviewer = new TextReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("notes.txt");
		Files.write(file, "hello".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void perform_formatsGuidanceFileWhenNamedGuidanceTxt() throws IOException {
		// Arrange
		TextReviewer reviewer = new TextReviewer();

		Path project = tempDir.resolve("project");
		Path folder = project.resolve("src").resolve("test");
		Files.createDirectories(folder);
		Path file = folder.resolve("@guidance.txt");
		Files.write(file, "Line1\nLine2".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
		assertTrue(result.contains("src/test"));
		assertTrue(result.contains("Line1"));
		assertTrue(result.contains("Line2"));
	}

	@Test
	void getPrompt_returnsSameReferenceWhenBlank() {
		// Arrange
		TextReviewer reviewer = new TextReviewer();
		String blank = "  \n\t";

		// Act
		String result = reviewer.getPrompt(tempDir.toFile(), tempDir.toFile(), blank);

		// Assert
		assertSame(blank, result);
	}

	@Test
	void getPrompt_returnsFormattedPromptWhenNonBlank() {
		// Arrange
		TextReviewer reviewer = new TextReviewer();
		String guidance = "Hello";

		// Act
		String result = reviewer.getPrompt(tempDir.toFile(), tempDir.resolve("child").toFile(), guidance);

		// Assert
		assertNotNull(result);
		assertTrue(result.contains("Hello"));
	}
}
