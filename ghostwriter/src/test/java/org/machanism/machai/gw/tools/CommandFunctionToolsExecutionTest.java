package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;

class CommandFunctionToolsExecutionTest {

    @TempDir
    Path projectDirectory;

    @Test
    void executeCommandRejectsInvalidWorkingDirectoryBeforeStartingProcess() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();

        // Act
        Object result = tools.executeCommand("echo ignored", null, "..", 128, "UTF-8",
                projectDirectory.toFile(), null);

        // Assert
        assertEquals("Error: Invalid working directory.", result);
    }

    @Test
    void executeCommandCapturesOutputAndReturnsSuccessfulReport() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();
        Configurator configurator = permissiveConfigurator();
        String command = System.getProperty("os.name").toLowerCase().contains("win")
                ? "cmd /c echo command-coverage" : "/usr/bin/printf command-coverage";

        // Act
        Object result = tools.executeCommand(command, Collections.emptyMap(), ".", 128, "UTF-8",
                projectDirectory.toFile(), configurator);

        // Assert
        Map<?, ?> report = assertInstanceOf(Map.class, result);
        assertEquals(0, report.get("exitCode"));
        Map<?, ?> log = assertInstanceOf(Map.class, report.get("log"));
        assertTrue(log.toString().contains("command-coverage"));
    }

    @Test
    void executeCommandReturnsErrorResultForNonZeroExitCode() {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();
        Configurator configurator = permissiveConfigurator();
        String command = System.getProperty("os.name").toLowerCase().contains("win")
                ? "cmd /c exit /b 3" : "/usr/bin/sh -c \"exit 3\"";

        // Act / Assert
        assertThrows(org.machanism.machai.ai.tools.ErrorResultException.class,
                () -> tools.executeCommand(command, null, ".", 128, "UTF-8", projectDirectory.toFile(), configurator));
    }

    private Configurator permissiveConfigurator() {
        Configurator configurator = mock(Configurator.class);
        when(configurator.get("ft.command.denylist", null)).thenReturn("");
        return configurator;
    }
}
