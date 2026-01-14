package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link TypeScriptReviewer}.
 */
class TypeScriptReviewerTest {

    @TempDir
    File tempDir;

    @Test
    void perform_returnsNull_whenNoGuidance() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        File tsFile = new File(tempDir, "file.ts");
        Files.writeString(tsFile.toPath(), "export const x = 1;");

        assertNull(reviewer.perform(tempDir, tsFile));
    }
}
