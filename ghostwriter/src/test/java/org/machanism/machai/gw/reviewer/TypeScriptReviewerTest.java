package org.machanism.machai.gw.reviewer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TypeScriptReviewer}.
 * <p>
 * Tests extraction of guidance from TypeScript files, ensuring detection of @guidance tag.
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
    @Disabled("Need to fix.")
    void perform_extractsGuidance_fromLineComment() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        File tsFile = new File(tempDir, "file.ts");
        Files.writeString(tsFile.toPath(), "// @guidance: Use camelCase variables.\nconst y = 1;");
        String result = reviewer.perform(tempDir, tsFile);
        assertNotNull(result);
        assertTrue(result.contains("Use camelCase variables."));
    }

    @Test
    @Disabled("Need to fix.")
    void perform_extractsGuidance_fromBlockComment() throws IOException {
        TypeScriptReviewer reviewer = new TypeScriptReviewer();
        File tsFile = new File(tempDir, "file.ts");
        Files.writeString(tsFile.toPath(), "/** @guidance: Block comment guidance */\nlet z = 2;");
        String result = reviewer.perform(tempDir, tsFile);
        assertNotNull(result);
        assertTrue(result.contains("Block comment guidance"));
    }
}
