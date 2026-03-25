package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.project.layout.ProjectLayout;
import org.apache.commons.lang.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

class ActCommandTest {

    @InjectMocks
    private ActCommand actCommand;

    @BeforeEach
    void setUp() {
        ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, null);
        ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, "TestModel");
    }

    @AfterEach
    void tearDown() {
        ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, null);
        ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, null);
    }

	@Test
	void init_shouldNotThrow() {
		// Arrange
		ActCommand cmd = new ActCommand();

		// Act + Assert
		assertDoesNotThrow(cmd::init);
	}

	@Test
	void act_whenGenAiProviderIsNotAvailable_shouldPropagateError() {
		// Arrange
		ActCommand cmd = new ActCommand();
		ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, new File(".").getAbsolutePath());
		ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, "TestProvider");

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> cmd.act(new String[] { "commit" }));
	}

    @Test
    void act_shouldResolveScanDirWithCorrectPriority() throws IOException {
        // TestMate-14fca44fcb6a83f06d6d046cee3ecfea
        ActCommand cmd = new ActCommand();
        String[] actArgs = {"test", "action"};
        File projectDir = new File("project-root").getAbsoluteFile();
        String customScanDir = new File("custom-scan").getAbsolutePath();
        String systemUserDir = SystemUtils.getUserDir().getAbsolutePath();
        // Scenario 1: Priority 1 - Resolved from ActProcessor's Configurator (Local gw.properties)
        try (MockedConstruction<ActProcessor> mocked = mockConstruction(ActProcessor.class, (mock, context) -> {
            File pDir = (File) context.arguments().get(0);
            Configurator localConfig = (Configurator) context.arguments().get(1);
            localConfig.set(Ghostwriter.GW_SCAN_DIR_PROP_NAME, customScanDir);
            
            org.mockito.Mockito.when(mock.getConfigurator()).thenReturn(localConfig);
            org.mockito.Mockito.when(mock.getProjectDir()).thenReturn(pDir);
        })) {
            ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, projectDir.getAbsolutePath());
            
            // When
            cmd.act(actArgs);
            
            // Then
            ActProcessor processor = mocked.constructed().get(0);
            verify(processor).scanDocuments(any(), eq(customScanDir));
        }
        // Scenario 2: Priority 2 - Resolved from Project Directory (ConfigCommand.config)
        ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, projectDir.getAbsolutePath());
        try (MockedConstruction<ActProcessor> mocked = mockConstruction(ActProcessor.class, (mock, context) -> {
            File pDir = (File) context.arguments().get(0);
            Configurator localConfig = (Configurator) context.arguments().get(1);
            
            org.mockito.Mockito.when(mock.getConfigurator()).thenReturn(localConfig);
            org.mockito.Mockito.when(mock.getProjectDir()).thenReturn(pDir);
        })) {
            // When
            cmd.act(actArgs);
            
            // Then
            ActProcessor processor = mocked.constructed().get(0);
            // scanDocuments is called with projectDir.getAbsolutePath() because local config scanDir is null
            verify(processor).scanDocuments(any(), eq(projectDir.getAbsolutePath()));
        }
        // Scenario 3: Priority 3 - Fallback to System User Directory
        ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, null);
        try (MockedConstruction<ActProcessor> mocked = mockConstruction(ActProcessor.class, (mock, context) -> {
            File pDir = (File) context.arguments().get(0);
            Configurator localConfig = (Configurator) context.arguments().get(1);
            
            org.mockito.Mockito.when(mock.getConfigurator()).thenReturn(localConfig);
            org.mockito.Mockito.when(mock.getProjectDir()).thenReturn(pDir);
        })) {
            // When
            cmd.act(actArgs);
            
            // Then
            ActProcessor processor = mocked.constructed().get(0);
            verify(processor).scanDocuments(any(), eq(systemUserDir));
        }
    }
}
