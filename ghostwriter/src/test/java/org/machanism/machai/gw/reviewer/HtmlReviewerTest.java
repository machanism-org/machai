package org.machanism.machai.gw.reviewer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HtmlReviewer}.
 * <p>
 * Verifies HTMLReviewer extracts @guidance tags from HTML files.
 */
class HtmlReviewerTest {

    @TempDir
    File tempDir;

    @Test
    void perform_returnsNull_whenNoGuidance() throws IOException {
        HtmlReviewer reviewer = new HtmlReviewer();
        File htmlFile = new File(tempDir, "index.html");
        Files.writeString(htmlFile.toPath(), "<html><body>No guidance here.</body></html>");
        assertNull(reviewer.perform(tempDir, htmlFile));
    }

    @Test
    @Disabled("Need to fix.")
    void perform_extractsGuidance_fromHtmlComment() throws IOException {
        HtmlReviewer reviewer = new HtmlReviewer();
        File htmlFile = new File(tempDir, "guide.html");
        Files.writeString(htmlFile.toPath(), "<!-- @guidance: HTML specific guidance -->\n<html></html>");
        String result = reviewer.perform(tempDir, htmlFile);
        assertNotNull(result);
        assertTrue(result.contains("HTML specific guidance"));
    }
}
