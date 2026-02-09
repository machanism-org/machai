package org.machanism.machai.ai.manager;

/**
 * Token usage metrics for a single generative-AI invocation.
 *
 * <p>Instances of this class are typically created by provider implementations and aggregated by
 * {@link GenAIProviderManager} to produce a summary log of the application's consumption.
 */
public class Usage {

	private final long inputTokens;
	private final long inputCachedTokens;
	private final long outputTokens;
	private final long reasoningTokens;

	/**
	 * Creates a usage record.
	 *
	 * @param inputTokens        number of input tokens
	 * @param inputCachedTokens  number of cached input tokens (provider-specific)
	 * @param outputTokens       number of output tokens
	 * @param reasoningTokens    number of reasoning tokens (provider-specific)
	 */
	public Usage(long inputTokens, long inputCachedTokens, long outputTokens, long reasoningTokens) {
		this.inputTokens = inputTokens;
		this.inputCachedTokens = inputCachedTokens;
		this.outputTokens = outputTokens;
		this.reasoningTokens = reasoningTokens;
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

	/**
	 * Returns the number of reasoning tokens.
	 *
	 * @return reasoning token count
	 */
	public long getReasoningTokens() {
		return reasoningTokens;
	}

}
