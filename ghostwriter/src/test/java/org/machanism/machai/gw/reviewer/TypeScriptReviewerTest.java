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

    @Test
    @Disabled
    void perform_extractsGuidance_fromLineComment() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        File tsFile = new File(tempDir, "file.ts");
        Files.writeString(tsFile.toPath(), "// @guidance: Use camelCase variables.\nconst y = 1;");

        String result = reviewer.perform(tempDir, tsFile);

        assertNotNull(result);
        assertTrue(result.contains("Use camelCase variables."));
    }

    @Test
    @Disabled
    void perform_extractsGuidance_fromBlockComment() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        File tsFile = new File(tempDir, "file.ts");
        Files.writeString(tsFile.toPath(), "/** @guidance: Block comment guidance */\nlet z = 2;");

        String result = reviewer.perform(tempDir, tsFile);

        assertNotNull(result);
        assertTrue(result.contains("Block comment guidance"));
    }
}
