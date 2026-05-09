package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PatchApplierTest {

    @TempDir
    Path tempDir;

    @Test
    void testApplyPatch() throws IOException {
        Path targetFile = tempDir.resolve("target.txt");
        List<String> originalLines = Arrays.asList(
                "Line 1",
                "Line 2",
                "Line 3",
                "Line 4"
        );
        Files.write(targetFile, originalLines, StandardCharsets.UTF_8);

        List<String> patchLines = Arrays.asList(
                "--- target.txt",
                "+++ target.txt",
                "@@ -1,4 +1,4 @@",
                " Line 1",
                "-Line 2",
                "+Line 2 updated",
                " Line 3",
                " Line 4"
        );

        PatchApplier.applyPatch(targetFile.toString(), patchLines, StandardCharsets.UTF_8);

        List<String> expectedLines = Arrays.asList(
                "Line 1",
                "Line 2 updated",
                "Line 3",
                "Line 4"
        );
        List<String> resultLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        assertEquals(expectedLines, resultLines);
    }

    @Test
    void testApplyPatchWithMultipleHunks() throws IOException {
        Path targetFile = tempDir.resolve("multi.txt");
        List<String> originalLines = Arrays.asList(
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"
        );
        Files.write(targetFile, originalLines, StandardCharsets.UTF_8);

        List<String> patchLines = Arrays.asList(
                "@@ -1,3 +1,4 @@",
                " A",
                "-B",
                "+B1",
                "+B2",
                " C",
                "@@ -7,3 +8,3 @@",
                " G",
                "-H",
                "+H1",
                " I"
        );

        PatchApplier.applyPatch(targetFile.toString(), patchLines, StandardCharsets.UTF_8);

        List<String> expectedLines = Arrays.asList(
                "A", "B1", "B2", "C", "D", "E", "F", "G", "H1", "I", "J"
        );
        List<String> resultLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        assertEquals(expectedLines, resultLines);
    }

    @Test
    void testFuzzyMatch() throws IOException {
        Path targetFile = tempDir.resolve("fuzzy.txt");
        // Simulated offset in the file
        List<String> originalLines = Arrays.asList(
                "Header", "Header2", "A", "B", "C"
        );
        Files.write(targetFile, originalLines, StandardCharsets.UTF_8);

        // Patch was generated without headers, expecting A B C at line 1
        List<String> patchLines = Arrays.asList(
                "@@ -1,3 +1,3 @@",
                " A",
                "-B",
                "+B1",
                " C"
        );

        PatchApplier.applyPatch(targetFile.toString(), patchLines, StandardCharsets.UTF_8);

        List<String> expectedLines = Arrays.asList(
                "Header", "Header2", "A", "B1", "C"
        );
        List<String> resultLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        assertEquals(expectedLines, resultLines);
    }

    @Test
    void testMissingSpaceContext() throws IOException {
        Path targetFile = tempDir.resolve("nospace.txt");
        List<String> originalLines = Arrays.asList(
                "A", "B", "C"
        );
        Files.write(targetFile, originalLines, StandardCharsets.UTF_8);

        List<String> patchLines = Arrays.asList(
                "@@ -1,3 +1,3 @@",
                "A", // Missing space
                "-B",
                "+B1",
                "C" // Missing space
        );

        PatchApplier.applyPatch(targetFile.toString(), patchLines, StandardCharsets.UTF_8);

        List<String> expectedLines = Arrays.asList(
                "A", "B1", "C"
        );
        List<String> resultLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        assertEquals(expectedLines, resultLines);
    }

    @Test
    void testEmptyFilePatch() throws IOException {
        Path targetFile = tempDir.resolve("empty.txt");
        // File does not exist yet

        List<String> patchLines = Arrays.asList(
                "@@ -0,0 +1,2 @@",
                "+A",
                "+B"
        );

        PatchApplier.applyPatch(targetFile.toString(), patchLines, StandardCharsets.UTF_8);

        List<String> expectedLines = Arrays.asList("A", "B");
        List<String> resultLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        assertEquals(expectedLines, resultLines);
    }

    @Test
    void testEmptyLinesInPatch() throws IOException {
        Path targetFile = tempDir.resolve("emptylines.txt");
        List<String> originalLines = Arrays.asList(
                "A", "", "B"
        );
        Files.write(targetFile, originalLines, StandardCharsets.UTF_8);

        List<String> patchLines = Arrays.asList(
                "@@ -1,3 +1,3 @@",
                " A",
                "", // Empty line context
                "-B",
                "+B1"
        );

        PatchApplier.applyPatch(targetFile.toString(), patchLines, StandardCharsets.UTF_8);

        List<String> expectedLines = Arrays.asList("A", "", "B1");
        List<String> resultLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        assertEquals(expectedLines, resultLines);
    }

    @Test
    void testContentMatchReplacement() throws IOException {
        Path targetFile = tempDir.resolve("content_match.txt");
        List<String> originalLines = Arrays.asList(
                "Line 1", "Line 2", "Target A", "Target B", "Line 5"
        );
        Files.write(targetFile, originalLines, StandardCharsets.UTF_8);

        List<String> patchLines = Arrays.asList(
                "@@ -100,2 +100,2 @@",
                " Target A",
                "-Target B",
                "+Target B updated"
        );

        PatchApplier.applyPatch(targetFile.toString(), patchLines, StandardCharsets.UTF_8);

        List<String> expectedLines = Arrays.asList("Line 1", "Line 2", "Target A", "Target B updated", "Line 5");
        List<String> resultLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        assertEquals(expectedLines, resultLines);
    }

    @Test
    void testContentMatchAddition() throws IOException {
        Path targetFile = tempDir.resolve("content_match_add.txt");
        List<String> originalLines = Arrays.asList(
                "Line 1", "Line 2", "Target A", "Target B", "Line 5"
        );
        Files.write(targetFile, originalLines, StandardCharsets.UTF_8);

        List<String> patchLines = Arrays.asList(
                "@@ -100,2 +100,3 @@",
                " Target A",
                " Target B",
                "+Target C"
        );

        PatchApplier.applyPatch(targetFile.toString(), patchLines, StandardCharsets.UTF_8);

        List<String> expectedLines = Arrays.asList("Line 1", "Line 2", "Target A", "Target B", "Target C", "Line 5");
        List<String> resultLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        assertEquals(expectedLines, resultLines);
    }
}
