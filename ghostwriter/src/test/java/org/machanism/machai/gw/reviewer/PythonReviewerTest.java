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
		PythonReviewer reviewer = new PythonReviewer();
		assertArrayEquals(new String[] { "py" }, reviewer.getSupportedFileExtensions());
	}

	@Test
	void perform_returnsNullWhenNoGuidanceTag() throws IOException {
		PythonReviewer reviewer = new PythonReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("a.py");
		Files.write(file, "print('hi')\n".getBytes(StandardCharsets.UTF_8));

		assertNull(reviewer.perform(project.toFile(), file.toFile()));
	}

	@Test
	void perform_formatsWhenGuidancePresentInLineComment() throws IOException {
		PythonReviewer reviewer = new PythonReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("src").resolve("a.py");
		Files.createDirectories(file.getParent());
		String content = "# @guidance: keep\nprint('hi')\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		String result = reviewer.perform(project.toFile(), file.toFile());
		assertNotNull(result);
	}

	@Test
	void perform_formatsWhenGuidancePresentInTripleQuotedString() throws IOException {
		PythonReviewer reviewer = new PythonReviewer();

		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("b.py");
		String content = "\"\"\" @guidance: doc \"\"\"\nprint('hi')\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		String result = reviewer.perform(project.toFile(), file.toFile());
		assertNotNull(result);
	}
}
