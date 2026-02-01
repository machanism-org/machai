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

class TypeScriptReviewerTest {

	@TempDir
	File tempDir;

	@Test
	void getSupportedFileExtensions_returnsTs() {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();

		// Act
		String[] extensions = reviewer.getSupportedFileExtensions();

		// Assert
		assertEquals(1, extensions.length);
		assertEquals("ts", extensions[0]);
	}

	@Test
	void perform_returnsNull_whenNoGuidance() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		File tsFile = new File(tempDir, "a.ts");
		Files.writeString(tsFile.toPath(), "const a = 1;\n", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, tsFile);

		// Assert
		assertNull(result);
	}

	@Test
	void perform_extractsGuidance_fromLineComment() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		File tsFile = new File(tempDir, "a.ts");
		Files.writeString(tsFile.toPath(), "// @guidance: do it\nconst a = 1;\n", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, tsFile);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("do it"));
		assertEquals(true, result.contains("a.ts"));
	}

	@Test
	void perform_extractsGuidance_fromBlockComment() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		File tsFile = new File(tempDir, "a.ts");
		Files.writeString(tsFile.toPath(), "/* @guidance: block */\nconst a = 1;\n", StandardCharsets.UTF_8);

		// Act
		String result = reviewer.perform(tempDir, tsFile);

		// Assert
		assertNotNull(result);
		assertEquals(true, result.contains("block"));
	}
}
