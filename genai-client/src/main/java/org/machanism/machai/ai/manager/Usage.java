package org.machanism.machai.ai.manager;

public class Usage {

	private long inputTokens;
	private long inputCachedTokens;
	private long outputTokens;
	private long reasoningTokens;

	public Usage(long inputTokens, long inputCachedTokens, long outputTokens, long reasoningTokens) {
		super();
		this.inputTokens = inputTokens;
		this.inputCachedTokens = inputCachedTokens;
		this.outputTokens = outputTokens;
		this.reasoningTokens = reasoningTokens;
	}

	public long getInputTokens() {
		return inputTokens;
	}

	public long getInputCachedTokens() {
		return inputCachedTokens;
	}

	public long getOutputTokens() {
		return outputTokens;
	}

	public long getReasoningTokens() {
		return reasoningTokens;
	}

}
