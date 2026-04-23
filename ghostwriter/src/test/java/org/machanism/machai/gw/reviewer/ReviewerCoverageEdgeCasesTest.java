package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.gw.processor.ActNotFound;

class ReviewerCoverageEdgeCasesTest {

	@TempDir
	Path tempDir;

	@Test
	void pythonReviewer_perform_returnsNullWhenGuidanceTagHasNoTrailingContent() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();
		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);
		Path pythonFile = projectDir.resolve("empty-guidance.py");
		Files.write(pythonFile, "# @guidance\nprint('x')\n".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(projectDir.toFile(), pythonFile.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void typeScriptReviewer_perform_returnsNullWhenGuidanceTagHasNoTrailingContent() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);
		Path tsFile = projectDir.resolve("empty-guidance.ts");
		Files.write(tsFile, "// @guidance\nconst x = 1;\n".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(projectDir.toFile(), tsFile.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void actNotFound_exposesNameAndMessage() {
		// Arrange
		ActNotFound exception = new ActNotFound("review-docs");

		// Act
		exception.setName("other-act");

		// Assert
		assertEquals("other-act", exception.getName());
		assertEquals("Act: `review-docs` not found.", exception.getMessage());
	}
}
