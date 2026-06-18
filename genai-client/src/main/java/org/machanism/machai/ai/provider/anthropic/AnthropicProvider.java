package org.machanism.machai.ai.provider.anthropic;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient.Builder;
import com.anthropic.core.JsonField;
import com.anthropic.core.JsonValue;
import com.anthropic.core.Timeout;
import com.anthropic.models.beta.messages.BetaCacheControlEphemeral;
import com.anthropic.models.beta.messages.BetaContentBlock;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaMessageParam;
import com.anthropic.models.beta.messages.BetaMessageParam.Role;
import com.anthropic.models.beta.messages.BetaRequestMcpServerUrlDefinition;
import com.anthropic.models.beta.messages.BetaTool;
import com.anthropic.models.beta.messages.BetaToolResultBlockParam;
import com.anthropic.models.beta.messages.BetaToolUnion;
import com.anthropic.models.beta.messages.BetaToolUseBlock;
import com.anthropic.models.beta.messages.BetaToolUseBlockParam;
import com.anthropic.models.beta.messages.BetaUsage;
import com.anthropic.models.beta.messages.BetaUserLocation;
import com.anthropic.models.beta.messages.BetaWebSearchTool20250305;
import com.anthropic.models.beta.messages.BetaWebSearchTool20260209;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Anthropic-backed implementation of Machai's {@link Genai} abstraction.
 *
 * <p>
 * This provider adapts the Anthropic Java SDK to Machai's provider interface.
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
 * </ul>
 *
 * @author Viktor Tovstyi
 * @since 1.1.13
 */
public class AnthropicProvider extends AbstractAIProvider {

	/** Logger for this provider. */
	private static final Logger logger = LoggerFactory.getLogger(AnthropicProvider.class);

	/** Configuration property for Anthropic API key. */
	public static final String ANTHROPIC_API_KEY = "ANTHROPIC_API_KEY";
	/** Configuration property for Anthropic base URL. */
	public static final String ANTHROPIC_BASE_URL = "ANTHROPIC_BASE_URL";
	/** Configuration property for cache threshold. */
	private static final String CACHE_THRESHOLD_PROP_NAME = "cacheThreshold";

	/** Maximum allowed length for cached tool results. */
	private Long cacheThreshold;

	/** Accumulated prompt messages for the current conversation. */
	private final List<BetaMessageParam> inputs = new ArrayList<>();

	/** Maps Anthropic tools to handler functions. */
	private final Map<BetaTool, ToolFunction> toolMap = new HashMap<>();

	/** Registered web search tool, if any. */
	private Object webSearchTool;

	/** List of registered MCP servers. */
	private List<BetaRequestMcpServerUrlDefinition> mcpServers = new ArrayList<>();

	/**
	 * Initializes the provider from the given configuration.
	 *
	 * @param model  the model identifier
	 * @param config provider configuration source
	 */
	@Override
	public void init(String model, Configurator config) {
		cacheThreshold = config.getLong(CACHE_THRESHOLD_PROP_NAME, cacheThreshold);
		super.init(model, config);
	}

	/**
	 * Registers an MCP server for Anthropic tool use.
	 *
	 * @param name          server name
	 * @param url           server endpoint URL
	 * @param authorization optional authorization token
	 * @param description   optional description
	 */
	@Override
	protected void addMcpServer(String name, String url, String authorization, String description) {
		com.anthropic.models.beta.messages.BetaRequestMcpServerUrlDefinition.Builder builder = BetaRequestMcpServerUrlDefinition
				.builder();
		builder.url(url);
		builder.name(name);
		if (authorization != null) {
			builder.authorizationToken(authorization);
		}
		mcpServers.add(builder.build());
	}

	/**
	 * Registers a web search tool for Anthropic, based on configuration.
	 *
	 * @param type    tool type/version
	 * @param city    optional city
	 * @param country optional country
	 * @param region  optional region
	 */
	@Override
	protected void addWebSearch(String type, String city, String country, String region) {
		BetaUserLocation.Builder locationBuilder = BetaUserLocation.builder();
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
		case DEFAULT_WEBSEARCH_TYPE_NAME:
		case "20260209":
			BetaWebSearchTool20260209.Builder builder1 = BetaWebSearchTool20260209.builder();
			builder1.userLocation(locationBuilder.build());
			webSearchTool = builder1.build();
			break;
		case "20250305":
			BetaWebSearchTool20250305.Builder builder2 = BetaWebSearchTool20250305.builder();
			builder2.userLocation(locationBuilder.build());
			webSearchTool = builder2.build();
			break;
		default:
			throw new IllegalArgumentException(
					"Invalid WebSearchTool type provided. Supported types are: 20260209, 20250305.");
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
			com.anthropic.models.beta.messages.BetaMessageParam.Builder builder = BetaMessageParam.builder()
					.content(text)
					.role(Role.USER);
			inputs.add(builder.build());
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
		BetaMessage response = getClient().beta().messages().create(params);
		captureUsage(response);

		String result = parseResponse(response);

		logger.debug("Received response from Claude service.");
		return result;
	}

	/**
	 * Parses the Anthropic response, handling tool calls if present.
	 *
	 * @param response the Anthropic response message
	 * @return the final response text
	 */
	private String parseResponse(BetaMessage response) {
		List<BetaContentBlock> content = response.content();
		String result = null;
		boolean anyToolCalls = false;
		String text = null;

		for (BetaContentBlock contentBlock : content) {
			if (contentBlock.isText()) {
				text = contentBlock.text().map(t -> t.text()).orElse(null);
				prompt(text);
			}
			if (contentBlock.isToolUse()) {
				BetaToolUseBlock toolUse = contentBlock.asToolUse();
				handleFunctionCall(toolUse);
				anyToolCalls = true;
			}
		}

		if (!anyToolCalls) {
			result = text;
		} else {
			MessageCreateParams params = createResponseBuilder(this.inputs);
			response = getClient().beta().messages().create(params);
			captureUsage(response);

			result = parseResponse(response);
			logger.debug("Sending follow-up request to LLM service for tool call resolution.");
		}

		return result;
	}

	/**
	 * Extracts token usage from the response and stores it as {@link #lastUsage}.
	 *
	 * @param response Anthropic response message
	 */
	private void captureUsage(BetaMessage response) {
		if (response.isValid()) {
			BetaUsage responseUsage = response.usage();
			long inputTokens = responseUsage.inputTokens();
			long inputCachedTokens = responseUsage.cacheCreationInputTokens().orElseGet(() -> 0L)
					+ responseUsage.cacheReadInputTokens().orElseGet(() -> 0L);
			long outputTokens = responseUsage.outputTokens();

			lastUsage = new Usage(inputTokens, inputCachedTokens, outputTokens);
			UsageStatistics.addUsage(chatModel, lastUsage);
		} else {
			lastUsage = new Usage(0, 0, 0);
		}
	}

	/**
	 * Handles a tool function call from the Anthropic response.
	 *
	 * @param toolUse the tool use block
	 */
	private void handleFunctionCall(BetaToolUseBlock toolUse) {
		BetaContentBlock toolUseBlock = BetaContentBlock.ofToolUse(toolUse);
		List<BetaContentBlockParam> toolUseList = new ArrayList<>();
		toolUseList.add(toolUseBlock.toParam());
		BetaMessageParam toolUseMessage = BetaMessageParam.builder()
				.role(BetaMessageParam.Role.ASSISTANT)
				.contentOfBetaContentBlockParams(toolUseList)
				.build();
		inputs.add(toolUseMessage);

		String result = callFunction(toolUse);

		com.anthropic.models.beta.messages.BetaToolResultBlockParam.Builder toolResult = BetaToolResultBlockParam
				.builder()
				.toolUseId(toolUse.id())
				.content(result);

		if (cacheThreshold != null && StringUtils.length(result) > cacheThreshold) {
			toolResult.cacheControl(BetaCacheControlEphemeral.builder().build());
		}

		BetaContentBlockParam toolContentBlock = BetaContentBlockParam.ofToolResult(toolResult.build());
		ArrayList<BetaContentBlockParam> arrayList = new ArrayList<>();
		arrayList.add(toolContentBlock);
		BetaMessageParam toolResultMessage = BetaMessageParam.builder()
				.role(BetaMessageParam.Role.USER)
				.contentOfBetaContentBlockParams(arrayList)
				.build();

		inputs.add(toolResultMessage);
	}

	/**
	 * Invokes the registered function for the given tool use.
	 *
	 * @param toolUse the tool use block
	 * @return the function result as a string
	 */
	private String callFunction(BetaToolUseBlock toolUse) {
		String name = toolUse.name();
		BetaToolUseBlockParam param = toolUse.toParam();
		JsonField<com.anthropic.models.beta.messages.BetaToolUseBlockParam.Input> params = param._input();

		JsonNode node = new ObjectMapper().valueToTree(params);

		String result = null;
		File file = projectDir;
		Set<Entry<BetaTool, ToolFunction>> entrySet = toolMap.entrySet();
		for (Entry<BetaTool, ToolFunction> entry : entrySet) {
			if (entry.getValue() != null && normalize(name).equals(normalize(entry.getKey().name()))) {
				result = safelyInvokeTool(name, entry.getValue(), node, file);
				break;
			}
		}
		return result;
	}

	/**
	 * Builds the Anthropic message request from accumulated inputs and
	 * configuration.
	 *
	 * @param inputs the list of message parameters
	 * @return the constructed {@link MessageCreateParams}
	 */
	private MessageCreateParams createResponseBuilder(List<BetaMessageParam> inputs) {
		com.anthropic.models.beta.messages.MessageCreateParams.Builder paramsBuilder = com.anthropic.models.beta.messages.MessageCreateParams
				.builder()
				.model(chatModel)
				.maxTokens(maxOutputTokens);

		paramsBuilder.messages(inputs);

		if (StringUtils.isNotBlank(instructions)) {
			paramsBuilder.system(instructions);
		}

		List<BetaToolUnion> tools = toolMap.keySet().stream().map(t -> BetaToolUnion.ofBetaTool(t))
				.collect(Collectors.toList());
		paramsBuilder.tools(tools);

		if (!mcpServers.isEmpty()) {
			paramsBuilder.mcpServers(mcpServers);
		}

		if (webSearchTool instanceof BetaWebSearchTool20260209) {
			paramsBuilder.addTool((BetaWebSearchTool20260209) webSearchTool);
		} else if (webSearchTool instanceof BetaWebSearchTool20250305) {
			paramsBuilder.addTool((BetaWebSearchTool20250305) webSearchTool);
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
	 * Registers a function tool for the current provider instance.
	 *
	 * @param name        tool function name
	 * @param description tool description
	 * @param function    handler callback for tool execution
	 * @param paramsDesc  parameter descriptors
	 */
	public void addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc) {
		Map<String, JsonValue> fromValue = new HashMap<>();
		List<String> requiredProps = new ArrayList<>();

		if (paramsDesc != null) {
			for (ParamDescriptor pDesc : paramsDesc) {
				if (!PROJECT_DIR_PARAM_NAME.equals(pDesc.getName())) {
					if (pDesc.isRequired()) {
						requiredProps.add(pDesc.getName());
					}
					Map<String, String> value = new HashMap<>();
					value.put("type", pDesc.getType());
					value.put("description", pDesc.getDescription());

					JsonValue requiredVal = JsonValue.from(value);
					fromValue.put(pDesc.getName(), requiredVal);
				}
			}
		}

		BetaTool.InputSchema.Properties.Builder builder = BetaTool.InputSchema.Properties.builder();
		builder.additionalProperties(fromValue);

		BetaTool.InputSchema.Properties properties = builder.build();

		// Build the input schema
		BetaTool.InputSchema.Builder inputSchemaBuilder = BetaTool.InputSchema.builder()
				.properties(properties)
				.required(requiredProps);

		// Build the tool
		BetaTool.Builder toolBuilder = BetaTool.builder()
				.name(name)
				.description(description)
				.inputSchema(inputSchemaBuilder.build());

		BetaTool tool = toolBuilder.build();

		// Register the tool and its function
		toolMap.put(tool, function);
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

		if (privateKey.startsWith("sk-")) {
			clientBuilder.apiKey(privateKey);
		} else {
			clientBuilder.authToken(privateKey);
		}

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
	 * Writes provider-specific input items to the supplied log writer.
	 *
	 * @param streamWriter destination writer
	 * @throws IOException if writing fails
	 */
	@Override
	protected void logInputsSpec(Writer streamWriter) throws IOException {
		for (BetaMessageParam responseInputItem : inputs) {
			String content = responseInputItem.content().asString();
			streamWriter.write(content);
			streamWriter.write(PARAGRAPH_SEPARATOR);
			streamWriter.write("-----------------------------------------");
			streamWriter.write(PARAGRAPH_SEPARATOR);
		}
	}
}