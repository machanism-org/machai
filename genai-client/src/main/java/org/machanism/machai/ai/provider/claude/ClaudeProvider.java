package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient.Builder;
import com.anthropic.core.JsonField;
import com.anthropic.core.JsonValue;
import com.anthropic.core.Timeout;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.MessageParam.Role;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.Tool.InputSchema.Properties;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUnion;
import com.anthropic.models.messages.ToolUseBlock;
import com.anthropic.models.messages.ToolUseBlockParam;
import com.anthropic.models.messages.ToolUseBlockParam.Input;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private static final String MCP_PROP_NAME_PREFIX = "MCP";

	private Configurator config;
	private String chatModel;
	private Long maxOutputTokens;
	private Long maxToolCalls;
	private String embeddingModel;
	private Long timeoutSec;

	/** Optional log file for input data. */
	private File inputsLog;

	/** Accumulated prompt messages for the current conversation. */
	private final List<MessageParam> inputs = new ArrayList<>();

	/** Optional instructions applied to the request. */
	private String instructions;

	/** Working directory passed to tool handlers as contextual information. */
	private File workingDir;

	/** Maps tools to handler functions. */
	private final Map<Tool, ToolFunction> toolMap = new HashMap<>();

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

		addWebSearch();
		addMcpServer();
	}

	private void addMcpServer() {
		int i = 0;
		String url = null;
		do {
			String id = "";

			if (i > 0) {
				id = "_" + i;
			}

			String propName = MCP_PROP_NAME_PREFIX + id;
			url = config.get(propName + ".url", null);
			String label = config.get(propName + ".label", null);
			String description = config.get(propName + ".description", null);
			String authorization = config.get(propName + ".authorization", null);

			if (url != null) {
				throw new NotImplementedException();
			}

		} while (i++ == 0 || url != null);
	}

	private void addWebSearch() {
		String type = config.get("WebSearchTool.type", null);
		String city = config.get("WebSearchTool.city", null);
		String country = config.get("WebSearchTool.country", null);
		String region = config.get("WebSearchTool.region", null);

		if (type != null) {
			throw new NotImplementedException();
		}
	}

	/**
	 * Adds a text prompt to the current request input.
	 *
	 * @param text the prompt string
	 */
	@Override
	public void prompt(String text) {
		if (StringUtils.isNotBlank(text)) {
			inputs.add(MessageParam.builder().content(text).role(Role.USER).build());
		}
	}

	/**
	 * Executes a request using the currently configured model and accumulated
	 * inputs.
	 *
	 * @return the final model response text, or {@code null} if no text was
	 *         produced
	 */
	@Override

	public String perform() {
		if (inputs.isEmpty()) {
			logger.warn("No inputs provided for Claude request.");
			return null;
		}

		logInputs();

		MessageCreateParams params = createResponseBuilder(inputs);

		logger.debug("Sending request to Claude service.");
		Message response = getClient().messages().create(params);
		captureUsage(response);

		String result = parseResponse(response);

		logger.debug("Received response from Claude service.");
		return result;
	}

	private String parseResponse(Message response) {
		List<ContentBlock> content = response.content();
		String result = null;
		boolean anyToolCalls = false;
		String text = null;

		for (ContentBlock contentBlock : content) {
			if (contentBlock.isText()) {
				text = contentBlock.text().map(t -> t.text()).orElse(null);
				prompt(text);
			}
			if (contentBlock.isToolUse()) {
				ToolUseBlock toolUse = contentBlock.asToolUse();
				handleFunctionCall(toolUse);
				anyToolCalls = true;
			}
		}

		if (!anyToolCalls) {
			result = text;

		} else {
			MessageCreateParams params = createResponseBuilder(this.inputs);
			response = getClient().messages().create(params);
			captureUsage(response);

			result = parseResponse(response);
			logger.debug("Sending follow-up request to LLM service for tool call resolution.");
		}

		return result;
	}

	/**
	 * Extracts token usage from the response and stores it as {@link #usage()}.
	 *
	 * @param response optional usage information from the OpenAI response
	 */
	private void captureUsage(Message response) {
		if (response.isValid()) {
			com.anthropic.models.messages.Usage responseUsage = response.usage();
			long inputTokens = responseUsage.inputTokens();
			long inputCachedTokens = responseUsage.cacheCreationInputTokens().get()
					+ responseUsage.cacheReadInputTokens().get();
			long outputTokens = responseUsage.outputTokens();

			lastUsage = new Usage(inputTokens, inputCachedTokens, outputTokens);
			UsageStatistics.addUsage(chatModel, lastUsage);
		} else {
			lastUsage = new Usage(0, 0, 0);
		}
	}

	private void handleFunctionCall(ToolUseBlock toolUse) {

		ContentBlock toolUseBlock = ContentBlock.ofToolUse(toolUse);
		List<ContentBlockParam> toolUseList = new ArrayList<>();
		toolUseList.add(toolUseBlock.toParam());
		MessageParam toolUseMessage = MessageParam.builder()
				.role(MessageParam.Role.ASSISTANT)
				.contentOfBlockParams(toolUseList)
				.build();
		inputs.add(toolUseMessage);

		Object result = null;

		boolean error = false;
		try {
			result = callFunction(toolUse);
		} catch (Exception e) {
			result = e.getMessage();
			error = true;
		}

		ToolResultBlockParam toolResult = ToolResultBlockParam.builder()
				.toolUseId(toolUse.id())
				.content(Objects.toString(result))
				.isError(error)
				.build();
		ContentBlockParam toolContentBlock = ContentBlockParam.ofToolResult(toolResult);
		ArrayList<ContentBlockParam> arrayList = new ArrayList<>();
		arrayList.add(toolContentBlock);
		MessageParam toolResultMessage = MessageParam.builder()
				.role(MessageParam.Role.USER)
				.contentOfBlockParams(arrayList)
				.build();

		inputs.add(toolResultMessage);
	}

	private Object callFunction(ToolUseBlock functionCall) {
		String name = functionCall.name();
		ToolUseBlockParam param = functionCall.toParam();
		JsonField<Input> params = param._input();

		JsonNode node = new ObjectMapper().valueToTree(params);

		Object result = null;
		File file = workingDir;
		Set<Entry<Tool, ToolFunction>> entrySet = toolMap.entrySet();
		for (Entry<Tool, ToolFunction> entry : entrySet) {
			if (entry.getValue() != null && hasSameToolName(name, entry.getKey())) {
				result = safelyInvokeTool(name, entry.getValue(), node, file);
				break;
			}
		}
		return result;
	}

	/**
	 * Safely invokes a tool function and converts {@link IOException}s into a
	 * textual error payload suitable for the model conversation.
	 *
	 * @param name       tool name
	 * @param tool       tool handler
	 * @param params     parsed tool parameters
	 * @param workingDir working directory passed to the tool
	 * @return tool output or a formatted error message
	 */
	private Object safelyInvokeTool(String name, ToolFunction tool, JsonNode params, File workingDir) {
		try {
			return tool.apply(params, workingDir);
		} catch (IOException e) {
			String errMsg = "Error: The functional tool call failed while executing '" + name + "'. Reason: "
					+ e.getMessage();
			logger.error(errMsg);
			return errMsg;
		}
	}

	/**
	 * Compares a requested tool name with a registered tool name using normalized,
	 * case-insensitive matching.
	 *
	 * @param toolName requested tool name
	 * @param tool     registered tool
	 * @return {@code true} when both names match after normalization
	 */
	private boolean hasSameToolName(String toolName, Tool tool) {
		// Sonar java:S1874: avoid deprecated StringUtils equality helpers.
		String name = tool.name();
		return normalize(toolName).equals(normalize(name));
	}

	private MessageCreateParams createResponseBuilder(List<MessageParam> inputs) {
		MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
				.model(chatModel)
				.maxTokens(maxOutputTokens);

		paramsBuilder.messages(inputs);

		if (StringUtils.isNotBlank(instructions)) {
			paramsBuilder.system(instructions);
		}

		List<ToolUnion> tools = toolMap.keySet().stream().map(t -> ToolUnion.ofTool(t)).collect(Collectors.toList());
		paramsBuilder.tools(tools);

		MessageCreateParams params = paramsBuilder.build();
		return params;
	}

	/**
	 * Clears all accumulated inputs for the next request.
	 */
	@Override
	public void clear() {
		inputs.clear();
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
		Map<String, JsonValue> fromValue = new HashMap<>();
		List<String> requiredProps = new ArrayList<>();

		if (paramsDesc != null) {
			for (String pDesc : paramsDesc) {
				String[] desc = StringUtils.splitPreserveAllTokens(pDesc, ":");
				if (desc.length >= 3 && normalize(desc[2]).equals("required")) {
					requiredProps.add(desc[0]);
				}
				Map<String, String> value = new HashMap<>();
				value.put("type", desc[1]);
				value.put("description", desc.length > 3 ? desc[3] : StringUtils.EMPTY);

				JsonValue requiredVal = JsonValue.from(value);
				fromValue.put(desc[0], requiredVal);
			}
		}

		com.anthropic.models.messages.Tool.InputSchema.Properties.Builder builder = Properties.builder();
		builder.additionalProperties(fromValue);

		Properties properties = builder.build();

		// Build the input schema (assuming Anthropic's Tool.InputSchema supports this)
		com.anthropic.models.messages.Tool.InputSchema.Builder inputSchemaBuilder = com.anthropic.models.messages.Tool.InputSchema
				.builder()
				.properties(properties)
				.required(requiredProps);

		// Build the tool
		com.anthropic.models.messages.Tool.Builder toolBuilder = com.anthropic.models.messages.Tool.builder()
				.name(name)
				.description(description)
				.inputSchema(inputSchemaBuilder.build());

		com.anthropic.models.messages.Tool tool = toolBuilder.build();

		// Register the tool and its function
		toolMap.put(tool, function); // Assuming toolMap is keyed by name; adjust as needed
	}

	/**
	 * Normalizes a string for case-insensitive comparisons.
	 *
	 * @param value source value
	 * @return lower-cased value, or an empty string when the input is {@code null}
	 */
	private String normalize(String value) {
		return StringUtils.defaultString(value).toLowerCase(Locale.ROOT);
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
	 * Enables request input logging to the given file.
	 *
	 * @param inputsLog file for input logging, or {@code null} to disable
	 */
	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
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
	 * Writes the current request inputs to {@link #inputsLog} when logging is
	 * enabled.
	 */
	void logInputs() {
		if (inputsLog != null) {
			File parentFile = inputsLog.getParentFile();
			if (parentFile != null && !parentFile.exists()) {
				parentFile.mkdirs();
			}
			try (Writer streamWriter = new FileWriter(inputsLog, false)) {
				logInputs(streamWriter);
			} catch (IOException e) {
				logger.error("Failed to save LLM inputs log to file: {}", inputsLog, e);
			}
		}
	}

	/**
	 * Serializes the current instructions and input items to the supplied writer.
	 *
	 * @param streamWriter destination writer
	 * @throws IOException when writing fails
	 */
	private void logInputs(Writer streamWriter) throws IOException {
		streamWriter.write(StringUtils.defaultString(instructions));
		streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
		streamWriter.write("-----------------------------------------");
		streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
		for (MessageParam responseInputItem : inputs) {
			String inputText = "";
			String content = responseInputItem.content().asString();
			streamWriter.write(content);
			streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
			streamWriter.write("-----------------------------------------");
			streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
		}
		logger.debug("LLM Inputs: {}", inputsLog);
	}

}