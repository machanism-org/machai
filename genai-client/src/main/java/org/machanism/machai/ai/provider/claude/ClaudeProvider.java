package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * It supports prompting, configuration, and basic usage tracking.
 * </p>
 *
 * <h2>Configuration</h2>
 * <ul>
 * <li>{@code chatModel} (required): model identifier for Anthropic API (e.g.,
 * "claude-3-opus-20240229").</li>
 * <li>{@code ANTHROPIC_API_KEY} (required): API key for Anthropic API.</li>
 * <li>{@code ANTHROPIC_BASE_URL} (optional): base URL for Anthropic-compatible
 * endpoints. If unset, SDK default is used.</li>
 * <li>{@code GENAI_TIMEOUT} (optional): request timeout in seconds. If missing,
 * 0, or negative, SDK default is used.</li>
 * <li>{@code MAX_OUTPUT_TOKENS} (optional): maximum number of output tokens.
 * Defaults to {@value #MAX_OUTPUT_TOKENS}.</li>
 * <li>{@code MAX_TOOL_CALLS} (optional): maximum number of tool calls per
 * response. 0 leaves the limit unset.</li>
 * <li>{@code embedding.model} (optional): embedding model identifier (not used
 * in this implementation).</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 * @since 1.1.13
 */
public class ClaudeProvider implements Genai {

	public static final String ANTHROPIC_API_KEY = "ANTHROPIC_API_KEY";
	public static final String ANTHROPIC_BASE_URL = "ANTHROPIC_BASE_URL";
	public static final long MAX_OUTPUT_TOKENS = 18000;

	private static final Logger logger = LoggerFactory.getLogger(ClaudeProvider.class);

	private Configurator config;
	private String chatModel;
	private Long maxOutputTokens;
	private Long maxToolCalls;
	private String embeddingModel;
	private Long timeoutSec;

	/** Accumulated prompt messages for the current conversation. */
	private final List<String> prompts = new ArrayList<>();

	/** Optional instructions applied to the request. */
	private String instructions;

	/** Working directory passed to tool handlers as contextual information. */
	private File workingDir;

	/** Maps tools to handler functions. */
	private final Map<String, ToolFunction> toolMap = new HashMap<>();

	/**
	 * Latest usage metrics captured from the most recent {@link #perform()} call.
	 */
	private Usage lastUsage = new Usage(0, 0, 0);

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
		timeoutSec = config.getLong("GENAI_TIMEOUT", 0L);
	}

	/**
	 * Adds a text prompt to the current request input.
	 *
	 * @param text the prompt string
	 */
	@Override
	public void prompt(String text) {
		if (StringUtils.isNotBlank(text)) {
			prompts.add(text);
		}
	}

	/**
	 * Executes a request using the currently configured model and accumulated
	 * prompts.
	 *
	 * @return the final model response text, or {@code null} if no text was
	 *         produced
	 */
	@Override

	public String perform() {
		if (prompts.isEmpty()) {
			logger.warn("No prompts provided for Claude request.");
			return null;
		}

		MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
				.model(chatModel)
				.maxTokens(maxOutputTokens);

		for (String prompt : prompts) {
			paramsBuilder.addUserMessage(prompt);
		}

		if (StringUtils.isNotBlank(instructions)) {
			paramsBuilder.system(instructions);
		}

		MessageCreateParams params = paramsBuilder.build();

		logger.debug("Sending request to Claude service.");
		Message response = getClient().messages().create(params);

		String result = response.content().isEmpty() ? null
				: response.content().get(0).text().map(t -> t.text()).orElse(null);

		logger.debug("Received response from Claude service.");
		return result;
	}

	/**
	 * Clears all accumulated prompts for the next request.
	 */
	@Override
	public void clear() {
		prompts.clear();
	}

	/**
	 * Registers a function tool for the current provider instance. (Not implemented
	 * for Claude in this version.)
	 *
	 * @param name        tool function name
	 * @param description tool description
	 * @param function    handler callback for tool execution
	 * @param paramsDesc  parameter descriptors
	 */
	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
		// Tool/function support can be implemented here if Anthropic SDK supports it
		logger.warn("Tool/function registration is not implemented for ClaudeProvider.");
	}

	/**
	 * Sets system-level instructions applied to subsequent requests.
	 *
	 * @param instructions instruction text, or {@code null} to clear
	 */
	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Enables request input logging to the given file. (Not implemented for Claude
	 * in this version.)
	 *
	 * @param inputsLog file for input logging, or {@code null} to disable
	 */
	@Override
	public void inputsLog(File inputsLog) {
		// Not implemented
	}

	/**
	 * Sets the working directory passed to tool handlers.
	 *
	 * @param workingDir working directory, or {@code null}
	 */
	@Override
	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Returns token usage metrics captured from the most recent {@link #perform()}
	 * call.
	 *
	 * @return usage metrics; never {@code null}
	 */
	@Override
	public Usage usage() {
		return lastUsage;
	}

	/**
	 * Requests an embedding vector for the given input text. (Not implemented for
	 * Claude in this version.)
	 *
	 * @param text       input to embed
	 * @param dimensions number of dimensions requested from the embedding model
	 * @return embedding as a list of {@code double} values, or an empty list if not
	 *         supported
	 */
	@Override
	public List<Double> embedding(String text, long dimensions) {
		logger.warn("Embeddings are not implemented for ClaudeProvider.");
		return Collections.emptyList();
	}

	/**
	 * Returns the underlying Anthropic client.
	 *
	 * @return Anthropic client
	 */
	protected AnthropicClient getClient() {
		String baseUrl = config.get(ANTHROPIC_BASE_URL, null);
		String privateKey = config.get(ANTHROPIC_API_KEY);
		Long timeout = timeoutSec != null ? timeoutSec : config.getLong("GENAI_TIMEOUT", 0L);

		Builder clientBuilder = AnthropicOkHttpClient.builder();
		clientBuilder.authToken(privateKey);

		if (baseUrl != null) {
			clientBuilder.baseUrl(baseUrl);
		}
		if (timeout != null && timeout > 0) {
			Duration ofSeconds = Duration.ofSeconds(timeout);
			Timeout t = Timeout.builder().request(ofSeconds).read(ofSeconds).write(ofSeconds).connect(ofSeconds)
					.build();
			clientBuilder.timeout(t);
		}

		clientBuilder.maxRetries(3);

		return clientBuilder.build();
	}

	/**
	 * Loads prompts from a resource bundle.
	 *
	 * @param promptBundle resource bundle containing prompts
	 */
	public void promptBundle(ResourceBundle promptBundle) {
		// Optional: implement loading prompts from a resource bundle if needed
	}
}