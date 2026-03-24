package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

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

/**
 * Additional tests focused on previously uncovered branches/paths in
 * {@link AIFileProcessor}.
 */
class AIFileProcessorCoverageTest {

	@TempDir
	Path tempDir;

	@Test
	void parseScanDir_whenDot_usesProjectDirAndDefaultPromptNull_addsRecursiveGlob() {
		// Arrange
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");
		processor.setDefaultPrompt(null);

		// Act
		String pattern = processor.parseScanDir(projectDir, ".");

		// Assert
		assertTrue(pattern.startsWith("glob:"), "Expected glob pattern");
		assertTrue(pattern.endsWith("{,/**}"), "Expected recursive glob when defaultPrompt is null");
		assertEquals(projectDir.getAbsolutePath(), processor.getScanDir().getAbsolutePath(),
				"scanDir should be project root when '.' is provided");
	}

	@Test
	void parseScanDir_whenAbsoluteOutsideProject_throwsIllegalArgumentException() {
		// Arrange
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");
		File outside = new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile();
		if (outside.getAbsolutePath().startsWith(projectDir.getAbsolutePath())) {
			outside = new File(outside, "outside-" + System.nanoTime());
		}
		final String outsidePath = outside.getAbsolutePath();

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.parseScanDir(projectDir, outsidePath));
		assertTrue(ex.getMessage().contains("must be located within"));
	}

	@Test
	void scanDocuments_whenProjectDirIsNull_throwsIllegalArgumentException() {
		// Arrange
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), configurator, "Any:Model");

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.scanDocuments(null, "src"));
		assertEquals("projectDir must not be null", ex.getMessage());
	}

	@Test
	void scanDocuments_whenScanDirIsBlank_throwsIllegalArgumentException() {
		// Arrange
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), configurator, "Any:Model");

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> processor.scanDocuments(tempDir.toFile(), "  "));
		assertEquals("scanDir must not be blank", ex.getMessage());
	}

	@Test
	void scanDocuments_whenScanDirEqualsProjectDirAbsolute_setsScanDirToProjectDir() throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();

		// Avoid heavy scanning by overriding scanFolder to no-op.
		AIFileProcessor noScan = new AIFileProcessor(projectDir, configurator, "Any:Model") {
			@Override
			public void scanFolder(File projectDir) {
				// no-op
			}
		};

		// Act
		noScan.scanDocuments(projectDir, projectDir.getAbsolutePath());

		// Assert
		assertEquals(projectDir.getAbsolutePath(), noScan.getScanDir().getAbsolutePath());
		assertNull(noScan.getPathMatcher(), "PathMatcher should remain null when scanDir equals projectDir");
	}

	@Test
	void getDirInfoLine_whenAllMissingOrNull_returnsNotDefined() {
		// Arrange
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");

		// Act
		String line = processor.getDirInfoLine(java.util.Arrays.asList(null, "missing-dir"), projectDir);

		// Assert
		assertEquals(AIFileProcessor.NOT_DEFINED, line);
	}

	@Test
	void getProjectStructureDescription_whenNullNames_usesNotDefinedAndIncludesRelativeFile() throws IOException {
		// Arrange
		File projectDir = tempDir.toFile();
		Files.createDirectories(tempDir.resolve("src/main/java"));
		Path filePath = tempDir.resolve("src/main/java/X.java");
		Files.write(filePath, "class X {}".getBytes(StandardCharsets.UTF_8));

		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");

		ProjectLayout layout = new DefaultProjectLayout().projectDir(projectDir);

		// Act
		String info = processor.getProjectStructureDescription(layout, filePath.toFile());

		// Assert
		assertNotNull(info);
		assertTrue(info.contains("not defined"), "Expected 'not defined' placeholder to appear");
		assertTrue(info.contains("src\\main\\java\\X.java") || info.contains("src/main/java/X.java"),
				"Expected relative file path to be included");
	}

	@Test
	void processFolder_wrapsIOExceptionIntoIllegalArgumentException() {
		// Arrange
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();

		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model") {
			@Override
			public String process(ProjectLayout projectLayout, File file, String instructions, String prompt) throws IOException {
				throw new IOException("boom");
			}
		};
		processor.setDefaultPrompt("x");

		ProjectLayout layout = new DefaultProjectLayout().projectDir(projectDir);

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.processFolder(layout));
		assertTrue(ex.getCause() instanceof IOException);
	}

	@Test
	void readFromFilePath_whenMissing_throwsIllegalArgumentExceptionWithPathInMessage() {
		// Arrange
		File projectDir = tempDir.toFile();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "Any:Model");

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.readFromFilePath("does-not-exist.txt"));
		assertTrue(ex.getMessage().contains("Failed to read file:"));
		assertTrue(ex.getMessage().contains(projectDir.getAbsolutePath()));
	}

	@Test
	void tryToGetInstructionsFromReference_whenNull_returnsNull_viaReflection() throws Exception {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		Method m = AIFileProcessor.class.getDeclaredMethod("tryToGetInstructionsFromReference", String.class);
		m.setAccessible(true);

		// Act
		Object result = m.invoke(processor, new Object[] { null });

		// Assert
		assertNull(result);
	}

	@Test
	void tryToGetInstructionsFromReference_whenFilePrefix_readsAndParses_viaReflection() throws Exception {
		// Arrange
		Path p = tempDir.resolve("i.txt");
		Files.write(p, "a\n\n b".getBytes(StandardCharsets.UTF_8));

		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		Method m = AIFileProcessor.class.getDeclaredMethod("tryToGetInstructionsFromReference", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(processor, "file:" + p.toAbsolutePath());

		// Assert
		assertNotNull(result);
		assertTrue(result.contains("a"));
		assertTrue(result.contains("b"));
		assertTrue(result.contains("\n\n"), "Expected blank line to be preserved by parseLines");
	}

	@Test
	void tryToGetInstructionsFromReference_whenHttpPrefix_withInvalidUrl_throwsInvocationCauseIOException() throws Exception {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		Method m = AIFileProcessor.class.getDeclaredMethod("tryToGetInstructionsFromReference", String.class);
		m.setAccessible(true);

		// Act + Assert
		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> m.invoke(processor, "http://localhost:0/does-not-exist"));
		assertTrue(ex.getCause() instanceof IOException, "Expected IOException from URL read");
	}
}
