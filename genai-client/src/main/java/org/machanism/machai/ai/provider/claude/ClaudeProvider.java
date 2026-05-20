package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ToolFunction;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient.Builder;
import com.anthropic.core.Timeout;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;

/**
 * Anthropic-backed implementation of MachAI's {@link Genai} abstraction.
 *
 * <p>
 * This provider adapts the Anthropic Java SDK to MachAI's provider interface.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.1.13
 */
public class ClaudeProvider implements Genai {

	public static final String ANTHROPIC_API_KEY = "ANTHROPIC_API_KEY";

	public static final String ANTHROPIC_BASE_URL = "ANTHROPIC_BASE_URL";

	/** Default maximum number of tokens the model may generate. */
	public static final long MAX_OUTPUT_TOKENS = 18000;

	private Configurator config;
	private String chatModel;
	private Long maxOutputTokens;
	private Long maxToolCalls;
	private String embeddingModel;

	private Long timeoutSec;

	private String text;

	/**
	 * Initializes the provider from the given configuration.
	 *
	 * @param config provider configuration source
	 */
	@Override
	public void init(Configurator config) {
		this.config = config;
		chatModel = config.get("chatModel");

		maxOutputTokens = config.getLong("MAX_OUTPUT_TOKENS", MAX_OUTPUT_TOKENS);
		maxToolCalls = config.getLong("MAX_TOOL_CALLS", 0L);
		embeddingModel = config.get("embedding.model", null);
	}

	@Override
	public void prompt(String text) {
		this.text = text;
	}

	@Override
	public String perform() {
		MessageCreateParams params = MessageCreateParams.builder()
				.model(chatModel)
				.maxTokens(maxOutputTokens)
				.addUserMessage(text)
				.build();

		Message response = getClient().messages().create(params);
		System.out.println(response.content().get(0).text().get().text());
		return null;
	}

	@Override
	public void clear() {
	}

	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
	}

	@Override
	public void instructions(String instructions) {
	}

	public void promptBundle(ResourceBundle promptBundle) {
	}

	@Override
	public void inputsLog(File inputsLog) {
	}

	@Override
	public void setWorkingDir(File workingDir) {
	}

	@Override
	public Usage usage() {
		return null;
	}

	@Override
	public List<Double> embedding(String text, long dimensions) {
		return Collections.emptyList();
	}

	protected AnthropicClient getClient() {
		String baseUrl = config.get(ANTHROPIC_BASE_URL, null);
		String privateKey = config.get(ANTHROPIC_API_KEY);
		timeoutSec = config.getLong("GENAI_TIMEOUT", 0L);

		Builder clientBuilder = AnthropicOkHttpClient.builder();

		clientBuilder.authToken(privateKey);
		if (baseUrl != null) {
			clientBuilder.baseUrl(baseUrl);
		}
		if (timeoutSec != 0) {
			Duration ofSeconds = Duration.ofSeconds(timeoutSec);
			Timeout timeout = Timeout.builder().request(ofSeconds).read(ofSeconds).write(ofSeconds)
					.connect(ofSeconds).build();
			clientBuilder.timeout(timeout);
		}

		clientBuilder.maxRetries(3);

		AnthropicClient client = clientBuilder.build();
		return client;
	}

}
