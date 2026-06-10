package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link StringBuilder}-like helper that retains only the last
 * {@code maxSize} characters.
 *
 * <p>
 * This utility is typically used when capturing potentially unbounded output
 * (for example, process stdout/stderr) while keeping a deterministic upper
 * bound on memory usage.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class LimitedStringBuilder {
	private final int maxSize;
	private final StringBuilder sb;
	private boolean truncated;
	private final String commandId;
	private File projectDir;
	private int totalLength;

	/**
	 * Creates a builder that keeps at most {@code maxSize} characters.
	 *
	 * @param maxSize   maximum number of characters to retain; must be positive
	 * @param commandId
	 * @throws IllegalArgumentException if {@code maxSize} is not positive
	 */
	public LimitedStringBuilder(int maxSize, String commandId, File projectDir) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize must be positive");
		}
		this.maxSize = maxSize;
		this.sb = new StringBuilder();
		this.truncated = false;
		this.commandId = commandId;
		this.projectDir = projectDir;
	}

	/**
	 * Appends text to this builder, trimming any excess characters from the start
	 * to stay within the configured maximum size. Also appends the text to a log
	 * file at: {projectDir}/.machai/command-log/{commandId}.log
	 *
	 * @param text text to append; ignored if {@code null}
	 * @return this instance for fluent chaining
	 */
	public LimitedStringBuilder append(String text) {
		if (text == null) {
			return this;
		}
		totalLength = getTotalLength() + text.length();
		sb.append(text);

		int excess = sb.length() - maxSize;
		if (excess > 0) {
			sb.delete(0, excess);
			truncated = true;
		}

		if (projectDir != null && commandId != null) {
			Path logPath = getCommandLogPath(commandId);
			try {
				Files.createDirectories(logPath.getParent());
				Files.write(logPath, text.getBytes(StandardCharsets.UTF_8),
						Files.exists(logPath) ? java.nio.file.StandardOpenOption.APPEND
								: java.nio.file.StandardOpenOption.CREATE);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		return this;
	}

	public static Path getCommandLogPath(String commandId) {
		String tempDir = System.getProperty("java.io.tmpdir");
		Path logDir = new File(tempDir, "gw-command-logs").toPath();
		try {
			Files.createDirectories(logDir);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create log directory: " + logDir, e);
		}
		return logDir.resolve(commandId + ".log");
	}

	/**
	 * Returns the retained content.
	 *
	 * <p>
	 * If truncation occurred, a prefix is added to indicate that earlier content
	 * was discarded.
	 * </p>
	 *
	 * @return retained text (possibly with a truncation prefix)
	 */
	public String getTail() {
		return sb.toString();
	}

	/**
	 * Returns the number of characters currently retained.
	 *
	 * @return retained length
	 */
	public int length() {
		return sb.length();
	}

	/**
	 * Clears the retained content.
	 */
	public void clear() {
		sb.setLength(0);
		truncated = false;
	}

	/**
	 * @return the totalLength
	 */
	public int getTotalLength() {
		return totalLength;
	}

	public Map<String, Object> getReport() {
		Map<String, Object> report = new HashMap<>();
		report.put("tail", sb.toString());
		report.put("totalLength", totalLength);
		report.put("truncated", truncated);

		return report;
	}

}