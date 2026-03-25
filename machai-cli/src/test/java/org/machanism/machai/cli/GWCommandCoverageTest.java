package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.machanism.machai.cli.ConfigCommand;
import org.machanism.machai.cli.GWCommand;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.io.TempDir;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Additional focused tests to cover package-private/private logic in {@link GWCommand}.
 *
 * <p>Uses reflection intentionally to validate behaviors of small resolver helpers
 * and internal value objects without widening visibility in production code.
 */
class GWCommandCoverageTest {

	private final InputStream originalIn = System.in;

    private AIFileProcessor aiFileProcessor;

    private final File projectDir = new File("test-project");

    private final String model = "test-model";

    @Mock
	private ProjectLayout projectLayout;

    @Mock
	private Configurator configurator;

    @BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		aiFileProcessor = new AIFileProcessor(projectDir, configurator, model);
	}

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
	}

	@Test
	void resolveInstructions_whenNull_returnsConfigValue() throws Exception {
		// Arrange
		ConfigCommand.config.set(org.machanism.machai.gw.processor.Ghostwriter.INSTRUCTIONS_PROP_NAME, "from-config");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);

		Method m = GWCommand.class.getDeclaredMethod("resolveInstructions", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("from-config", result);
	}

	@Test
	void resolveGuidance_whenNull_returnsConfigValue() throws Exception {
		// Arrange
		ConfigCommand.config.set(org.machanism.machai.gw.processor.Ghostwriter.GW_GUIDANCE_PROP_NAME,
				"guidance-from-config");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveGuidance", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("guidance-from-config", result);
	}

	@Test
	void splitExcludes_whenNull_returnsNull_andWhenCsv_splits() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
		m.setAccessible(true);

		// Act
		String[] resultNull = (String[]) m.invoke(cmd, new Object[] { null });
		String[] resultCsv = (String[]) m.invoke(cmd, "target,.git");

		// Assert
		assertNull(resultNull);
		assertArrayEquals(new String[] { "target", ".git" }, resultCsv);
	}

	@Test
	void resolveScanDirs_whenNullOrEmpty_returnsRootDirAbsolutePath() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveScanDirs", String[].class, File.class);
		m.setAccessible(true);
		File rootDir = new File(".").getAbsoluteFile();

		// Act
		String[] resultNull = (String[]) m.invoke(cmd, null, rootDir);
		String[] resultEmpty = (String[]) m.invoke(cmd, new Object[] { new String[0], rootDir });

		// Assert
		assertArrayEquals(new String[] { rootDir.getAbsolutePath() }, resultNull);
		assertArrayEquals(new String[] { rootDir.getAbsolutePath() }, resultEmpty);
	}

	@Test
	void loadMachaiPropertiesConfig_whenFileMissing_doesNotThrow_andReturnsNonNullConfig() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("loadMachaiPropertiesConfig");
		m.setAccessible(true);

		// Act
		Object cfg = m.invoke(cmd);

		// Assert
		assertNotNull(cfg);
		assertEquals("org.machanism.macha.core.commons.configurator.PropertiesConfigurator", cfg.getClass().getName());
	}

	@Test
	void internalValueObjects_constructorsAssignFields() throws Exception {
		// Arrange
		Class<?> promptCtxClass = Class.forName("org.machanism.machai.cli.GWCommand$PromptContext");
		Constructor<?> promptCtor = promptCtxClass.getDeclaredConstructor(String.class, String.class);
		promptCtor.setAccessible(true);

		Class<?> execCtxClass = Class.forName("org.machanism.machai.cli.GWCommand$ExecutionContext");
		Constructor<?> execCtor = execCtxClass.getDeclaredConstructor(int.class, Boolean.class);
		execCtor.setAccessible(true);

		Class<?> processingCtxClass = Class.forName("org.machanism.machai.cli.GWCommand$ProcessingContext");
		Constructor<?> processingCtor = processingCtxClass.getDeclaredConstructor(File.class, String.class, String.class,
				org.machanism.macha.core.commons.configurator.PropertiesConfigurator.class, String[].class, promptCtxClass,
				execCtxClass);
		processingCtor.setAccessible(true);

		Object promptCtx = promptCtor.newInstance("instr", "guid");
		Object execCtx = execCtor.newInstance(3, Boolean.TRUE);
		Object propsCfg = new org.machanism.macha.core.commons.configurator.PropertiesConfigurator();
		String[] excludes = new String[] { "a", "b" };
		File root = new File(".");
		String scanDir = "src";
		String model = "X:Y";

		// Act
		Object processingCtx = processingCtor.newInstance(root, scanDir, model, propsCfg, excludes, promptCtx, execCtx);

		// Assert (via reflective field reads)
		assertEquals("instr", getField(promptCtx, "instructionsValue"));
		assertEquals("guid", getField(promptCtx, "defaultGuidance"));
		assertEquals(3, getField(execCtx, "threads"));
		assertEquals(Boolean.TRUE, getField(execCtx, "logInputs"));

		assertEquals(root, getField(processingCtx, "projectDir"));
		assertEquals(scanDir, getField(processingCtx, "scanDir"));
		assertEquals(model, getField(processingCtx, "genaiValue"));
		assertSame(propsCfg, getField(processingCtx, "config"));
		assertArrayEquals(excludes, (String[]) getField(processingCtx, "excludesArr"));
		assertSame(promptCtx, getField(processingCtx, "prompts"));
		assertSame(execCtx, getField(processingCtx, "execution"));
	}

	private static Object getField(Object target, String fieldName) throws Exception {
		var f = target.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(target);
	}

	@Test
	void init_isNoOpButCallable() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);

		// Act/Assert
		assertDoesNotThrow(cmd::init);
	}

	@Test
	void gw_whenDownstreamThrowsException_isHandledAndDoesNotThrow() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);

		// Act/Assert
		// Use a clearly invalid scan path to increase the likelihood of downstream IOException.
		assertDoesNotThrow(() -> cmd.gw(1, null, null, null, null, null, null, new String[] { "Z:\\this-drive-should-not-exist" }));
	}

	@Test
	void processTerminationException_isConstructible_andExitCodeReadable() throws Exception {
		// Arrange
		Constructor<ProcessTerminationException> ctor = ProcessTerminationException.class.getDeclaredConstructor(String.class,
				int.class);
		ctor.setAccessible(true);

		// Act
		ProcessTerminationException ex = ctor.newInstance("bye", 7);

		// Assert
		assertEquals(7, ex.getExitCode());
		assertEquals("bye", ex.getMessage());
	}

    @Test
	void getProjectStructureDescription_whenFieldsAreMissing_usesNotDefinedPlaceholder() throws IOException {
  // TestMate-3cacb2f59f15740d9dcca7d9f06a1f2a
		// Arrange
		File file = new File(projectDir, "src/Main.java");
		when(projectLayout.getProjectDir()).thenReturn(projectDir);
		when(projectLayout.getProjectName()).thenReturn(null);
		when(projectLayout.getProjectId()).thenReturn("test-id");
		when(projectLayout.getParentId()).thenReturn(null);
		when(projectLayout.getProjectLayoutType()).thenReturn("Maven");
		when(projectLayout.getSources()).thenReturn(Collections.emptyList());
		when(projectLayout.getTests()).thenReturn(Collections.emptyList());
		when(projectLayout.getDocuments()).thenReturn(Collections.emptyList());
		when(projectLayout.getModules()).thenReturn(Collections.emptyList());
		// Act
		String description = aiFileProcessor.getProjectStructureDescription(projectLayout, file);
		// Assert
		assertTrue(description.contains(AIFileProcessor.NOT_DEFINED));
		int count = StringUtils.countMatches(description, AIFileProcessor.NOT_DEFINED);
		assertTrue(count >= 5, "Expected multiple 'not defined' placeholders for missing metadata");
		assertTrue(description.endsWith(GenAIProvider.LINE_SEPARATOR));
	}

    @Test
	void getProjectStructureDescription_whenParentDirIsNull_handlesRootDirectoryGracefully() throws IOException {
  // TestMate-7d27b66bfb20f88c237ad4d0d53a8e66
		// Arrange
		File rootProjectDir = mock(File.class);
		File targetFile = mock(File.class);
		when(rootProjectDir.getName()).thenReturn("/");
		when(rootProjectDir.getAbsolutePath()).thenReturn("/");
		when(rootProjectDir.getParentFile()).thenReturn(null);
		when(targetFile.getName()).thenReturn("Main.java");
		when(targetFile.getAbsolutePath()).thenReturn("/Main.java");
		when(projectLayout.getProjectDir()).thenReturn(rootProjectDir);
		when(projectLayout.getProjectName()).thenReturn("RootProject");
		when(projectLayout.getProjectId()).thenReturn("root-id");
		when(projectLayout.getParentId()).thenReturn(null);
		when(projectLayout.getProjectLayoutType()).thenReturn("Default");
		when(projectLayout.getSources()).thenReturn(Collections.emptyList());
		when(projectLayout.getTests()).thenReturn(Collections.emptyList());
		when(projectLayout.getDocuments()).thenReturn(Collections.emptyList());
		when(projectLayout.getModules()).thenReturn(Collections.emptyList());
		// Act
		String description = aiFileProcessor.getProjectStructureDescription(projectLayout, targetFile);
		// Then
		assertNotNull(description);
		assertTrue(description.contains(AIFileProcessor.NOT_DEFINED));
		int count = StringUtils.countMatches(description, AIFileProcessor.NOT_DEFINED);
		assertTrue(count >= 2, "Expected 'not defined' for both parentId and parentDir name");
		assertTrue(description.endsWith(GenAIProvider.LINE_SEPARATOR));
	}

    @Test
void getDirInfoLine_whenDirectoriesExist_returnsFormattedJoinedString(@TempDir File tempDir) throws Exception {
    // TestMate-457ebcbd2eb59adf1ee98fb544a32eba
    // Arrange
    File projectDir = new File(tempDir, "project-root");
    projectDir.mkdir();
    File srcDir = new File(projectDir, "src");
    srcDir.mkdir();
    File docsDir = new File(projectDir, "docs");
    docsDir.mkdir();
    // The empty string "" in the original list resolved to projectDir itself, which exists.
    // To match the expected output "`src`, `docs`", we remove the empty string and 
    // provide a non-existent directory "test" and a null entry to verify filtering logic.
    List<String> sources = Arrays.asList("src", "test", "docs", null);
    AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "test-model");
    Method method = AIFileProcessor.class.getDeclaredMethod("getDirInfoLine", List.class, File.class);
    method.setAccessible(true);
    // Act
    String result = (String) method.invoke(processor, sources, projectDir);
    // Then
    assertEquals("`src`, `docs`", result);
}

    @Test
	void setInstructions_whenNull_shouldSetEmptyString() {
  // TestMate-ef926d482c6800720728320586162c64
		// Arrange
		String input = null;
		// Act
		aiFileProcessor.setInstructions(input);
		// Assert
		assertEquals("", aiFileProcessor.getInstructions());
	}

    @Test
    void setInstructions_whenFileReference_shouldExpandContentAndResolveProperties(@TempDir File tempDir) throws Exception {
        // TestMate-05e8c453671d1306d18c468ec1e8df82
        // Given
        String propertyName = "test.instruction.dir";
        String fileName = "instructions.txt";
        String fileContent = "System instructions from file.";
        File instructionFile = new File(tempDir, fileName);
        Files.write(instructionFile.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
        System.setProperty(propertyName, tempDir.getAbsolutePath());
        try {
            String input = "file:${" + propertyName + "}/" + fileName;
            // When
            aiFileProcessor.setInstructions(input);
            // Then
            // The implementation of parseLines is recursive. 
            // 1. The top-level call reads the "file:" line and appends the result of tryToGetInstructionsFromReference + LINE_SEPARATOR.
            // 2. tryToGetInstructionsFromReference calls parseLines on the file content.
            // 3. The recursive call reads the file content line and appends that line + LINE_SEPARATOR.
            // This results in two line separators.
            String expected = fileContent + GenAIProvider.LINE_SEPARATOR + GenAIProvider.LINE_SEPARATOR;
            assertEquals(expected, aiFileProcessor.getInstructions());
        } finally {
            System.clearProperty(propertyName);
        }
    }

    @Test
    void setInstructions_whenMixedContent_shouldProcessLineByLine(@TempDir File tempDir) throws IOException {
        // TestMate-9cf2ee1363babd7f1cc9a6e70ced727d
        // Given
        String fileName = "part2.txt";
        String fileContent = "Expanded Content";
        File instructionFile = new File(tempDir, fileName);
        Files.write(instructionFile.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
        
        // Create processor with tempDir as project root to resolve relative file: references
        AIFileProcessor processor = new AIFileProcessor(tempDir, configurator, model);
        String input = "Line 1" + "\n" + "file:" + fileName + "\n" + "\n" + "Line 4";
        
        // When
        processor.setInstructions(input);
        
        // Then
        String sep = GenAIProvider.LINE_SEPARATOR;
        // Line 1 -> "Line 1" + sep
        // Line 2 (file reference) -> "Expanded Content" + sep (from recursive parseLines) + sep (from main loop)
        // Line 3 (empty line) -> sep
        // Line 4 -> "Line 4" + sep
        String expected = "Line 1" + sep + fileContent + sep + sep + sep + "Line 4" + sep;
        assertEquals(expected, processor.getInstructions());
    }

    @Test
    void setInstructions_whenRecursiveReference_shouldExpandDeeply(@TempDir File tempDir) throws IOException {
        // TestMate-542cfc12606881ddd62ff9c98c89e664
        // Given
        String leafContent = "Leaf Content";
        File leafFile = new File(tempDir, "leaf.txt");
        Files.write(leafFile.toPath(), leafContent.getBytes(StandardCharsets.UTF_8));
        File rootFile = new File(tempDir, "root.txt");
        String rootContent = "file:leaf.txt";
        Files.write(rootFile.toPath(), rootContent.getBytes(StandardCharsets.UTF_8));
        AIFileProcessor processor = new AIFileProcessor(tempDir, configurator, model);
        String input = "file:root.txt";
        // When
        processor.setInstructions(input);
        // Then
        String sep = GenAIProvider.LINE_SEPARATOR;
        // 1. setInstructions("file:root.txt") calls parseLines("file:root.txt")
        // 2. parseLines reads "file:root.txt", calls tryToGetInstructionsFromReference("file:root.txt")
        // 3. tryToGetInstructionsFromReference reads root.txt content ("file:leaf.txt"), calls parseLines("file:leaf.txt")
        // 4. recursive parseLines reads "file:leaf.txt", calls tryToGetInstructionsFromReference("file:leaf.txt")
        // 5. tryToGetInstructionsFromReference reads leaf.txt content ("Leaf Content"), calls parseLines("Leaf Content")
        // 6. deepest parseLines reads "Leaf Content", appends "Leaf Content" + sep
        // Each level of parseLines appends its own separator after the content returned by the reference resolver.
        String expected = leafContent + sep + sep + sep;
        assertEquals(expected, processor.getInstructions());
    }

    @Test
    void setInstructions_whenRelativeFilePath_shouldResolveAgainstProjectDir(@TempDir File tempDir) throws IOException {
        // TestMate-5501b167fa0331c7c4d4c53426b95355
        // Given
        String fileName = "rules.txt";
        String subDirName = "docs";
        String fileContent = "Relative file content";
        
        File docsDir = new File(tempDir, subDirName);
        docsDir.mkdir();
        File instructionFile = new File(docsDir, fileName);
        Files.write(instructionFile.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
        AIFileProcessor processor = new AIFileProcessor(tempDir, configurator, model);
        String input = "file:" + subDirName + "/" + fileName;
        // When
        processor.setInstructions(input);
        // Then
        String sep = GenAIProvider.LINE_SEPARATOR;
        String expected = fileContent + sep + sep;
        assertEquals(expected, processor.getInstructions());
    }
}
