package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link PythonReviewer}.
 * <p>
 * Verifies that PythonReviewer correctly extracts @guidance tags from Python files.
 */
class PythonReviewerTest {

    @TempDir
    File tempDir;

    @Test
    void perform_returnsNull_whenNoGuidance() throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        File pyFile = new File(tempDir, "example.py");
        Files.writeString(pyFile.toPath(), "print('Hello world')");
        assertNull(reviewer.perform(tempDir, pyFile));
    }

    @Test
    @Disabled("Need to fix.")
    void perform_extractsGuidance_fromComment() throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        File pyFile = new File(tempDir, "example.py");
        Files.writeString(pyFile.toPath(), "# @guidance: Use snake_case for variables.\ndef foo(): pass");
        String result = reviewer.perform(tempDir, pyFile);
        assertNotNull(result);
        assertTrue(result.contains("Use snake_case for variables."));
    }

    @Test
    @Disabled("Need to fix.")
    void perform_extractsGuidance_fromTripleQuotes() throws IOException {
        PythonReviewer reviewer = new PythonReviewer();
        File pyFile = new File(tempDir, "example.py");
        Files.writeString(pyFile.toPath(), "\"\"\" @guidance: Triple quoted guidance.\n\"\"\"\nfoo = 123");
        String result = reviewer.perform(tempDir, pyFile);
        assertNotNull(result);
        assertTrue(result.contains("Triple quoted guidance."));
    }
}
