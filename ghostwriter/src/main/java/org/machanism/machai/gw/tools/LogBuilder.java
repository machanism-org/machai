package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link StringBuilder}-like helper that retains only the last
 * {@code maxSize} characters.
 *
 * <p>
 * This utility is typically used when capturing potentially unbounded output
 * (for example, process stdout/stderr) while keeping a deterministic upper
 * bound on memory usage. It also supports optional persistence of appended
 * content to a log file on disk.
 * </p>
 *
 * <p>
 * The log buffer is truncated from the beginning if the maximum size is
 * exceeded, and a flag is set to indicate truncation. The class also tracks the
 * total number of characters ever appended and the elapsed time since
 * instantiation.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public class LogBuilder {

	/**
	 * Name of the directory where log files are stored.
	 * <p>
	 * This directory is typically used as the parent location for all log files.
	 * </p>
	 */
	public static final String LOGS_DIR_NAME = "gw_logs";

	/**
	 * Standard file extension for log files.
	 * <p>
	 * All log files created by this component or related utilities should use this
	 * extension.
	 * </p>
	 */
	public static final String LOG_EXTENSION = ".log";

	/** Maximum number of characters to retain in the buffer. */
	private final int maxSize;
	/** Internal buffer for retained log content. */
	private final StringBuilder sb;
	/** Flag indicating whether truncation has occurred. */
	private boolean truncated;
	/** Optional log identifier for file persistence. */
	private final String logId;
	/** Optional project directory for log file location. */
	private File projectDir;
	/** Total number of characters ever appended. */
	private int totalLength;
	/** Start time in milliseconds since epoch. */
	private long startTime;

	/**
	 * Creates a builder that keeps at most {@code maxSize} characters.
	 *
	 * @param maxSize    maximum number of characters to retain; must be positive
	 * @param logId      optional log identifier for file persistence
	 * @param projectDir optional project directory for log file location
	 * @throws IllegalArgumentException if {@code maxSize} is not positive
	 */
	public LogBuilder(int maxSize, String logId, File projectDir) {
		startTime = System.currentTimeMillis();
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize must be positive");
		}
		this.maxSize = maxSize;
		this.sb = new StringBuilder();
		this.truncated = false;
		this.logId = logId;
		this.projectDir = projectDir;
	}

	/**
	 * Appends the specified text to the internal log buffer and optionally persists
	 * it to disk.
	 *
	 * <p>
	 * This method updates the internal buffer by adding the provided text. If the
	 * buffer exceeds the configured maximum size ({@code maxSize}), the oldest
	 * content is truncated to maintain the limit. The {@code truncated} flag is set
	 * if truncation occurs.
	 * </p>
	 *
	 * <p>
	 * If both {@code projectDir} and {@code logId} are set, the appended text is
	 * also written to a log file on disk. The log file is created if it does not
	 * exist, or appended to if it does. Parent directories are created as needed.
	 * </p>
	 *
	 * @param text the text to append to the log buffer; if {@code null}, no action
	 *             is taken
	 * @return this {@code LogBuilder} instance for method chaining
	 * @throws IllegalArgumentException if an I/O error occurs while writing to the
	 *                                  log file
	 */
	public LogBuilder append(String text) {
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

		if (projectDir != null && logId != null) {
			Path logPath = getCommandLogPath(logId);
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

	/**
	 * Returns the path to the log file for the given log identifier.
	 *
	 * <p>
	 * The log file is located in the system temporary directory under
	 * {@code gw-command-logs}. Parent directories are created if necessary.
	 * </p>
	 *
	 * @param logId the log identifier
	 * @return the path to the log file
	 * @throws RuntimeException if the log directory cannot be created
	 */
	public static Path getCommandLogPath(String logId) {
		String tempDir = System.getProperty("java.io.tmpdir");
		Path logDir = new File(tempDir, LOGS_DIR_NAME).toPath();
		try {
			Files.createDirectories(logDir);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create log directory: " + logDir, e);
		}
		return logDir.resolve(logId + LOG_EXTENSION);
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
	 * Clears the retained content and resets the truncation flag.
	 */
	public void clear() {
		sb.setLength(0);
		truncated = false;
	}

	/**
	 * Returns the total number of characters ever appended to this builder.
	 *
	 * @return the total length of all appended content
	 */
	public int getTotalLength() {
		return totalLength;
	}

	/**
	 * Returns a report of the log state, including log ID, retained tail, total
	 * length, truncation status, and elapsed process time in milliseconds.
	 *
	 * @return a map containing log state information
	 */
	public Map<String, Object> getReport() {
		Map<String, Object> report = new HashMap<>();
		report.put("logId", logId);
		report.put("tail", sb.toString());
		report.put("totalLength", totalLength);
		report.put("truncated", truncated);
		report.put("processTime_ms", System.currentTimeMillis() - startTime);

		return report;
	}

}