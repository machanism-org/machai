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
						// Fallback to searching from start
					}
				}

				// Collect hunk lines, ignoring no-newline warnings
				List<String> hunkLines = new ArrayList<>();
				patchIndex++;
				while (patchIndex < patchLines.size()) {
					String hunkLine = patchLines.get(patchIndex);
					String trimmedHunk = hunkLine.trim();
					if (trimmedHunk.startsWith("@@") || trimmedHunk.startsWith("***")) {
						break;
					}
					if (!hunkLine.startsWith("\\")) {
						hunkLines.add(hunkLine);
					}
					patchIndex++;
				}

				// If we have a range, compute expected index. Otherwise search from beginning
				// (0).
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
					if (hunkLine.isEmpty()) {
						op = ' ';
					} else {
						op = hunkLine.charAt(0);
					}
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
						if (fileIndex < resultLines.size() && resultLines.get(fileIndex).equals(content)) {
							fileIndex++;
						} else {
							int nextIndex = indexOfLine(resultLines, content, fileIndex + 1);
							fileIndex = nextIndex == -1 ? fileIndex + 1 : nextIndex + 1;
						}
					} else if (op == '-') {
						if (fileIndex < resultLines.size()) {
							resultLines.remove(fileIndex);
							removed++;
						} else {
							throw new IOException(
									"Attempted to delete line past end of file context at index: " + fileIndex);
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

		validatePatchResult(originalLines, resultLines, file.getName());

		if (path.getParent() != null && !Files.exists(path.getParent())) {
			Files.createDirectories(path.getParent());
		}
		Files.write(path, resultLines, charset);
	}

	/**
	 * Asserts structural sanity on the patched code structure before writing to
	 * disk.
	 *
	 * @param original lines read before patching
	 * @param modified lines produced by patching
	 * @param fileName name used in validation errors
	 * @throws IOException if the patch removes all existing content or makes no
	 *                     changes
	 */
	private static void validatePatchResult(List<String> original, List<String> modified, String fileName)
			throws IOException {
		// 1. Extreme Reduction Check
		if (modified.isEmpty() && !original.isEmpty()) {
			throw new IOException("Validation failed: Patch application wiped file content completely: " + fileName);
		}

		// 2. Exact Duplication Check (Check if unchanged)
		if (modified.equals(original)) {
			throw new IOException(
					"Validation failed: Patch resulted in zero changes to the original file: " + fileName);
		}
	}

	/**
	 * Locates the input context for a patch hunk near its expected position.
	 *
	 * @param fileLines current file lines
	 * @param hunkLines patch hunk lines
	 * @param expectedStart preferred zero-based start position
	 * @return matching zero-based start position, or {@code -1} when none exists
	 */
	private static int findHunkStart(List<String> fileLines, List<String> hunkLines, int expectedStart) {
		List<String> expectedOriginal = new ArrayList<>();
		for (String hunkLine : hunkLines) {
			if (hunkLine.isEmpty()) {
				expectedOriginal.add("");
			} else if (hunkLine.charAt(0) == ' ') {
				expectedOriginal.add(hunkLine.substring(1));
			} else if (hunkLine.charAt(0) == '-') {
				expectedOriginal.add(hunkLine.substring(1));
			} else if (hunkLine.charAt(0) != '+') {
				expectedOriginal.add(hunkLine);
			}
		}

		if (expectedOriginal.isEmpty()) {
			return expectedStart;
		}

		int maxSearchOffset = fileLines.size() + Math.max(expectedStart, expectedOriginal.size());

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

	/**
	 * Determines whether expected lines exactly match a file-line range.
	 *
	 * @param fileLines current file lines
	 * @param expectedLines lines expected at the position
	 * @param startOffset zero-based position to test
	 * @return {@code true} when every expected line matches
	 */
	private static boolean matchLines(List<String> fileLines, List<String> expectedLines, int startOffset) {
		for (int j = 0; j < expectedLines.size(); j++) {
			if (!fileLines.get(startOffset + j).equals(expectedLines.get(j))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Finds a line at or after a specified index.
	 *
	 * @param lines lines to search
	 * @param expectedLine line value to find
	 * @param startIndex first index to inspect
	 * @return matching index, or {@code -1} when no line matches
	 */
	private static int indexOfLine(List<String> lines, String expectedLine, int startIndex) {
		for (int i = Math.max(0, startIndex); i < lines.size(); i++) {
			if (lines.get(i).equals(expectedLine)) {
				return i;
			}
		}
		return -1;
	}
}
