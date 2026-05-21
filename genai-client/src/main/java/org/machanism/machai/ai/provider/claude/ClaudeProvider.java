package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.AbstractAIProvider;
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
import com.anthropic.models.messages.UserLocation;
import com.anthropic.models.messages.WebSearchTool20250305;
import com.anthropic.models.messages.WebSearchTool20260209;
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
public class ClaudeProvider extends AbstractAIProvider {

	private static final Logger logger = LoggerFactory.getLogger(ClaudeProvider.class);

	public static final String ANTHROPIC_API_KEY = "ANTHROPIC_API_KEY";
	public static final String ANTHROPIC_BASE_URL = "ANTHROPIC_BASE_URL";

	/** Accumulated prompt messages for the current conversation. */
	private final List<MessageParam> inputs = new ArrayList<>();

	/** Maps tools to handler functions. */
	private final Map<Tool, ToolFunction> toolMap = new HashMap<>();

	private Object webSearchTool;

	@Override
	protected void addMcpServer() {
		int i = 0;
		String url = null;
		do {
			String id = "";

			if (i > 0) {
				id = "_" + i;
			}

			String propName = MCP_PROP_NAME_PREFIX + id;
			url = config.get(propName + ".url", null);
			if (url != null) {
				throw new NotImplementedException();
			}

		} while (i++ == 0 || url != null);
	}

	@Override
	protected void addWebSearch() {
		String type = config.get("WebSearchTool.type", null);
		String city = config.get("WebSearchTool.city", null);
		String country = config.get("WebSearchTool.country", null);
		String region = config.get("WebSearchTool.region", null);

		if (type != null) {

			com.anthropic.models.messages.UserLocation.Builder locationBuilder = UserLocation.builder();
			if (city != null) {
				locationBuilder.city(city);
			}

			if (country != null) {
				locationBuilder.country(country);
			}

			if (region != null) {
				locationBuilder.region(region);
			}

			switch (type) {
			case "WebSearchTool20260209":
				com.anthropic.models.messages.WebSearchTool20260209.Builder builder1 = WebSearchTool20260209.builder();
				builder1.userLocation(locationBuilder.build());
				webSearchTool = builder1.build();
				break;

			case "WebSearchTool20250305":
				com.anthropic.models.messages.WebSearchTool20250305.Builder builder2 = WebSearchTool20250305.builder();
				builder2.userLocation(locationBuilder.build());
				webSearchTool = builder2.build();
				break;

			default:
				throw new IllegalArgumentException(
						"Invalid WebSearchTool type provided. Supported types are: WebSearchTool20260209, WebSearchTool20250305.");
			}
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
			if (entry.getValue() != null && normalize(name).equals(normalize(entry.getKey().name()))) {
				result = safelyInvokeTool(name, entry.getValue(), node, file);
				break;
			}
		}
		return result;
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

		if (webSearchTool != null) {
			switch (webSearchTool.getClass().getSimpleName()) {
			case "WebSearchTool20260209":
				paramsBuilder.addTool((WebSearchTool20260209) webSearchTool);
				break;

			case "WebSearchTool20250305":
				paramsBuilder.addTool((WebSearchTool20250305) webSearchTool);
				break;

			default:
				break;
			}
		}

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

	@Override
	protected void logInputsSpec(Writer streamWriter) throws IOException {
		for (MessageParam responseInputItem : inputs) {
			String content = responseInputItem.content().asString();
			streamWriter.write(content);
			streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
			streamWriter.write("-----------------------------------------");
			streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
		}
	}

}