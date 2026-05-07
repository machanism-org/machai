package org.machanism.machai.gw.tools;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to apply unified diff patches to text files.
 */
public class PatchApplier {

    /**
     * Applies a unified diff patch to a file.
     *
     * @param filePath   Path to the original file.
     * @param patchLines List of lines from the patch file.
     * @param charset    Charset for reading/writing the file.
     * @throws IOException if file operations fail or patch cannot be applied.
     */
    public static void applyPatch(String filePath, List<String> patchLines, Charset charset) throws IOException {
        Path path = Paths.get(filePath);
        List<String> originalLines = Files.exists(path)
                ? Files.readAllLines(path, charset)
                : new ArrayList<>();

        List<String> resultLines = new ArrayList<>(originalLines);

        int patchIndex = 0;
        int offsetDelta = 0;

        while (patchIndex < patchLines.size()) {
            String line = patchLines.get(patchIndex);
            if (line.startsWith("@@")) {
                // Parse hunk header: @@ -start,len +start,len @@
                String[] parts = line.split(" ");
                if (parts.length < 3) {
                    patchIndex++;
                    continue;
                }

                String oldRangeStr = parts[1];
                if (!oldRangeStr.startsWith("-")) {
                    patchIndex++;
                    continue;
                }

                String[] oldRange = oldRangeStr.substring(1).split(",");
                int oldStart = Integer.parseInt(oldRange[0]);
                if (oldStart > 0) {
                    oldStart--; // 0-based index
                }

                // Collect hunk lines
                List<String> hunkLines = new ArrayList<>();
                patchIndex++;
                while (patchIndex < patchLines.size() && !patchLines.get(patchIndex).startsWith("@@")) {
                    hunkLines.add(patchLines.get(patchIndex));
                    patchIndex++;
                }

                int expectedStart = oldStart + offsetDelta;

                // Find the matching context in the file
                int matchIndex = findHunkStart(resultLines, hunkLines, expectedStart);
                if (matchIndex == -1) {
                    throw new IOException("Failed to find matching context for patch hunk: " + line);
                }

                // Apply the hunk
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
                        if (firstChar == ' ' || firstChar == '+' || firstChar == '-' || firstChar == '\\') {
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

    /**
     * Finds the start index in the file where the hunk should be applied.
     */
    private static int findHunkStart(List<String> fileLines, List<String> hunkLines, int expectedStart) {
        List<String> originalLines = new ArrayList<>();
        for (String hunkLine : hunkLines) {
            char op;
            String content;
            if (hunkLine.isEmpty()) {
                op = ' ';
                content = "";
            } else {
                char firstChar = hunkLine.charAt(0);
                if (firstChar == ' ' || firstChar == '+' || firstChar == '-' || firstChar == '\\') {
                    op = firstChar;
                    content = hunkLine.substring(1);
                } else {
                    op = ' ';
                    content = hunkLine;
                }
            }
            if (op == ' ' || op == '-') {
                originalLines.add(content);
            }
        }
        if (originalLines.isEmpty()) {
            return Math.max(0, Math.min(expectedStart, fileLines.size()));
        }

        expectedStart = Math.max(0, Math.min(expectedStart, fileLines.size()));

        if (isMatch(fileLines, originalLines, expectedStart)) {
            return expectedStart;
        }

        int maxOffset = Math.max(fileLines.size(), expectedStart);
        for (int offset = 1; offset <= maxOffset; offset++) {
            if (isMatch(fileLines, originalLines, expectedStart + offset)) {
                return expectedStart + offset;
            }
            if (isMatch(fileLines, originalLines, expectedStart - offset)) {
                return expectedStart - offset;
            }
        }

        return -1;
    }

    private static boolean isMatch(List<String> fileLines, List<String> originalLines, int startIndex) {
        if (startIndex < 0 || startIndex + originalLines.size() > fileLines.size()) {
            return false;
        }
        for (int i = 0; i < originalLines.size(); i++) {
            if (!fileLines.get(startIndex + i).equals(originalLines.get(i))) {
                return false;
            }
        }
        return true;
    }
}
