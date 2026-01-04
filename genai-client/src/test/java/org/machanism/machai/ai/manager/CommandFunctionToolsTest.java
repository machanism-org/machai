package org.machanism.machai.ai.manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

class CommandFunctionToolsTest {
    @Test
    void throwsIfWorkingDirIsNull() {
        CommandFunctionTools tools = new CommandFunctionTools(null);
        Exception exception = assertThrows(IllegalArgumentException.class, tools::getWorkingDir);
        assertTrue(exception.getMessage().contains("working dir is not defined"));
    }

    @Test
    void setAndGetWorkingDirWorksCorrectly() {
        File dir = new File("/");
        CommandFunctionTools tools = new CommandFunctionTools(dir);
        assertEquals(dir, tools.getWorkingDir());
    }
}
