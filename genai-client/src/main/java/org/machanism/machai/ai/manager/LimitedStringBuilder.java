package org.machanism.machai.ai.manager;

/**
 * A {@link StringBuilder}-like helper that retains only the last {@code maxSize} characters.
 *
 * <p>This is useful when capturing potentially unbounded process output (for example, from command execution)
 * while ensuring a deterministic upper bound on memory usage.
 */
public class LimitedStringBuilder {
	private final int maxSize;
	private final StringBuilder sb;

	/**
	 * Creates a builder that keeps at most {@code maxSize} characters.
	 *
	 * @param maxSize maximum number of characters to retain; must be positive
	 * @throws IllegalArgumentException if {@code maxSize} is not positive
	 */
	public LimitedStringBuilder(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize must be positive");
		}
		this.maxSize = maxSize;
		this.sb = new StringBuilder();
	}

	/**
	 * Appends text to this builder, trimming any excess characters from the start to stay within the
	 * configured maximum size.
	 *
	 * @param text text to append; ignored if {@code null}
	 * @return this instance for fluent chaining
	 */
	public LimitedStringBuilder append(String text) {
		if (text == null) {
			return this;
		}
		sb.append(text);

		// Trim the builder to keep only the last maxSize characters
		int excess = sb.length() - maxSize;
		if (excess > 0) {
			sb.delete(0, excess);
		}

		return this;
	}

	/**
	 * Returns the retained content.
	 *
	 * <p>If truncation occurred, a prefix is added to indicate prior content was discarded.
	 *
	 * @return retained text (possibly with a truncation prefix)
	 */
	public String getLastText() {
		String prefix = (sb.length() == maxSize) ? "(Previous content has been truncated)..." : "";
		return prefix + sb.toString();
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
	}
}
