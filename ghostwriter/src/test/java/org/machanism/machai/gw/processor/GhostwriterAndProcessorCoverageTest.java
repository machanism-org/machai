package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.project.layout.ProjectLayout;

class GhostwriterAndProcessorCoverageTest {

	@TempDir
	File tempDir;

	@Test
	void ghostwriterPerform_returnsExitCodeFromProcessTerminationException() throws Exception {
		AIFileProcessor processor = mock(AIFileProcessor.class);
		when(processor.getProjectDir()).thenReturn(tempDir);
		doThrow(new ProcessTerminationException("stop", 7)).when(processor).scanDocuments(tempDir, ".");
		Ghostwriter ghostwriter = new Ghostwriter("provider:model", processor);
		initializeLogger();

		int result = ghostwriter.perform(new String[] { "." });

		assertEquals(7, result);
	}

	@Test
	void ghostwriterPerform_returnsOneOnIllegalArgumentException() throws Exception {
		AIFileProcessor processor = mock(AIFileProcessor.class);
		when(processor.getProjectDir()).thenReturn(tempDir);
		doThrow(new IllegalArgumentException("bad")).when(processor).scanDocuments(tempDir, ".");
		Ghostwriter ghostwriter = new Ghostwriter("provider:model", processor);
		initializeLogger();

		int result = ghostwriter.perform(new String[] { "." });

		assertEquals(1, result);
	}

	@Test
	void readText_supportsMultilineContinuation() throws Exception {
		Field scannerField = Ghostwriter.class.getDeclaredField("scanner");
		scannerField.setAccessible(true);
		Scanner original = (Scanner) scannerField.get(null);
		Scanner replacement = new Scanner(
				new ByteArrayInputStream("line1\\\nline2\n".getBytes(StandardCharsets.UTF_8)));
		scannerField.set(null, replacement);

		try {
			String result = Ghostwriter.readText("Prompt");
			assertEquals("line1" + org.machanism.machai.ai.provider.Genai.LINE_SEPARATOR + "line2", result);
		} finally {
			scannerField.set(null, original);
		}
	}

	@Test
	void resolveActPrompt_readsFromConsoleWhenOptionWithoutValue() throws Exception {
		Field scannerField = Ghostwriter.class.getDeclaredField("scanner");
		scannerField.setAccessible(true);
		Scanner original = (Scanner) scannerField.get(null);
		scannerField.set(null,
				new Scanner(new ByteArrayInputStream("chosen-act\n".getBytes(StandardCharsets.UTF_8))));
		Options options = new Options();
		Option act = Option.builder("a").longOpt("act").hasArg(true).optionalArg(true).build();
		options.addOption(act);
		CommandLine cmd = new DefaultParser().parse(options, new String[] { "-a" });
		PropertiesConfigurator config = new PropertiesConfigurator();

		try {
			String result = Ghostwriter.resolveActPrompt(cmd, config);
			assertEquals("chosen-act", result);
		} finally {
			scannerField.set(null, original);
		}
	}

	@Test
	void aiFileProcessor_parseLines_returnsEmptyForNull() {
		AIFileProcessor processor = new AIFileProcessor(tempDir, new PropertiesConfigurator(), "provider:model");

		String result = processor.parseLines(null);

		assertEquals("", result);
	}

	@Test
	void aiFileProcessor_tryToGetInstructionsFromReference_returnsNullForNull() throws Exception {
		AIFileProcessor processor = new AIFileProcessor(tempDir, new PropertiesConfigurator(), "provider:model");

		String result = processor.tryToGetInstructionsFromReference(null);

		assertNull(result);
	}

	@Test
	void aiFileProcessor_readFromFilePath_readsRelativePath() throws Exception {
		File source = new File(tempDir, "instruction.txt");
		Files.write(source.toPath(), "instruction".getBytes(StandardCharsets.UTF_8));
		AIFileProcessor processor = new AIFileProcessor(tempDir, new PropertiesConfigurator(), "provider:model");

		String result = processor.readFromFilePath("instruction.txt");

		assertEquals("instruction", result);
	}

	@Test
	void aiFileProcessor_readFromFilePath_wrapsIoErrors() {
		AIFileProcessor processor = new AIFileProcessor(tempDir, new PropertiesConfigurator(), "provider:model");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.readFromFilePath("missing.txt"));

		assertTrue(ex.getMessage().contains("Failed to read file"));
	}

	@Test
	void aiFileProcessor_parseScanDir_usesProjectDirForDotAndDefaultPromptChangesPattern() {
		AIFileProcessor processor = new AIFileProcessor(tempDir, new PropertiesConfigurator(), "provider:model");
		processor.setDefaultPrompt(null);

		String withoutPrompt = processor.parseScanDir(tempDir, ".");
		processor.setDefaultPrompt("default");
		String withPrompt = processor.parseScanDir(tempDir, "src");

		assertEquals("glob:.{,/**}", withoutPrompt);
		assertEquals("glob:src", withPrompt);
	}

	@Test
	void aiFileProcessor_processFolder_wrapsFailures() {
		AIFileProcessor processor = new AIFileProcessor(tempDir, new PropertiesConfigurator(), "provider:model") {
			@Override
			public String process(ProjectLayout projectLayout, File file, String instructions, String prompt) {
				throw new IllegalStateException("boom");
			}
		};
		ProjectLayout layout = mock(ProjectLayout.class);
		when(layout.getProjectDir()).thenReturn(tempDir);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.processFolder(layout));

		assertEquals(IllegalStateException.class, ex.getCause().getClass());
	}

	@Test
	void aiFileProcessor_addTool_storesFunctionTool() throws Exception {
		AIFileProcessor processor = new AIFileProcessor(tempDir, new PropertiesConfigurator(), "provider:model");
		org.machanism.machai.ai.tools.FunctionTools tool = provider -> {
			// Sonar java:S1186 - empty lambda body is intentional for storage verification only.
		};

		processor.addTool(tool);

		Field field = AIFileProcessor.class.getDeclaredField("toolFunctions");
		field.setAccessible(true);
		List<?> tools = (List<?>) field.get(processor);
		assertEquals(1, tools.size());
	}

	@Test
	void guidanceProcessor_match_withoutMatcher_returnsTrueOnlyForProjectDirOrDefaultPromptMissing() {
		GuidanceProcessor processor = new GuidanceProcessor(tempDir, "provider:model", new PropertiesConfigurator());
		File child = new File(tempDir, "child.txt");

		boolean childWithoutPrompt = processor.match(child, tempDir);
		processor.setDefaultPrompt("default");
		boolean childWithPrompt = processor.match(child, tempDir);
		boolean projectDirMatch = processor.match(tempDir, tempDir);

		assertTrue(childWithoutPrompt);
		assertFalse(childWithPrompt);
		assertTrue(projectDirMatch);
	}

	@Test
	void guidanceProcessor_parseFile_returnsNullForDirectoryAndUnsupportedExtension() throws Exception {
		GuidanceProcessor processor = new GuidanceProcessor(tempDir, "provider:model", new PropertiesConfigurator());
		File unsupported = new File(tempDir, "file.unknown");
		Files.write(unsupported.toPath(), "x".getBytes(StandardCharsets.UTF_8));

		String dirResult = processor.parseFile(tempDir, tempDir);
		String unsupportedResult = processor.parseFile(tempDir, unsupported);

		assertNull(dirResult);
		assertNull(unsupportedResult);
	}

	@Test
	void abstractProcessor_matchPath_supportsDirectScanDirEqualityWhenMatcherMissing() {
		TestAbstractFileProcessor processor = new TestAbstractFileProcessor(tempDir);
		File scanDir = new File(tempDir, "scan");
		processor.setScanDir(scanDir);

		boolean result = processor.matchPath(tempDir, scanDir, "", "scan");

		assertTrue(result);
	}

	@Test
	void abstractProcessor_shouldExcludePath_supportsExactFileNameAndPattern() {
		TestAbstractFileProcessor processor = new TestAbstractFileProcessor(tempDir);
		processor.setExcludes(new String[] { "secret.txt", "glob:**/*.log" });

		boolean exact = processor.shouldExcludePath(new File("a/secret.txt").toPath());
		boolean pattern = processor.shouldExcludePath(new File("a/b/test.log").toPath());
		boolean other = processor.shouldExcludePath(new File("a/b/test.txt").toPath());

		assertTrue(exact);
		assertTrue(pattern);
		assertFalse(other);
	}

	@Test
	void abstractProcessor_getPatternPath_returnsMatcherOnlyForPatterns() {
		PathMatcher matcher = TestAbstractFileProcessor.callGetPatternPath("glob:**/*.java");
		PathMatcher none = TestAbstractFileProcessor.callGetPatternPath("plain/path");

		assertNotNull(matcher);
		assertNull(none);
	}

	@Test
	void abstractProcessor_processProjectDir_wrapsIoException() {
		TestAbstractFileProcessor processor = new TestAbstractFileProcessor(tempDir) {
			@Override
			List<File> findFiles(File projectDir, String pattern) throws IOException {
				throw new IOException("boom");
			}
		};
		ProjectLayout layout = mock(ProjectLayout.class);
		when(layout.getProjectDir()).thenReturn(tempDir);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.processProjectDir(layout, "."));

		assertEquals(IOException.class, ex.getCause().getClass());
	}

	private void initializeLogger() throws Exception {
		Method method = Ghostwriter.class.getDeclaredMethod("initializeConfiguration", File.class);
		method.setAccessible(true);
		method.invoke(null, tempDir);
	}

	private static class TestAbstractFileProcessor extends AbstractFileProcessor {

		protected TestAbstractFileProcessor(File projectDir) {
			super(projectDir, new PropertiesConfigurator());
		}

		static PathMatcher callGetPatternPath(String value) {
			return getPatternPath(value);
		}

		@Override
		protected void processParentFiles(ProjectLayout projectLayout) {
			// Sonar java:S1186 - no parent-file processing is required for this test stub.
		}

		@Override
		protected void processFile(ProjectLayout projectLayout, File file) {
			// Sonar java:S1186 - no file processing is required for this test stub.
		}
	}
}
