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

class PythonReviewerTest {

	@TempDir
	Path tempDir;

	@Test
	void getSupportedFileExtensions_returnsPy() {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();

		// Act
		String[] result = reviewer.getSupportedFileExtensions();

		// Assert
		assertArrayEquals(new String[] { "py" }, result);
	}

	@Test
	void perform_returnsNullWhenNoGuidanceTag() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("a.py");
		Files.write(file, "print('hi')\n".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void perform_returnsNullWhenGuidanceTagPresentButNoSupportedSyntaxMatches() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("a.py");
		Files.write(file, ("x = '@guidance: not a comment'\n").getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void perform_formatsWhenGuidancePresentInLineComment() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("src").resolve("a.py");
		Files.createDirectories(file.getParent());
		String content = "# @guidance: keep\nprint('hi')\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}

	@Test
	void perform_formatsWhenGuidancePresentInTripleQuotedString() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("b.py");
		String content = "\"\"\" @guidance: doc \"\"\"\nprint('hi')\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}
}
