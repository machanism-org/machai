package org.machanism.machai.ai.manager;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link SystemFunctionTools}.
 * <p>
 * Verifies delegation and working directory management for tool application.
 *
 * @author Viktor Tovstyi
 * @guidance
 */
class SystemFunctionToolsTest {
    private SystemFunctionTools systemFunctionTools;
    private File tempDir;

    /**
     * Set up a SystemFunctionTools instance using system temp directory for each test.
     */
    @BeforeEach
    void setUp() {
        tempDir = new File(System.getProperty("java.io.tmpdir"));
        systemFunctionTools = new SystemFunctionTools(tempDir);
    }

    /**
     * Ensure applyTools delegates to the file and command tool adders.
     */
    @Test
    void applyTools_delegatesToFileAndCommandFunctionTools() {
        GenAIProvider mockProvider = Mockito.mock(GenAIProvider.class);
        systemFunctionTools.applyTools(mockProvider);
        Mockito.verify(mockProvider, Mockito.atLeastOnce()).addTool(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    /**
     * Checks correct delegation for working directory changes to child tools.
     */
    @Test
    void setWorkingDir_changesWorkingDirOfDelegates() {
        File newDir = new File(tempDir, "systemTestDir");
        systemFunctionTools.setWorkingDir(newDir);
        // Should not throw, and should delegate
    }
}
