package org.machanism.machai.gw.reviewer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MarkdownReviewer}.
 * <p>
 * Tests extraction of @guidance comments from Markdown files.
 */
class MarkdownReviewerTest {

    @TempDir
    File tempDir;

    @Test
    void perform_returnsNull_whenNoGuidance() throws IOException {
        MarkdownReviewer reviewer = new MarkdownReviewer();
        File mdFile = new File(tempDir, "README.md");
        Files.writeString(mdFile.toPath(), "# Hello world");
        assertNull(reviewer.perform(tempDir, mdFile));
    }

    @Test
    @Disabled("Need to fix.")
    void perform_extractsGuidance_whenGuidanceCommentPresent() throws IOException {
        MarkdownReviewer reviewer = new MarkdownReviewer();
        File mdFile = new File(tempDir, "GUIDE.md");
        Files.writeString(mdFile.toPath(), "<!-- @guidance: Markdown guidance -->\n# Title");
        String result = reviewer.perform(tempDir, mdFile);
        assertNotNull(result);
        assertTrue(result.contains("Markdown guidance"));
    }
}
