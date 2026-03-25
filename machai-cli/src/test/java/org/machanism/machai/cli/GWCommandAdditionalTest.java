package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.lang.reflect.Method;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.Mockito;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.mockito.MockedConstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.apache.commons.lang.SystemUtils;

class GWCommandAdditionalTest {

	@Test
	void resolveModel_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, "Configured:Model");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveModel", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("Configured:Model", result);
	}

	@Test
	void resolveModel_shouldPreferOptionValue_whenProvided() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, "Configured:Model");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveModel", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, "Opt:Model");

		// Assert
		assertEquals("Opt:Model", result);
	}

	@Test
	void resolveLogInputs_shouldReturnFalse_whenConfigValueIsEmptyString() throws Exception {
		// Arrange
		ConfigCommand.config.set(GenAIProvider.LOG_INPUTS_PROP_NAME, "");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveLogInputs", Boolean.class);
		m.setAccessible(true);

		// Act
		Boolean result = (Boolean) m.invoke(cmd, Boolean.TRUE);

		// Assert
		assertFalse(result);
	}

	@Test
	void resolveLogInputs_shouldReturnConfiguredValue_whenPresent() throws Exception {
		// Arrange
		ConfigCommand.config.set(GenAIProvider.LOG_INPUTS_PROP_NAME, "false");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveLogInputs", Boolean.class);
		m.setAccessible(true);

		// Act
		Boolean result = (Boolean) m.invoke(cmd, Boolean.TRUE);

		// Assert
		assertFalse(result);
	}

	@Test
	void resolveRootDir_shouldReturnNonNull_whenArgumentNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, "");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveProjectDir", File.class);
		m.setAccessible(true);

		// Act
		File result = (File) m.invoke(cmd, new Object[] { null });

		// Assert
		assertNotNull(result);
	}

	@Test
	void resolveRootDir_shouldReturnConfiguredRootDir_whenPresent() throws Exception {
		// Arrange
		File configured = new File("target").getAbsoluteFile();
		ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, configured.getAbsolutePath());
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveProjectDir", File.class);
		m.setAccessible(true);

		// Act
		File result = (File) m.invoke(cmd, new Object[] { new File(".") });

		// Assert
		assertEquals(configured.getAbsolutePath(), result.getAbsolutePath());
	}

	@Test
	void resolveInstructions_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.INSTRUCTIONS_PROP_NAME, "cfg-instr");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveInstructions", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("cfg-instr", result);
	}

	@Test
	void resolveGuidance_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_GUIDANCE_PROP_NAME, "cfg-guidance");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveGuidance", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("cfg-guidance", result);
	}

	@Test
	void splitExcludes_shouldReturnSingleEntry_whenNoComma() throws Exception {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
		m.setAccessible(true);

		// Act
		String[] result = (String[]) m.invoke(cmd, "target");

		// Assert
		assertEquals(1, result.length);
		assertEquals("target", result[0]);
	}

	@Test
	void loadMachaiPropertiesConfig_shouldReturnConfigurator_whenFileMissing() throws Exception {
		// Arrange
		File props = new File(ConfigCommand.MACHAI_PROPERTIES_FILE_NAME);
		File backup = null;
		if (props.exists()) {
			backup = new File(props.getParentFile(), props.getName() + ".bak-test");
			if (backup.exists()) {
				backup.delete();
			}
			assertEquals(true, props.renameTo(backup));
		}

		try {
			LineReader reader = Mockito.mock(LineReader.class);
			GWCommand cmd = new GWCommand(reader);
			Method m = GWCommand.class.getDeclaredMethod("loadMachaiPropertiesConfig");
			m.setAccessible(true);

			// Act
			Object result = m.invoke(cmd);

			// Assert
			assertNotNull(result);
		} finally {
			if (backup != null && backup.exists()) {
				backup.renameTo(props);
			}
		}
	}

    @Test
void gw_shouldExecuteGuidancePipelineSuccessfully_whenValidOptionsProvided(@TempDir File tempDir) throws IOException {
    // TestMate-6d35d4b9fa2931d232b6ac7231a1db67
    // Arrange
    int threads = 2;
    String model = "OpenAI:gpt-4";
    String instructions = "test-instructions";
    String guidance = "test-guidance";
    String excludes = "target,node_modules";
    Boolean logInputs = true;
    File projectDir = tempDir;
    String[] scanDirs = new String[] { tempDir.getAbsolutePath() };
    ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, "Default:Model");
    ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, tempDir.getAbsolutePath());
    GenAIProvider mockProvider = mock(GenAIProvider.class);
    LineReader reader = mock(LineReader.class);
    GWCommand cmd = new GWCommand(reader);
    try (MockedStatic<GenAIProviderManager> providerManagerMock = mockStatic(GenAIProviderManager.class);
         MockedStatic<FunctionToolsLoader> toolsLoaderMock = mockStatic(FunctionToolsLoader.class)) {
        
        providerManagerMock.when(() -> GenAIProviderManager.getProvider(eq(model), any())).thenReturn(mockProvider);
        FunctionToolsLoader mockLoader = mock(FunctionToolsLoader.class);
        toolsLoaderMock.when(FunctionToolsLoader::getInstance).thenReturn(mockLoader);
        // Act
        cmd.gw(threads, model, instructions, guidance, excludes, logInputs, projectDir, scanDirs);
        // Assert
        providerManagerMock.verify(GenAIProviderManager::logUsage);
        verify(mockProvider).instructions(any());
        verify(mockProvider).setWorkingDir(any(File.class));
    }
}

    @Test
	void gw_shouldHandleProcessTerminationException_whenAIToolSignalsAbort() throws Exception {
  // TestMate-493eeb8c3b4fd79db3031b587214b484
		// Given
		String terminationMessage = "User Abort";
		int exitCode = 130;
		ProcessTerminationException exception = new ProcessTerminationException(terminationMessage, exitCode);
		LineReader lineReader = mock(LineReader.class);
		GWCommand cmd = new GWCommand(lineReader);
		// Ensure configuration is set to avoid NullPointerExceptions during resolution phase
		ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, "MockProvider:MockModel");
		// Intercept GuidanceProcessor construction to inject the exception
		// We also mock GenAIProviderManager to verify the finally block execution (logUsage)
		try (MockedStatic<GenAIProviderManager> providerManagerMock = mockStatic(GenAIProviderManager.class);
				MockedConstruction<GuidanceProcessor> mockedProcessor = mockConstruction(GuidanceProcessor.class,
						(mock, context) -> {
							// Force the processor to throw the exception when scanDocuments is called
							// This simulates an AI Tool signaling a termination/abort
							doThrow(exception).when(mock).scanDocuments(any(File.class), any(String.class));
						})) {
			// When
			// Calling the actual gw method.
			// The method should catch the ProcessTerminationException internally as per its implementation.
			cmd.gw(1, "MockProvider:MockModel", "instructions", "guidance", null, false, new File("."),
					new String[] { "." });
			// Then
			// Verify that the finally block was executed by checking the static call to logUsage.
			// If the exception was not caught, this point would not be reached or the test would fail with the exception.
			providerManagerMock.verify(GenAIProviderManager::logUsage);
		}
	}

    @Test
    void gw_shouldHandleGenericException_whenUnexpectedErrorOccurs() {
        // TestMate-dbe17b670dcd62dee51aace304ef683d
        // Given
        LineReader reader = mock(LineReader.class);
        GWCommand cmd = new GWCommand(reader);
        RuntimeException unexpectedException = new RuntimeException("Disk Full");
        // Arrange: Set up configuration to ensure the execution reaches the GuidanceProcessor
        ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, "Mock:Model");
        try (MockedStatic<GenAIProviderManager> providerManagerMock = mockStatic(GenAIProviderManager.class);
             MockedConstruction<GuidanceProcessor> mockedProcessor = mockConstruction(GuidanceProcessor.class,
                     (mock, context) -> {
                         // Force the processor to throw a generic RuntimeException when scanDocuments is called.
                         // This allows the internal try-catch block in GWCommand.gw() to handle it.
                         doThrow(unexpectedException).when(mock).scanDocuments(any(File.class), any(String.class));
                     })) {
            // When
            // Calling the actual gw method. The internal catch (Exception e) block should handle the RuntimeException.
            cmd.gw(1, "Mock:Model", "instr", "guidance", null, false, new File("."), new String[]{"."});
            // Then
            // Verify that the finally block was executed by checking the static call to logUsage.
            providerManagerMock.verify(GenAIProviderManager::logUsage);
        }
    }

    @Test
void resolveGuidance_shouldPromptUser_whenOptionIsEmptyString() throws Exception {
    // TestMate-309122a994428f05a929007f14cdb2d1
    // Arrange
    LineReader reader = Mockito.mock(LineReader.class);
    String expectedGuidance = "Refactor for clarity";
    when(reader.readLine("Guidance: ")).thenReturn(expectedGuidance);
    
    GWCommand cmd = new GWCommand(reader);
    Method m = GWCommand.class.getDeclaredMethod("resolveGuidance", String.class);
    m.setAccessible(true);
    // Act
    String result = (String) m.invoke(cmd, "");
    // Assert
    assertEquals(expectedGuidance, result);
    verify(reader).readLine("Guidance: ");
}

    @Test
void resolveScanDirs_shouldReturnProjectDir_whenScanDirsIsNullOrEmpty() throws Exception {
    // TestMate-64c0d7755384fa9ac819987bc5392386
    // Arrange
    LineReader reader = Mockito.mock(LineReader.class);
    GWCommand cmd = new GWCommand(reader);
    Method m = GWCommand.class.getDeclaredMethod("resolveScanDirs", String[].class, File.class);
    m.setAccessible(true);
    File projectDir = new File("test-root").getAbsoluteFile();
    String expectedPath = projectDir.getAbsolutePath();
    // Act
    String[] resultNull = (String[]) m.invoke(cmd, null, projectDir);
    String[] resultEmpty = (String[]) m.invoke(cmd, new Object[] { new String[0], projectDir });
    // Assert
    assertArrayEquals(new String[] { expectedPath }, resultNull);
    assertArrayEquals(new String[] { expectedPath }, resultEmpty);
}

    @Test
void resolveProjectDir_shouldReturnUserDir_whenInputAndConfigAreNull() throws Exception {
    // TestMate-11a27e0610db4c71fd0ca86e7670a5c0
    // Arrange
    ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, null);
    LineReader reader = Mockito.mock(LineReader.class);
    GWCommand cmd = new GWCommand(reader);
    Method m = GWCommand.class.getDeclaredMethod("resolveProjectDir", File.class);
    m.setAccessible(true);
    File expected = SystemUtils.getUserDir();
    // Act
    File result = (File) m.invoke(cmd, new Object[] { null });
    // Assert
    assertNotNull(result);
    assertEquals(expected.getAbsolutePath(), result.getAbsolutePath());
}

    @Test
void splitExcludes_shouldReturnArray_whenCommaSeparatedStringProvided() throws Exception {
    // TestMate-6a723da8b6ef7b067982e0e1b94fc913
    // Arrange
    LineReader reader = Mockito.mock(LineReader.class);
    GWCommand cmd = new GWCommand(reader);
    String input = "target,.git,node_modules";
    Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
    m.setAccessible(true);
    // Act
    String[] result = (String[]) m.invoke(cmd, input);
    // Assert
    assertNotNull(result);
    assertEquals(3, result.length);
    assertArrayEquals(new String[]{"target", ".git", "node_modules"}, result);
}

}
