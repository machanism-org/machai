package org.machanism.machai.gw.reviewer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TextReviewer}.
 * <p>
 * Tests review and guidance extraction for plain text files.
 */
class TextReviewerTest {

    @TempDir
    File tempDir;

    @Test
    void perform_returnsNull_whenNoGuidanceFile() throws IOException {
        TextReviewer reviewer = new TextReviewer();
        reviewer.setDirGuidanceMap(new HashMap<>());
        File txtFile = new File(tempDir, "foo.txt");
        Files.writeString(txtFile.toPath(), "Just text");
        assertNull(reviewer.perform(tempDir, txtFile));
    }

    @Test
    void perform_guidanceFile_extractsAndUpdatesMap() throws IOException {
        TextReviewer reviewer = new TextReviewer();
        Map<String, String> map = new HashMap<>();
        reviewer.setDirGuidanceMap(map);
        File txtFile = new File(tempDir, "@guidance.txt");
        Files.writeString(txtFile.toPath(), "Guidance from dir.");
        String result = reviewer.perform(tempDir, txtFile);
        assertNotNull(result);
        assertTrue(result.contains("Guidance from dir."));
        assertFalse(map.isEmpty());
    }
}
