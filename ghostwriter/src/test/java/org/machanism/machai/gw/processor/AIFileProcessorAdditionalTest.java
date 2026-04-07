package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.Genai;

class AIFileProcessorAdditionalTest {

	@TempDir
	Path tempDir;

	@Test
	void scanDocuments_whenProjectDirNull_thenThrows() {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		String scanDir = ".";

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.scanDocuments(null, scanDir));

		// Assert
		assertTrue(ex.getMessage().contains("projectDir"));
	}

	@Test
	void scanDocuments_whenScanDirBlank_thenThrows() {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		File projectDir = tempDir.toFile();
		String scanDir = "  ";

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.scanDocuments(projectDir, scanDir));

		// Assert
		assertTrue(ex.getMessage().contains("scanDir"));
	}

	@Test
	void parseScanDir_whenRootDirIsProjectDirAndDefaultPromptNull_thenAddsRecursiveGlobSuffix() {
		// Arrange
		File projectDir = tempDir.resolve("project").toFile();
		File scanDir = tempDir.resolve("project/src").toFile();
		assertTrue(scanDir.mkdirs());

		AIFileProcessor processor = new AIFileProcessor(projectDir, new PropertiesConfigurator(), "Any:Model");

		// Act
		String pattern = processor.parseScanDir(projectDir, "src");

		// Assert
		assertTrue(pattern.startsWith("glob:"));
		assertTrue(pattern.contains("src"), pattern);
		assertTrue(pattern.endsWith("{,/**}"), pattern);
	}

	@Test
	void parseScanDir_whenRootDirIsProjectDirAndDefaultPromptPresent_thenGlobDoesNotAddRecursiveSuffix() {
		// Arrange
		File projectDir = tempDir.resolve("project").toFile();
		File scanDir = tempDir.resolve("project/src").toFile();
		assertTrue(scanDir.mkdirs());

		AIFileProcessor processor = new AIFileProcessor(projectDir, new PropertiesConfigurator(), "Any:Model");
		processor.setDefaultPrompt("something");

		// Act
		String pattern = processor.parseScanDir(projectDir, "src");

		// Assert
		assertTrue(pattern.startsWith("glob:"));
		assertTrue(pattern.contains("src"), pattern);
		assertFalse(pattern.endsWith("{,/**}"), pattern);
	}

	@Test
	void readFromFilePath_whenMissing_thenThrowsIllegalArgumentExceptionWithPath() {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.readFromFilePath("does-not-exist.txt"));

		// Assert
		assertTrue(ex.getMessage().contains("Failed to read file:"));
		assertTrue(ex.getMessage().contains("does-not-exist.txt"));
		assertNotNull(ex.getCause());
	}

	@Test
	void parseLines_whenNull_thenReturnsEmptyString() {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act
		String out = processor.parseLines(null);

		// Assert
		assertEquals("", out);
	}

	@Test
	void parseLines_whenFileReference_thenIncludesFileContentsAndPreservesNewline() throws IOException {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		Path f = tempDir.resolve("instructions.txt");
		Files.write(f, "hello\nworld".getBytes(StandardCharsets.UTF_8));

		String input = "file:" + f.toAbsolutePath();

		// Act
		String out = processor.parseLines(input);

		// Assert
		assertTrue(out.contains("hello"));
		assertTrue(out.contains("world"));
		assertTrue(out.endsWith(Genai.LINE_SEPARATOR), "parseLines should append newline after each input line");
	}
}
