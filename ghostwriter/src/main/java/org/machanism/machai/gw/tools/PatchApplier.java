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
 */
public class PatchApplier {

    /**
     * Applies a unified or simplified diff patch to a file.
     * Supports both standard unified diff headers (e.g. "@@ -1,5 +1,6 @@")
     * and simplified search-and-replace headers (e.g. "@@").
     *
     * @param file       The target file to patch.
     * @param patchLines List of lines from the patch file.
     * @param charset    Charset for reading/writing the file.
     * @throws IOException if file operations fail or patch cannot be applied.
     */
    public static void applyPatch(File file, List<String> patchLines, Charset charset) throws IOException {
        Path path = file.toPath();
        List<String> originalLines = Files.exists(path)
                ? Files.readAllLines(path, charset)
                : new ArrayList<>();

        List<String> resultLines = new ArrayList<>(originalLines);

        int patchIndex = 0;
        int offsetDelta = 0;

        while (patchIndex < patchLines.size()) {
            String line = patchLines.get(patchIndex).trim();

            // Skip custom wrappers like *** Begin Patch, *** Update File, etc.
            if (line.startsWith("***")) {
                patchIndex++;
                continue;
            }

            if (line.startsWith("@@")) {
                // Parse standard vs simplified range
                String[] parts = line.split(" ");
                int oldStart = 0;
                boolean hasRange = false;

                if (parts.length >= 3 && parts[1].startsWith("-")) {
                    String oldRangeStr = parts[1];
                    String[] oldRange = oldRangeStr.substring(1).split(",");
                    try {
                        oldStart = Integer.parseInt(oldRange[0]);
                        if (oldStart > 0) {
                            oldStart--; // Convert to 0-based index
                        }
                        hasRange = true;
                    } catch (NumberFormatException ignored) {
                        // Keep hasRange = false, we will fallback to search
                    }
                }

                // Collect hunk lines, ignoring no-newline warnings
                List<String> hunkLines = new ArrayList<>();
                patchIndex++;
                while (patchIndex < patchLines.size()) {
                    String hunkLine = patchLines.get(patchIndex);
                    String trimmedHunk = hunkLine.trim();
                    if (trimmedHunk.startsWith("@@") || trimmedHunk.startsWith("***")) {
                        break; // Stop collecting if we hit a new hunk or end wrapper
                    }
                    if (!hunkLine.startsWith("\\")) { // Ignore "\ No newline..."
                        hunkLines.add(hunkLine);
                    }
                    patchIndex++;
                }

                // If we have a range, compute expected index. Otherwise search from beginning (0).
                int expectedStart = hasRange ? (oldStart + offsetDelta) : 0;

                // Find the matching context in the file
                int matchIndex = findHunkStart(resultLines, hunkLines, expectedStart);
                if (matchIndex == -1) {
                    throw new IOException("Failed to find matching context for patch hunk: " + line);
                }

                // Apply the hunk using stable index tracking
                int fileIndex = matchIndex;
                int added = 0;
                int removed = 0;
                for (String hunkLine : hunkLines) {
                    char op;
                    String content;
                    if (hunkLine.isEmpty()) {
                        op = ' ';
                        content = "";
                    } else {
                        char firstChar = hunkLine.charAt(0);
                        if (firstChar == ' ' || firstChar == '+' || firstChar == '-') {
                            op = firstChar;
                            content = hunkLine.substring(1);
                        } else {
                            op = ' ';
                            content = hunkLine;
                        }
                    }

                    if (op == ' ') {
                        fileIndex++;
                    } else if (op == '-') {
                        if (fileIndex < resultLines.size()) {
                            resultLines.remove(fileIndex);
                            removed++;
                        } else {
                            throw new IOException("Attempted to delete line past end of file context at index: " + fileIndex);
                        }
                    } else if (op == '+') {
                        resultLines.add(fileIndex, content);
                        fileIndex++;
                        added++;
                    }
                }
                offsetDelta += (added - removed);
            } else {
                patchIndex++;
            }
        }

        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, resultLines, charset);
    }
    
    private static int findHunkStart(List<String> fileLines, List<String> hunkLines, int expectedStart) {
        List<String> expectedOriginal = new ArrayList<>();
        for (String hunkLine : hunkLines) {
            if (hunkLine.isEmpty()) {
                expectedOriginal.add("");
            } else if (hunkLine.charAt(0) == ' ' || hunkLine.charAt(0) == '-') {
                expectedOriginal.add(hunkLine.substring(1));
            }
        }

        if (expectedOriginal.isEmpty()) {
            return expectedStart;
        }

        int maxSearchOffset = Math.max(fileLines.size(), expectedOriginal.size());
        
        for (int i = 0; i <= maxSearchOffset; i++) {
            int forwardIndex = expectedStart + i;
            if (forwardIndex >= 0 && forwardIndex + expectedOriginal.size() <= fileLines.size()) {
                if (matchLines(fileLines, expectedOriginal, forwardIndex)) {
                    return forwardIndex;
                }
            }
            int backwardIndex = expectedStart - i;
            if (i > 0 && backwardIndex >= 0 && backwardIndex + expectedOriginal.size() <= fileLines.size()) {
                if (matchLines(fileLines, expectedOriginal, backwardIndex)) {
                    return backwardIndex;
                }
            }
        }
        return -1;
    }

    private static boolean matchLines(List<String> fileLines, List<String> expectedLines, int startOffset) {
        for (int j = 0; j < expectedLines.size(); j++) {
            if (!fileLines.get(startOffset + j).equals(expectedLines.get(j))) {
                return false;
            }
        }
        return true;
    }
}
