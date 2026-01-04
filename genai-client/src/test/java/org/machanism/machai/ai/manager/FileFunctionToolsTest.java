package org.machanism.machai.ai.manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

class FileFunctionToolsTest {
    @Test
    void throwsIfWorkingDirIsNull() {
        FileFunctionTools tools = new FileFunctionTools(null);
        Exception exception = assertThrows(IllegalArgumentException.class, tools::getWorkingDir);
        assertTrue(exception.getMessage().contains("working dir is not defined"));
    }

    @Test
    void setAndGetWorkingDirWorksCorrectly() {
        File dir = new File("/");
        FileFunctionTools tools = new FileFunctionTools(dir);
        assertEquals(dir, tools.getWorkingDir());
    }
}
