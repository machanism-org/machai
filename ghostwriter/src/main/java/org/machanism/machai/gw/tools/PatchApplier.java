package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to apply unified diff patches to text files.
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public final class PatchApplier {

    private PatchApplier() {
        // Sonar java:S1118: utility methods are static, so construction is prohibited.
    }

    private static PatchLine parsePatchLine(String hunkLine) {
        if (hunkLine.isEmpty()) {
            return new PatchLine(' ', "");
        }
        char operation = hunkLine.charAt(0);
        if (operation == ' ' || operation == '+' || operation == '-') {
            return new PatchLine(operation, hunkLine.substring(1));
        }
        return new PatchLine(' ', hunkLine);
    }

    private static int advancePastContext(List<String> lines, String content, int index) {
        if (index < lines.size() && lines.get(index).equals(content)) {
            return index + 1;
        }
        int nextIndex = indexOfLine(lines, content, index + 1);
        return nextIndex == -1 ? index + 1 : nextIndex + 1;
    }

    private static void removeLine(List<String> lines, int index) throws IOException {
        if (index >= lines.size()) {
            throw new IOException("Attempted to delete line past end of file context at index: " + index);
        }
        lines.remove(index);
    }

    private static final class PatchLine {
        private final char operation;
        private final String content;

        private PatchLine(char operation, String content) {
            this.operation = operation;
            this.content = content;
        }
    }

    private static final class Hunk {
        private final int expectedStart;
        private final List<String> lines;

        private Hunk(int expectedStart, List<String> lines) {
            this.expectedStart = expectedStart;
            this.lines = lines;
        }
    }

    /**
     * Applies a unified or simplified diff patch to a file.
     * Supports both standard unified diff headers (e.g. "@@ -1,5 +1,6 @@")
     * and simplified search-and-replace headers (e.g. "@@").
     *
     * @param file target file to patch
     * @param patchLines lines from the patch file
     * @param charset charset for reading/writing the file
     * @throws IOException if file operations fail or patch cannot be applied
     */
    public static void applyPatch(File file, List<String> patchLines, Charset charset) throws IOException {
        Path path = file.toPath();
        List<String> originalLines = Files.exists(path) ? Files.readAllLines(path, charset) : new ArrayList<>();
        List<String> resultLines = new ArrayList<>(originalLines);
        int patchIndex = 0;
        int offsetDelta = 0;
        while (patchIndex < patchLines.size()) {
            String line = patchLines.get(patchIndex).trim();
            if (line.startsWith("***")) {
                patchIndex++;
            } else if (line.startsWith("@@")) {
                Hunk hunk = readHunk(patchLines, patchIndex, offsetDelta);
                applyHunk(resultLines, hunk, line);
                offsetDelta += countAdded(hunk.lines) - countRemoved(hunk.lines);
                patchIndex += hunk.lines.size() + 1;
            } else {
                patchIndex++;
            }
        }
        validatePatchResult(originalLines, resultLines, file.getName());
        createParentDirectory(path);
        Files.write(path, resultLines, charset);
    }

    private static Hunk readHunk(List<String> patchLines, int headerIndex, int offsetDelta) {
        String header = patchLines.get(headerIndex).trim();
        List<String> hunkLines = new ArrayList<>();
        int index = headerIndex + 1;
        while (index < patchLines.size() && !isHunkBoundary(patchLines.get(index))) {
            String hunkLine = patchLines.get(index++);
            if (!hunkLine.startsWith("\\")) {
                hunkLines.add(hunkLine);
            }
        }
        return new Hunk(getExpectedStart(header, offsetDelta), hunkLines);
    }

    private static boolean isHunkBoundary(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("@@") || trimmed.startsWith("***");
    }

    private static int getExpectedStart(String header, int offsetDelta) {
        String[] parts = header.split(" ");
        if (parts.length < 3 || !parts[1].startsWith("-")) {
            return 0;
        }
        try {
            int oldStart = Integer.parseInt(parts[1].substring(1).split(",")[0]);
            return (oldStart > 0 ? oldStart - 1 : oldStart) + offsetDelta;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static void applyHunk(List<String> resultLines, Hunk hunk, String header) throws IOException {
        int matchIndex = findHunkStart(resultLines, hunk.lines, hunk.expectedStart);
        if (matchIndex == -1) {
            throw new IOException("Failed to find matching context for patch hunk: " + header);
        }
        int fileIndex = matchIndex;
        for (String hunkLine : hunk.lines) {
            fileIndex = applyPatchLine(resultLines, parsePatchLine(hunkLine), fileIndex);
        }
    }

    private static int applyPatchLine(List<String> lines, PatchLine patchLine, int index) throws IOException {
        if (patchLine.operation == ' ') {
            return advancePastContext(lines, patchLine.content, index);
        }
        if (patchLine.operation == '-') {
            removeLine(lines, index);
            return index;
        }
        if (patchLine.operation == '+') {
            lines.add(index, patchLine.content);
            return index + 1;
        }
        return index;
    }

    private static int countAdded(List<String> lines) {
        return (int) lines.stream().filter(line -> line.startsWith("+")).count();
    }

    private static int countRemoved(List<String> lines) {
        return (int) lines.stream().filter(line -> line.startsWith("-")).count();
    }

    private static void createParentDirectory(Path path) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
    }

    private static void validatePatchResult(List<String> original, List<String> modified, String fileName)
            throws IOException {
        if (modified.isEmpty() && !original.isEmpty()) {
            throw new IOException("Validation failed: Patch application wiped file content completely: " + fileName);
        }
        if (modified.equals(original)) {
            throw new IOException("Validation failed: Patch resulted in zero changes to the original file: " + fileName);
        }
    }

    private static int findHunkStart(List<String> fileLines, List<String> hunkLines, int expectedStart) {
        List<String> expectedOriginal = expectedOriginalLines(hunkLines);
        if (expectedOriginal.isEmpty()) {
            return expectedStart;
        }
        int maxSearchOffset = fileLines.size() + Math.max(expectedStart, expectedOriginal.size());
        for (int offset = 0; offset <= maxSearchOffset; offset++) {
            int forwardIndex = expectedStart + offset;
            if (matchesAt(fileLines, expectedOriginal, forwardIndex)) {
                return forwardIndex;
            }
            int backwardIndex = expectedStart - offset;
            if (offset > 0 && matchesAt(fileLines, expectedOriginal, backwardIndex)) {
                return backwardIndex;
            }
        }
        return -1;
    }

    private static List<String> expectedOriginalLines(List<String> hunkLines) {
        List<String> expectedOriginal = new ArrayList<>();
        for (String hunkLine : hunkLines) {
            PatchLine patchLine = parsePatchLine(hunkLine);
            if (patchLine.operation != '+') {
                expectedOriginal.add(patchLine.content);
            }
        }
        return expectedOriginal;
    }

    private static boolean matchesAt(List<String> fileLines, List<String> expectedLines, int startOffset) {
        return startOffset >= 0 && startOffset + expectedLines.size() <= fileLines.size()
                && matchLines(fileLines, expectedLines, startOffset);
    }

    private static boolean matchLines(List<String> fileLines, List<String> expectedLines, int startOffset) {
        for (int index = 0; index < expectedLines.size(); index++) {
            if (!fileLines.get(startOffset + index).equals(expectedLines.get(index))) {
                return false;
            }
        }
        return true;
    }

    private static int indexOfLine(List<String> lines, String expectedLine, int startIndex) {
        for (int index = Math.max(0, startIndex); index < lines.size(); index++) {
            if (lines.get(index).equals(expectedLine)) {
                return index;
            }
        }
        return -1;
    }
}
