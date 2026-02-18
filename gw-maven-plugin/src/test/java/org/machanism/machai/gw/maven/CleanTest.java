package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.ProjectProcessor;

/**
 * Unit tests for {@link Clean} Maven Mojo.
 * <p>
 * Verifies cleanup of documentation inputs in a Maven project using temporary directories.
 * </p>
 */
public class CleanTest {

    private Clean clean;

    @TempDir
    File tempDir;

    @BeforeEach
    void setup() {
        clean = new Clean();
        clean.basedir = tempDir;
    }

    /**
     * Tests that the execute method completes without exception.
     */
    @Test
    void testExecuteCleansInputsLog() {
        File machaiDir = new File(tempDir, ProjectProcessor.MACHAI_TEMP_DIR);
        assertTrue(machaiDir.mkdirs());
        File docsLog = new File(machaiDir, "docs-inputs");
        assertDoesNotThrow(() -> docsLog.createNewFile());
        assertTrue(docsLog.exists());
        assertDoesNotThrow(() -> clean.execute());
        assertFalse(docsLog.exists());
    }
}
