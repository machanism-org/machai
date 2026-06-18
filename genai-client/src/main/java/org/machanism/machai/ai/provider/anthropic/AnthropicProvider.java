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
 * This provider adapts the Anthropic Java SDK to the Machai provider interface. It manages prompt
 * collection, request construction for the Anthropic Beta Messages API, custom tool execution,
 * optional web search integration, optional MCP server forwarding, and usage tracking.
 * </p>
 *
 * <h2>Configuration</h2>
 * <ul>
 * <li>{@code chatModel} (required): model identifier for the Anthropic API, for example
 * {@code "claude-3-opus-20240229"}.</li>
 * <li>{@code ANTHROPIC_API_KEY} (required): API key or authorization token for the Anthropic API.</li>
 * <li>{@code ANTHROPIC_BASE_URL} (optional): base URL for Anthropic-compatible endpoints. If unset,
 * the SDK default is used.</li>
 * <li>{@code GENAI_TIMEOUT} (optional): request timeout in seconds. If missing, zero, or negative,
 * the SDK default is used.</li>
 * <li>{@code MAX_OUTPUT_TOKENS} (optional): maximum number of output tokens. Defaults to
 * {@value #MAX_OUTPUT_TOKENS}.</li>
 * <li>{@code MAX_TOOL_CALLS} (optional): maximum number of tool calls per response. Zero leaves the
 * limit unset.</li>
 * <li>{@code cacheThreshold} (optional): character length above which tool results are marked with
 * ephemeral prompt cache control.</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 * @since 1.1.13
 */
public class AnthropicProvider extends AbstractAIProvider {

	/** Logger used for provider diagnostics and request lifecycle messages. */
	private static final Logger logger = LoggerFactory.getLogger(AnthropicProvider.class);

	/** Configuration property name that contains the Anthropic API key or authorization token. */
	public static final String ANTHROPIC_API_KEY = "ANTHROPIC_API_KEY";
	/** Configuration property name that overrides the Anthropic API base URL. */
	public static final String ANTHROPIC_BASE_URL = "ANTHROPIC_BASE_URL";
	/** Configuration property name that controls prompt-cache application for large tool results. */
	private static final String CACHE_THRESHOLD_PROP_NAME = "cacheThreshold";

	/** Character-count threshold above which tool results are marked as ephemeral-cacheable. */
	private Long cacheThreshold;

	/** Accumulated Anthropic message inputs for the current conversation. */
	private final List<BetaMessageParam> inputs = new ArrayList<>();

	/** Mapping between Anthropic tool definitions and the local functions that execute them. */
	private final Map<BetaTool, ToolFunction> toolMap = new HashMap<>();

	/** Anthropic web search tool instance registered for outgoing requests, or {@code null}. */
	private Object webSearchTool;

	/** MCP server definitions forwarded to Anthropic with each request. */
	private List<BetaRequestMcpServerUrlDefinition> mcpServers = new ArrayList<>();

	/**
	 * Initializes this provider with the supplied model and configuration.
	 *
	 * @param model the Anthropic model identifier to use for subsequent requests
	 * @param config provider configuration containing credentials, endpoint, timeout, and limits
	 */
	@Override
	public void init(String model, Configurator config) {
		cacheThreshold = config.getLong(CACHE_THRESHOLD_PROP_NAME, cacheThreshold);
		super.init(model, config);
	}

	/**
	 * Registers an MCP server definition to include in future Anthropic requests.
	 *
	 * @param name server name exposed to the model
	 * @param url server endpoint URL
	 * @param authorization optional authorization token, or {@code null} when not required
	 * @param description optional human-readable server description; currently not forwarded by the SDK model
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
	 * Registers a web search tool and optional user-location hints for future requests.
	 *
	 * @param type web search tool version; supported values are {@code "20260209"}, {@code "20250305"},
	 *        and {@link #DEFAULT_WEBSEARCH_TYPE_NAME}
	 * @param city optional city hint for search localization
	 * @param country optional country hint for search localization
	 * @param region optional region hint for search localization
	 * @throws IllegalArgumentException if {@code type} is not a supported Anthropic web search tool version
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
	 * Adds a non-blank user prompt to the current conversation.
	 *
	 * @param text prompt text to submit as a user message; blank values are ignored
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
	 * Sends the accumulated conversation to Anthropic and returns the final text response.
	 *
	 * <p>
	 * If the model requests tool use, this method resolves the tool-call loop before returning the final
	 * textual answer.
	 * </p>
	 *
	 * @return final model response text, or {@code null} when no inputs are available or no text is produced
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
	 * Parses an Anthropic response and recursively handles requested tool calls.
	 *
	 * @param response response message returned by Anthropic
	 * @return final text response after any tool calls are resolved, or {@code null} if no text is present
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
	 * Captures token usage from a response and updates both {@link #lastUsage} and global statistics.
	 *
	 * @param response Anthropic response message whose usage should be recorded
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
	 * Appends the assistant tool-use message, invokes the matching local function, and appends the tool result.
	 *
	 * @param toolUse Anthropic tool-use block containing the tool name, id, and input payload
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
	 * Invokes the registered local function that matches an Anthropic tool-use request.
	 *
	 * @param toolUse tool-use block received from Anthropic
	 * @return local tool result as a string, or {@code null} if no matching tool is registered
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
	 * Builds request parameters from the current provider configuration and supplied messages.
	 *
	 * @param inputs message list to include in the Anthropic request
	 * @return immutable Anthropic message creation parameters
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
	 * Clears the accumulated conversation inputs so the provider can be reused for a new request.
	 */
	@Override
	public void clear() {
		inputs.clear();
	}

	/**
	 * Registers a local function tool that Anthropic may request during response generation.
	 *
	 * @param name tool function name exposed to the model
	 * @param description human-readable tool description used by the model to decide when to call it
	 * @param function callback that executes the tool locally
	 * @param paramsDesc descriptors for tool input parameters; the project directory parameter is handled
	 *        internally and is not included in the Anthropic input schema
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
	 * Creates and configures an Anthropic client using the current provider configuration.
	 *
	 * @return Anthropic client configured with authentication, optional base URL, timeout, and retry settings
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
	 * @param streamWriter destination writer that receives serialized prompt content
	 * @throws IOException if writing to {@code streamWriter} fails
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