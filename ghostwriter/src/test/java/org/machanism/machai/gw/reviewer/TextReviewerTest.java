package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
		TextReviewer reviewer = new TextReviewer();
		assertArrayEquals(new String[] { "txt" }, reviewer.getSupportedFileExtensions());
	}

	@Test
	void perform_returnsNullWhenNotGuidanceFileName() throws IOException {
		TextReviewer reviewer = new TextReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("notes.txt");
		Files.write(file, "hello".getBytes(StandardCharsets.UTF_8));

		assertNull(reviewer.perform(project.toFile(), file.toFile()));
	}

	@Test
	void perform_formatsGuidanceFileWhenNamedGuidanceTxt() throws IOException {
		TextReviewer reviewer = new TextReviewer();

		Path project = tempDir.resolve("project");
		Path folder = project.resolve("src").resolve("test");
		Files.createDirectories(folder);
		Path file = folder.resolve("@guidance.txt");
		Files.write(file, "Line1\nLine2".getBytes(StandardCharsets.UTF_8));

		String result = reviewer.perform(project.toFile(), file.toFile());
		assertNotNull(result);
		assertEquals(true, result.contains("src/test"));
		assertEquals(true, result.contains("Line1"));
	}

	@Test
	void getPrompt_returnsOriginalWhenBlank() {
		TextReviewer reviewer = new TextReviewer();
		String result = reviewer.getPrompt(tempDir.toFile(), tempDir.toFile(), "  \n\t");
		assertEquals("  \n\t", result);
	}
}
