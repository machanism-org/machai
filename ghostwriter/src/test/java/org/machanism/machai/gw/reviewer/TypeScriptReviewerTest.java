package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TypeScriptReviewerTest {

	@TempDir
	Path tempDir;

	@Test
	void getSupportedFileExtensions_returnsTs() {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();

		// Act
		String[] result = reviewer.getSupportedFileExtensions();

		// Assert
		assertArrayEquals(new String[] { "ts" }, result);
	}

	@Test
	void perform_returnsNullWhenNoGuidanceTag() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);

		Path file = projectDir.resolve("a.ts");
		Files.write(file, "const x = 1;\n".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(projectDir.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void perform_returnsNonNullWhenGuidancePresentInLineComment() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);

		Path file = projectDir.resolve("src").resolve("a.ts");
		Files.createDirectories(file.getParent());
		String content = "// @guidance: keep\nconst x = 1;\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(projectDir.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}

	@Test
	void perform_returnsNonNullWhenGuidancePresentInBlockComment() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);

		Path file = projectDir.resolve("b.ts");
		String content = "/* @guidance: keep */\nconst x = 1;\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(projectDir.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}

	@Test
	void perform_returnsNonNullWhenGuidancePresentMultipleTimes() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);

		Path file = projectDir.resolve("c.ts");
		String content = String.join("\n",
				"// @guidance: first",
				"const x = 1;",
				"// @guidance: second",
				"const y = 2;",
				"");
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(projectDir.toFile(), file.toFile());

		// Assert
		assertNotNull(result);
	}

	@Test
	void perform_throwsWhenFileDoesNotExist() {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		Path projectDir = tempDir.resolve("project");
		File missing = projectDir.resolve("missing.ts").toFile();

		// Act + Assert
		assertThrows(IOException.class, () -> reviewer.perform(projectDir.toFile(), missing));
	}
}
