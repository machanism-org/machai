package org.machanism.machai.ai.manager;

/**
 * Token usage metrics for a single generative-AI invocation.
 *
 * <p>Instances of this class are typically created by provider implementations and aggregated by
 * {@link GenAIProviderManager} to produce a summary log of the application's consumption.
 *
 * <p>This is an immutable value object.
 */
public class Usage {

	private final long inputTokens;
	private final long inputCachedTokens;
	private final long outputTokens;

	/**
	 * Creates a usage record.
	 *
	 * @param inputTokens       number of input tokens
	 * @param inputCachedTokens number of cached input tokens (provider-specific)
	 * @param outputTokens      number of output tokens
	 */
	public Usage(long inputTokens, long inputCachedTokens, long outputTokens) {
		this.inputTokens = inputTokens;
		this.inputCachedTokens = inputCachedTokens;
		this.outputTokens = outputTokens;
	}

	/**
	 * Returns the number of input tokens.
	 *
	 * @return input token count
	 */
	public long getInputTokens() {
		return inputTokens;
	}

	/**
	 * Returns the number of cached input tokens.
	 *
	 * @return cached input token count
	 */
	public long getInputCachedTokens() {
		return inputCachedTokens;
	}

	/**
	 * Returns the number of output tokens.
	 *
	 * @return output token count
	 */
	public long getOutputTokens() {
		return outputTokens;
	}

}
