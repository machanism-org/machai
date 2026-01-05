package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CommandFunctionToolsTest {
    private CommandFunctionTools commandFunctionTools;
    private File tempDir;
    private static final Logger logger = LoggerFactory.getLogger(CommandFunctionToolsTest.class);

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"));
        commandFunctionTools = new CommandFunctionTools(tempDir);
    }

    @Test
    void getWorkingDir_withNullWorkingDir_throwsException() {
        CommandFunctionTools tools = new CommandFunctionTools(null);
        Exception ex = assertThrows(IllegalArgumentException.class, tools::getWorkingDir);
        assertEquals("The function tool working dir is not defined.", ex.getMessage());
    }

    @Test
    void setWorkingDir_setsDirSuccessfully() {
        File newDir = new File(tempDir, "testDir");
        commandFunctionTools.setWorkingDir(newDir);
        assertSame(newDir, commandFunctionTools.getWorkingDir());
    }

    @Test
    void applyTools_invokesAddTool() {
        GenAIProvider mockProvider = Mockito.mock(GenAIProvider.class);
        commandFunctionTools.applyTools(mockProvider);
        Mockito.verify(mockProvider, Mockito.atLeastOnce()).addTool(
                Mockito.eq("run_command_line_tool"),
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any());
    }

}
