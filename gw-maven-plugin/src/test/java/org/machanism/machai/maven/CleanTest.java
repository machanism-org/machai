package org.machanism.machai.maven;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

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
        File machaiDir = new File(tempDir, Clean.MACHAI_TEMP_DIR);
        assertTrue(machaiDir.mkdirs());
        File docsLog = new File(machaiDir, "docs-inputs");
        assertDoesNotThrow(() -> docsLog.createNewFile());
        assertTrue(docsLog.exists());
        assertDoesNotThrow(() -> clean.execute());
        assertFalse(docsLog.exists());
    }
}
