package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReviewerCoverageEdgeCasesAdditionalTest {

	@TempDir
	Path tempDir;

	@Test
	void pythonReviewer_perform_returnsResultForTripleQuotedGuidance() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();
		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("sample.py");
		String content = "\"\"\" @guidance: doc \"\"\"\nprint('ok')\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		org.junit.jupiter.api.Assertions.assertNotNull(result);
	}

	@Test
	void pythonReviewer_perform_returnsNullWhenGuidanceTextResolvesToNull() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();
		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("empty.py");
		Files.write(file, "x='@guidance'\n".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void typeScriptReviewer_perform_returnsResultForBlockGuidanceComment() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("app.ts");
		String content = "/* @guidance: keep */\nexport const app = 1;\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		org.junit.jupiter.api.Assertions.assertNotNull(result);
	}

	@Test
	void javaReviewer_extractPackageName_returnsDefaultPackageForInvalidDeclaration() {
		// Arrange
		String content = "package 123.invalid;\nclass Sample {}";

		// Act
		String result = JavaReviewer.extractPackageName(content);

		// Assert
		assertEquals("<default package>", result);
	}
}
