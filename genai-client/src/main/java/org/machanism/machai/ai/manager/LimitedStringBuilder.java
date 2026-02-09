package org.machanism.machai.ai.manager;

public class LimitedStringBuilder {
	private final int maxSize;
	private final StringBuilder sb;

	public LimitedStringBuilder(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize must be positive");
		}
		this.maxSize = maxSize;
		this.sb = new StringBuilder();
	}

	public LimitedStringBuilder append(String text) {
		if (text == null)
			return this;
		sb.append(text);

		// Trim the builder to keep only the last maxSize characters
		int excess = sb.length() - maxSize;
		if (excess > 0) {
			sb.delete(0, excess);
		}

		return this;
	}

	public String getLastText() {
		String prefix = (sb.length() == maxSize) ? "(Previous content has been truncated)..." : "";
		return prefix + sb.toString();
	}

	public int length() {
		return sb.length();
	}

	public void clear() {
		sb.setLength(0);
	}
}