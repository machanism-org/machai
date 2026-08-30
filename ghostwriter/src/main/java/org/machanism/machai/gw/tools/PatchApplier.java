package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies unified and simplified search-and-replace diff patches to text files.
 *
 * <p>The utility parses patch hunks, locates their original context near the
 * position indicated by a unified-diff header, and writes the resulting lines
 * using the caller-supplied character set. It rejects patches that cannot be
 * matched, make no changes, or would erase a nonempty file.</p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public final class PatchApplier {

    /** Prevents instantiation of this static utility class. */
    private PatchApplier() {
        // Sonar java:S1118: utility methods are static, so construction is prohibited.
    }

    /**
     * Parses one hunk line into its operation and unprefixed content.
     *
     * @param hunkLine raw patch line
     * @return parsed patch line; unprefixed input is treated as context
     */
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

    /**
     * Advances beyond a context line, searching forward when its expected
     * position no longer matches exactly.
     *
     * @param lines modified file lines
     * @param content expected context content
     * @param index current line index
     * @return index immediately after the matching or expected context line
     */
    private static int advancePastContext(List<String> lines, String content, int index) {
        if (index < lines.size() && lines.get(index).equals(content)) {
            return index + 1;
        }
        int nextIndex = indexOfLine(lines, content, index + 1);
        return nextIndex == -1 ? index + 1 : nextIndex + 1;
    }

    /**
     * Removes a line at the current patch position.
     *
     * @param lines modified file lines
     * @param index index of the line to remove
     * @throws IOException if the removal is beyond the available file context
     */
    private static void removeLine(List<String> lines, int index) throws IOException {
        if (index >= lines.size()) {
            throw new IOException("Attempted to delete line past end of file context at index: " + index);
        }
        lines.remove(index);
    }

    /** Represents a single context, addition, or removal line in a patch hunk. */
    private static final class PatchLine {
        /** Patch operation: space for context, plus for addition, minus for removal. */
        private final char operation;
        /** Line content without its patch-operation prefix. */
        private final String content;

        /**
         * Creates a parsed patch line.
         *
         * @param operation patch operation
         * @param content unprefixed line content
         */
        private PatchLine(char operation, String content) {
            this.operation = operation;
            this.content = content;
        }
    }

    /** Represents a patch hunk and its preferred zero-based starting position. */
    private static final class Hunk {
        /** Expected zero-based location of the original hunk content. */
        private final int expectedStart;
        /** Raw lines that comprise the hunk. */
        private final List<String> lines;

        /**
         * Creates a patch hunk.
         *
         * @param expectedStart preferred zero-based start position
         * @param lines raw hunk lines
         */
        private Hunk(int expectedStart, List<String> lines) {
            this.expectedStart = expectedStart;
            this.lines = lines;
        }
    }

    /**
     * Applies a unified or simplified diff patch to a file.
     *
     * <p>Standard unified-diff headers, such as {@code @@ -1,5 +1,6 @@}, and
     * simplified {@code @@} headers are supported.</p>
     *
     * @param file target file to patch
     * @param patchLines lines from the patch file
     * @param charset charset for reading and writing the file
     * @throws IOException if file operations fail, a hunk cannot be applied, or
     *                     validation rejects the resulting content
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

    /**
     * Reads the hunk beginning at a patch header.
     *
     * @param patchLines complete patch lines
     * @param headerIndex index of the header
     * @param offsetDelta cumulative line-count delta from preceding hunks
     * @return parsed hunk
     */
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

    /**
     * Determines whether a line begins another hunk or file section.
     *
     * @param line candidate patch line
     * @return {@code true} when the line is a recognized boundary
     */
    private static boolean isHunkBoundary(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("@@") || trimmed.startsWith("***");
    }

    /**
     * Calculates the preferred result-file position from a unified-diff header.
     *
     * @param header hunk header
     * @param offsetDelta cumulative prior line-count delta
     * @return zero-based preferred position, or zero for a simplified or invalid header
     */
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

    /**
     * Locates and applies a hunk to the mutable result lines.
     *
     * @param resultLines mutable result file lines
     * @param hunk parsed hunk
     * @param header original hunk header for diagnostics
     * @throws IOException if no matching hunk context is found
     */
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

    /**
     * Applies one parsed hunk line at the current result-file index.
     *
     * @param lines mutable result lines
     * @param patchLine parsed operation and content
     * @param index current result-file index
     * @return index at which the next operation should occur
     * @throws IOException if a removal exceeds available context
     */
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

    /**
     * Counts addition lines in a hunk.
     *
     * @param lines raw hunk lines
     * @return number of additions
     */
    private static int countAdded(List<String> lines) {
        return (int) lines.stream().filter(line -> line.startsWith("+")).count();
    }

    /**
     * Counts removal lines in a hunk.
     *
     * @param lines raw hunk lines
     * @return number of removals
     */
    private static int countRemoved(List<String> lines) {
        return (int) lines.stream().filter(line -> line.startsWith("-")).count();
    }

    /**
     * Creates the target file's parent directory when it does not exist.
     *
     * @param path target path
     * @throws IOException if the directory cannot be created
     */
    private static void createParentDirectory(Path path) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
    }

    /**
     * Rejects unsafe or ineffective patch results.
     *
     * @param original original file lines
     * @param modified patched file lines
     * @param fileName file name for diagnostics
     * @throws IOException if the patch erases a nonempty file or makes no change
     */
    private static void validatePatchResult(List<String> original, List<String> modified, String fileName)
            throws IOException {
        if (modified.isEmpty() && !original.isEmpty()) {
            throw new IOException("Validation failed: Patch application wiped file content completely: " + fileName);
        }
        if (modified.equals(original)) {
            throw new IOException("Validation failed: Patch resulted in zero changes to the original file: " + fileName);
        }
    }

    /**
     * Finds the first complete original-context match nearest the expected start.
     *
     * @param fileLines candidate file lines
     * @param hunkLines raw hunk lines
     * @param expectedStart preferred zero-based position
     * @return matching index, or {@code -1} when no match exists
     */
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

    /**
     * Extracts the original-file lines required by a hunk.
     *
     * @param hunkLines raw hunk lines
     * @return context and removal content in original-file order
     */
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

    /**
     * Checks whether expected lines match the file at an index.
     *
     * @param fileLines file lines
     * @param expectedLines expected consecutive lines
     * @param startOffset candidate start index
     * @return {@code true} if all expected lines match in bounds
     */
    private static boolean matchesAt(List<String> fileLines, List<String> expectedLines, int startOffset) {
        return startOffset >= 0 && startOffset + expectedLines.size() <= fileLines.size()
                && matchLines(fileLines, expectedLines, startOffset);
    }

    /**
     * Compares expected lines to a consecutive segment of file lines.
     *
     * @param fileLines file lines
     * @param expectedLines expected lines
     * @param startOffset start of the candidate segment
     * @return {@code true} if every line matches
     */
    private static boolean matchLines(List<String> fileLines, List<String> expectedLines, int startOffset) {
        for (int index = 0; index < expectedLines.size(); index++) {
            if (!fileLines.get(startOffset + index).equals(expectedLines.get(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds a line at or after the supplied index.
     *
     * @param lines lines to search
     * @param expectedLine line to locate
     * @param startIndex first index to examine
     * @return matching index, or {@code -1} when absent
     */
    private static int indexOfLine(List<String> lines, String expectedLine, int startIndex) {
        for (int index = Math.max(0, startIndex); index < lines.size(); index++) {
            if (lines.get(index).equals(expectedLine)) {
                return index;
            }
        }
        return -1;
    }
}
