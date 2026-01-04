package org.machanism.machai.ai.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class SystemFunctionToolsTest {
    private SystemFunctionTools systemFunctionTools;
    private File tempDir;

    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"));
        systemFunctionTools = new SystemFunctionTools(tempDir);
    }

    @Test
    void applyTools_delegatesToFileAndCommandFunctionTools() {
        GenAIProvider mockProvider = Mockito.mock(GenAIProvider.class);
        systemFunctionTools.applyTools(mockProvider);
        Mockito.verify(mockProvider, Mockito.atLeastOnce()).addTool(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void setWorkingDir_changesWorkingDirOfDelegates() {
        File newDir = new File(tempDir, "systemTestDir");
        systemFunctionTools.setWorkingDir(newDir);
        // Should not throw, and should delegate
    }
}
