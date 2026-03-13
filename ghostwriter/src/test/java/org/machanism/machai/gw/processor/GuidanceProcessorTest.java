package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class GuidanceProcessorTest {

	@TempDir
	File tempDir;

	@Test
	void normalizeExtensionKey_shouldHandleNullBlankAndLeadingDot() {
		assertNull(GuidanceProcessor.normalizeExtensionKey(null));
		assertNull(GuidanceProcessor.normalizeExtensionKey("  "));
		assertEquals("java", GuidanceProcessor.normalizeExtensionKey("java"));
		assertEquals("java", GuidanceProcessor.normalizeExtensionKey(".java"));
		assertEquals("md", GuidanceProcessor.normalizeExtensionKey(".MD"));
	}

	@Test
	void parseFile_shouldReturnNullForDirectoriesAndUnsupportedExtensions() throws Exception {
		GuidanceProcessor processor = new GuidanceProcessor(tempDir, "model", new PropertiesConfigurator());

		File dir = new File(tempDir, "folder");
		assertTrue(dir.mkdirs());
		assertNull(processor.parseFile(tempDir, dir));

		File unknown = new File(tempDir, "file.unknown");
		assertTrue(unknown.createNewFile());
		assertNull(processor.parseFile(tempDir, unknown));
	}

	@Test
	void deleteTempFiles_shouldReturnFalseWhenNothingToDelete() {
		assertFalse(GuidanceProcessor.deleteTempFiles(tempDir));
	}

	@Test
	void match_whenNoPathMatcher_shouldMatchOnlyProjectDirIfDefaultPromptPresent() throws IOException {
		GuidanceProcessor processor = new GuidanceProcessor(tempDir, "model", new PropertiesConfigurator());
		processor.setDefaultPrompt("default");

		File projectDir = tempDir;
		File other = new File(tempDir, "a.txt");
		assertTrue(other.createNewFile());

		assertTrue(processor.match(projectDir, projectDir));
		assertFalse(processor.match(other, projectDir));
	}

	@Test
	void match_whenNoPathMatcher_shouldMatchAllWhenNoDefaultPrompt() throws IOException {
		GuidanceProcessor processor = new GuidanceProcessor(tempDir, "model", new PropertiesConfigurator());
		processor.setDefaultPrompt(null);

		File projectDir = tempDir;
		File other = new File(tempDir, "a.txt");
		assertTrue(other.createNewFile());

		assertTrue(processor.match(projectDir, projectDir));
		assertTrue(processor.match(other, projectDir));
	}

	@Test
	void getReviewerForExtension_shouldReturnReviewerForJavaWhenAvailable() {
		GuidanceProcessor processor = new GuidanceProcessor(tempDir, "model", new PropertiesConfigurator());
		assertNotNull(processor.getReviewerForExtension("java"));
		assertNotNull(processor.getReviewerForExtension(".java"));
		assertNull(processor.getReviewerForExtension(null));
	}
}
