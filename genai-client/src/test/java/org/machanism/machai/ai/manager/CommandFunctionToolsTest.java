package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandFunctionToolsTest {

    @Test
    void getWorkingDir_whenNotSet_throwsException() {
        CommandFunctionTools tools = new CommandFunctionTools(null);
        Exception exception = assertThrows(IllegalArgumentException.class, tools::getWorkingDir);
        assertTrue(exception.getMessage().contains("working dir is not defined"));
    }

    @Test
    void setWorkingDir_setsWorkingDirCorrectly(@TempDir File tempDir) {
        CommandFunctionTools tools = new CommandFunctionTools(tempDir);
        assertEquals(tempDir, tools.getWorkingDir());
    }
}
