package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

class AIFileProcessorCoverageTest {

	@TempDir
	Path tempDir;

	@Test
	void parseScanDir_whenDot_usesProjectDirAndDefaultPromptNull_addsRecursiveGlob() {
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");
		processor.setDefaultPrompt(null);

		String pattern = processor.parseScanDir(projectDir, ".");

		assertTrue(pattern.startsWith("glob:"), "Expected glob pattern");
		assertTrue(pattern.endsWith("{,/**}"), "Expected recursive glob when defaultPrompt is null");
		assertEquals(projectDir.getAbsolutePath(), processor.getScanDir().getAbsolutePath(),
				"scanDir should be project root when '.' is provided");
	}

	@Test
	void parseScanDir_whenAbsoluteOutsideProject_throwsIllegalArgumentException() {
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");
		File outside = new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile();
		if (outside.getAbsolutePath().startsWith(projectDir.getAbsolutePath())) {
			outside = new File(outside, "outside-" + System.nanoTime());
		}
		final String outsidePath = outside.getAbsolutePath();

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.parseScanDir(projectDir, outsidePath));
		assertTrue(ex.getMessage().contains("must be located within"));
	}

	@Test
	void scanDocuments_whenProjectDirIsNull_throwsIllegalArgumentException() {
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), configurator, "Any:Model");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.scanDocuments(null, "src"));
		assertEquals("projectDir must not be null", ex.getMessage());
	}

	@Test
	void scanDocuments_whenScanDirIsBlank_throwsIllegalArgumentException() {
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), configurator, "Any:Model");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.scanDocuments(tempDir.toFile(), "  "));
		assertEquals("scanDir must not be blank", ex.getMessage());
	}

	@Test
	void scanDocuments_whenScanDirEqualsProjectDirAbsolute_setsScanDirToProjectDir() throws Exception {
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();

		AIFileProcessor noScan = new AIFileProcessor(projectDir, configurator, "Any:Model") {
			@Override
			public void scanFolder(File projectDir) {
				// no-op
			}
		};

		noScan.scanDocuments(projectDir, projectDir.getAbsolutePath());

		assertEquals(projectDir.getAbsolutePath(), noScan.getScanDir().getAbsolutePath());
		assertNull(noScan.getPathMatcher(), "PathMatcher should remain null when scanDir equals projectDir");
	}

	@Test
	void getDirInfoLine_whenAllMissingOrNull_returnsNotDefined() {
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");

		String line = processor.getDirInfoLine(java.util.Arrays.asList(null, "missing-dir"), projectDir);

		assertEquals(AIFileProcessor.NOT_DEFINED_VALUE, line);
	}

	@Test
	void getProjectStructureDescription_whenNullNames_usesNotDefinedAndIncludesRelativeFile() throws IOException {
		File projectDir = tempDir.toFile();
		Files.createDirectories(tempDir.resolve("src/main/java"));
		Path filePath = tempDir.resolve("src/main/java/X.java");
		Files.write(filePath, "class X {}".getBytes(StandardCharsets.UTF_8));

		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");

		ProjectLayout layout = new DefaultProjectLayout().projectDir(projectDir);

		String info = processor.getProjectStructureDescription(layout, filePath.toFile());

		assertNotNull(info);
		assertTrue(info.contains(AIFileProcessor.NOT_DEFINED_VALUE), "Expected 'not defined' placeholder to appear");
		assertTrue(info.contains("src\\main\\java\\X.java") || info.contains("src/main/java/X.java"),
				"Expected relative file path to be included");
	}

	@Test
	void processFolder_wrapsExceptionIntoIllegalArgumentException() {
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();

		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model") {
			@Override
			public String process(ProjectLayout projectLayout, File file, String instructions, String prompt) {
				throw new IllegalArgumentException(new IOException("boom"));
			}
		};
		processor.setDefaultPrompt("x");

		ProjectLayout layout = new DefaultProjectLayout().projectDir(projectDir);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.processFolder(layout));
		assertTrue(ex.getCause() instanceof IllegalArgumentException);
		assertTrue(ex.getCause().getCause() instanceof IOException);
	}

	@Test
	void readFromFilePath_whenMissing_throwsIllegalArgumentExceptionWithPathInMessage() {
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.readFromFilePath("does-not-exist.txt"));
		assertTrue(ex.getMessage().contains("Failed to read file:"));
		assertTrue(ex.getMessage().contains(projectDir.getAbsolutePath()));
	}

	@Test
	void tryToGetInstructionsFromReference_whenNull_returnsNull_viaReflection() throws Exception {
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		Method m = AIFileProcessor.class.getDeclaredMethod("tryToGetInstructionsFromReference", String.class);
		m.setAccessible(true);

		Object result = invokeTryToGetInstructionsFromReference(m, processor, null);

		assertNull(result);
	}

	@Test
	void tryToGetInstructionsFromReference_whenFilePrefix_readsAndParses_viaReflection() throws Exception {
		Path p = tempDir.resolve("i.txt");
		Files.write(p, "a\n\n b".getBytes(StandardCharsets.UTF_8));

		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		Method m = AIFileProcessor.class.getDeclaredMethod("tryToGetInstructionsFromReference", String.class);
		m.setAccessible(true);

		String result = (String) invokeTryToGetInstructionsFromReference(m, processor, "file:" + p.toAbsolutePath());

		assertNotNull(result);
		assertTrue(result.contains("a"));
		assertTrue(result.contains("b"));
		assertTrue(result.contains("\n\n"), "Expected blank line to be preserved by parseLines");
	}

	@Test
	void tryToGetInstructionsFromReference_whenHttpPrefix_withInvalidUrl_throwsInvocationCauseIOException() throws Exception {
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		Method m = AIFileProcessor.class.getDeclaredMethod("tryToGetInstructionsFromReference", String.class);
		m.setAccessible(true);
		String reference = "http://localhost:0/does-not-exist";

		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				invokeTryToGetInstructionsFromReferenceExecutable(m, processor, reference));

		assertTrue(ex.getCause() instanceof IOException, "Expected IOException from URL read");
	}

	// Sonar java:S5778 - ensure the lambda contains only a single call that may throw.
	private static org.junit.jupiter.api.function.Executable invokeTryToGetInstructionsFromReferenceExecutable(Method m,
			AIFileProcessor processor, String reference) {
		return () -> {
			try {
				invokeTryToGetInstructionsFromReference(m, processor, reference);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		};
	}

	private static Object invokeTryToGetInstructionsFromReference(Method m, AIFileProcessor processor, String reference)
			throws IllegalAccessException, InvocationTargetException {
		return m.invoke(processor, reference);
	}
}
