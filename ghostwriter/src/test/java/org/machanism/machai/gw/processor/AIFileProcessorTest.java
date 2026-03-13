package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

class AIFileProcessorTest {

	@TempDir
	File tempDir;

	private TestAIFileProcessor processor;
	private ProjectLayout layout;

	@BeforeEach
	void setUp() {
		processor = new TestAIFileProcessor(tempDir, new PropertiesConfigurator(), "test-provider");
		layout = new DefaultProjectLayout().projectDir(tempDir);
	}

	@Test
	void parseLines_shouldPreserveBlankLinesAndAppendTrailingNewline() {
		String input = "line1\n\n line2 \n";

		String out = processor.parseLines(input);

		assertEquals("line1\n\nline2\n", out);
	}

	@Test
	void tryToGetInstructionsFromReference_shouldReturnNullForNull() throws Exception {
		assertNull(processor.tryToGetInstructionsFromReference(null));
	}

	@Test
	void tryToGetInstructionsFromReference_shouldReturnDataWhenNotReference() throws Exception {
		assertEquals("hello", processor.tryToGetInstructionsFromReference("hello"));
		assertEquals("  hello  ", processor.tryToGetInstructionsFromReference("  hello  "));
	}

	@Test
	void readFromFilePath_shouldResolveRelativeToRootDir() throws IOException {
		File data = new File(tempDir, "instructions.txt");
		Files.write(data.toPath(), Arrays.asList("a", "file"), StandardCharsets.UTF_8);

		String out = processor.readFromFilePath("instructions.txt");
		assertTrue(out.contains("a"));
		assertTrue(out.contains("file"));
	}

	@Test
	void readFromFilePath_shouldThrowIllegalArgumentExceptionWhenMissing() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.readFromFilePath("missing.txt"));
		assertTrue(ex.getMessage().contains("Failed to read file:"));
	}

	@Test
	void parseScanDir_shouldCreateGlobPatternAndValidatePathInsideProject() {
		File projectDir = tempDir;

		processor.setDefaultPrompt(null);
		String glob = processor.parseScanDir(projectDir, "src");
		assertTrue(glob.startsWith("glob:"));
		assertTrue(glob.contains("src"));
		assertNotNull(processor.getScanDir());

		processor.setDefaultPrompt("prompt");
		String glob2 = processor.parseScanDir(projectDir, "src");
		assertTrue(glob2.startsWith("glob:"));
		assertFalse(glob2.contains("{,/**}"), "when default prompt is set, pattern should not auto-include children");
	}

	@Test
	void parseScanDir_shouldThrowWhenScanDirNotInsideProject() {
		File projectDir = new File(tempDir, "project");
		assertTrue(projectDir.mkdirs());

		File outside = new File(tempDir.getParentFile(), "outside");
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.parseScanDir(projectDir, outside.getPath()));
		assertTrue(ex.getMessage().contains("must be located within the root project directory"));
	}

	@Test
	void getDirInfoLine_shouldReturnNotDefinedWhenNoExistingEntries() {
		String line = processor.getDirInfoLine(Collections.singletonList("does-not-exist"), tempDir);
		assertEquals(AIFileProcessor.NOT_DEFINED, line);
	}

	@Test
	void getDirInfoLine_shouldIncludeOnlyExistingDirsAndFormatWithBackticks() {
		File src = new File(tempDir, "src/main/java");
		assertTrue(src.mkdirs());

		String line = processor.getDirInfoLine(Arrays.asList("src/main/java", "missing"), tempDir);
		assertEquals("`src/main/java`", line);
	}

	@Test
	void getProjectStructureDescription_shouldIncludeRelativePathsAndFile() throws IOException {
		File f = new File(tempDir, "src/main/java/App.java");
		assertTrue(f.getParentFile().mkdirs());
		assertTrue(f.createNewFile());

		String desc = processor.getProjectStructureDescription(layout, f);
		assertTrue(desc.contains(tempDir.getName()));
		assertTrue(desc.contains("src/main/java"));
		assertTrue(desc.contains("App.java"));
	}

	private static final class TestAIFileProcessor extends AIFileProcessor {
		TestAIFileProcessor(File rootDir, PropertiesConfigurator configurator, String genai) {
			super(rootDir, configurator, genai);
		}

		@Override
		public ProjectLayout getProjectLayout(File projectDir) {
			return new DefaultProjectLayout().projectDir(projectDir);
		}
	}
}
