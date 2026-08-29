package org.machanism.machai.ai.provider.impl;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
 * This provider adapts the Anthropic Java SDK to the Machai provider interface.
 * It manages prompt collection, request construction for the Anthropic Beta
 * Messages API, custom tool execution, optional web search integration,
 * optional MCP server forwarding, and usage tracking.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.1.13
 */
public class AnthropicProvider extends AbstractAIProvider {

	/** Creates an Anthropic provider instance. */
	public AnthropicProvider() {
		// SonarQube java:S1186: public no-argument constructor is required for
		// reflective provider loading.
	}

	/** Logger used for provider diagnostics and request lifecycle messages. */
	private static final Logger logger = LoggerFactory.getLogger(AnthropicProvider.class);
	/**
	 * Configuration property name that contains the Anthropic API key or
	 * authorization token.
	 */
	public static final String ANTHROPIC_API_KEY = "ANTHROPIC_API_KEY";
	/** Configuration property that overrides the Anthropic API base URL. */
	public static final String ANTHROPIC_BASE_URL = "ANTHROPIC_BASE_URL";
	/** Accumulated Anthropic message inputs for the current conversation. */
	private final List<BetaMessageParam> inputs = new ArrayList<>();
	/** Mapping between Anthropic tool definitions and local functions. */
	private final Map<BetaTool.Builder, ToolFunction> toolMap = new LinkedHashMap<>();
	/**
	 * Anthropic web search tool instance registered for outgoing requests, or
	 * {@code null}.
	 */
	private Object webSearchTool;
	/** MCP server definitions forwarded to Anthropic with each request. */
	private List<BetaRequestMcpServerUrlDefinition> mcpServers = new ArrayList<>();

	/**
	 * Registers an MCP server definition for future requests.
	 * 
	 * @param name          server name exposed to the model
	 * @param url           server endpoint URL
	 * @param authorization optional authorization token
	 * @param description   optional description, currently unused by the SDK model
	 */
	@Override
	protected void addMcpServer(String name, String url, String authorization, String description) {
		BetaRequestMcpServerUrlDefinition.Builder builder = BetaRequestMcpServerUrlDefinition.builder();
		builder.url(url);
		builder.name(name);
		if (authorization != null) {
			builder.authorizationToken(authorization);
		}
		mcpServers.add(builder.build());
	}

	/**
	 * Registers a web search tool and optional location hints.
	 * 
	 * @param type    web search tool version
	 * @param city    optional city hint
	 * @param country optional country hint
	 * @param region  optional region hint
	 * @throws IllegalArgumentException if {@code type} is unsupported
	 */
	@Override
	protected void addWebSearch(String type, String city, String country, String region) {
		BetaUserLocation.Builder locationBuilder = BetaUserLocation.builder();
		if (city != null)
			locationBuilder.city(city);
		if (country != null)
			locationBuilder.country(country);
		if (region != null)
			locationBuilder.region(region);
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

	/** Adds a non-blank user prompt. @param text prompt text */
	@Override
	public void prompt(String text) {
		if (StringUtils.isNotBlank(text)) {
			inputs.add(BetaMessageParam.builder().content(text).role(Role.USER).build());
		}
	}

	/**
	 * Sends the accumulated conversation and returns the final text
	 * response. @return response text
	 */
	@Override
	public String perform() {
		return parseResponse(call(createResponseBuilder(inputs)));
	}

	/**
	 * Submits a request to the Anthropic Beta Messages API and records its token
	 * usage.
	 *
	 * @param params immutable request parameters to submit
	 * @return the message returned by Anthropic
	 */
	private BetaMessage call(MessageCreateParams params) {
		if (logger.isDebugEnabled())
			logger.debug("GenAI service request params: {}", params);
		BetaMessage response = getClient().beta().messages().create(params);
		if (logger.isDebugEnabled())
			logger.debug("GenAI service response: {}", params);
		captureUsage(response);
		return response;
	}

	/**
	 * Converts an Anthropic response into text, executing local tool calls and
	 * recursively submitting their results until no tool call remains.
	 *
	 * @param response response to process
	 * @return final assistant text, or {@code null} when the response contains no
	 *         text
	 */
	private String parseResponse(BetaMessage response) {
		List<BetaContentBlock> content = response.content();
		String result = null;
		boolean anyToolCalls = false;
		String text = null;
		for (BetaContentBlock contentBlock : content) {
			if (contentBlock.isText()) {
				text = contentBlock.text().map(t -> t.text()).orElse(null);
				if (text != null) {
					inputs.add(BetaMessageParam.builder().content(text).role(Role.ASSISTANT).build());
				}
			}
			if (contentBlock.isToolUse()) {
				handleFunctionCall(contentBlock.asToolUse());
				anyToolCalls = true;
			}
		}
		if (!anyToolCalls)
			result = text;
		else
			result = parseResponse(call(createResponseBuilder(inputs)));
		return result;
	}

	/**
	 * Captures token usage and updates global usage statistics. @param response
	 * response message
	 */
	private void captureUsage(BetaMessage response) {
		if (response.isValid()) {
			BetaUsage responseUsage = response.usage();
			long inputTokens = responseUsage.inputTokens();
			long inputCachedTokens = responseUsage.cacheCreationInputTokens().orElseGet(() -> 0L)
					+ responseUsage.cacheReadInputTokens().orElseGet(() -> 0L);
			long outputTokens = responseUsage.outputTokens();
			Usage usage = new Usage(inputTokens, inputCachedTokens, outputTokens);
			UsageStatistics.addUsage(chatModel, usage);
		}
	}

	/**
	 * Adds a received tool-use block to the conversation, invokes its local
	 * handler, and appends the corresponding tool-result user message.
	 *
	 * @param toolUse tool invocation returned by Anthropic
	 */
	private void handleFunctionCall(BetaToolUseBlock toolUse) {
		BetaContentBlock toolUseBlock = BetaContentBlock.ofToolUse(toolUse);
		List<BetaContentBlockParam> toolUseList = new ArrayList<>();
		toolUseList.add(toolUseBlock.toParam());
		inputs.add(
				BetaMessageParam.builder().role(Role.ASSISTANT).contentOfBetaContentBlockParams(toolUseList).build());
		
		Object result = callFunction(toolUse);
		if (result != null) {
			BetaToolResultBlockParam.Builder toolResult = BetaToolResultBlockParam.builder().toolUseId(toolUse.id())
					.contentAsJson(result);
			if (result instanceof String) {
				toolResult.isError(Strings.CS.startsWith((String) result, AbstractAIProvider.ERROR_TOOL_RESULT_PREFIX));
			}
			ArrayList<BetaContentBlockParam> arrayList = new ArrayList<>();
			arrayList.add(BetaContentBlockParam.ofToolResult(toolResult.build()));
			inputs.add(BetaMessageParam.builder().role(Role.USER).contentOfBetaContentBlockParams(arrayList).build());
		}
	}

	/**
	 * Locates and invokes the locally registered handler for an Anthropic tool
	 * invocation.
	 *
	 * @param toolUse tool invocation to execute
	 * @return handler result, or {@code null} when no matching handler is
	 *         registered
	 */
	private Object callFunction(BetaToolUseBlock toolUse) {
		String name = toolUse.name();
		JsonField<BetaToolUseBlockParam.Input> params = toolUse.toParam()._input();
		JsonNode node = new ObjectMapper().valueToTree(params);
		Object result = null;
		File file = projectDir;
		Set<Entry<BetaTool.Builder, ToolFunction>> entrySet = toolMap.entrySet();
		for (Entry<BetaTool.Builder, ToolFunction> entry : entrySet) {
			BetaTool tool = entry.getKey().build();
			if (entry.getValue() != null && normalize(name).equals(normalize(tool.name()))) {
				result = safelyInvokeTool(name, entry.getValue(), node, file);
				break;
			}
		}
		return result;
	}

	/**
	 * Builds a Messages API request from the current conversation and provider
	 * configuration, including registered local tools, MCP servers, and web
	 * search when configured.
	 *
	 * @param inputs conversation messages to include in the request
	 * @return configured immutable request parameters
	 */
	private MessageCreateParams createResponseBuilder(List<BetaMessageParam> inputs) {
		com.anthropic.models.beta.messages.MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
				.model(chatModel).maxTokens(maxOutputTokens);
		paramsBuilder.messages(inputs);
		if (StringUtils.isNotBlank(instructions))
			paramsBuilder.system(instructions);
		List<BetaTool.Builder> collect = new ArrayList<>(toolMap.keySet());
		List<BetaToolUnion> tools = new ArrayList<>(collect.size());
		for (int i = 0; i < collect.size(); i++) {
			BetaTool.Builder builder = collect.get(i);
			if (i == collect.size() - 1)
				builder.cacheControl(BetaCacheControlEphemeral.builder().build());
			tools.add(BetaToolUnion.ofBetaTool(builder.build()));
		}
		paramsBuilder.tools(tools);
		if (!mcpServers.isEmpty())
			paramsBuilder.mcpServers(mcpServers);
		if (webSearchTool instanceof BetaWebSearchTool20260209)
			paramsBuilder.addTool((BetaWebSearchTool20260209) webSearchTool);
		else if (webSearchTool instanceof BetaWebSearchTool20250305)
			paramsBuilder.addTool((BetaWebSearchTool20250305) webSearchTool);
		return paramsBuilder.build();
	}

	/** Clears accumulated conversation inputs. */
	@Override
	public void clear() {
		inputs.clear();
	}

	/**
	 * Registers a local function tool.
	 * 
	 * @param name        tool name
	 * @param description tool description
	 * @param function    local callback
	 * @param paramsDesc  tool input descriptors
	 */
	protected void addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc) {
		if (toolMap.keySet().stream().noneMatch(key -> Strings.CS.equals(name, key.build().name()))) {
			Map<String, JsonValue> fromValue = new HashMap<>();
			List<String> requiredProps = new ArrayList<>();
			if (paramsDesc != null)
				for (ParamDescriptor pDesc : paramsDesc) {
					if (!PROJECT_DIR_PARAM_NAME.equals(pDesc.getName())) {
						if (pDesc.isRequired())
							requiredProps.add(pDesc.getName());
						Map<String, Object> value = new HashMap<>();
						value.put("type", pDesc.getType());
						value.put("description", pDesc.getDescription());
						if (!pDesc.isRequired())
							value.put("default", pDesc.getDefaultValue());
						fromValue.put(pDesc.getName(), JsonValue.from(value));
					}
				}
			BetaTool.InputSchema.Properties properties = BetaTool.InputSchema.Properties.builder()
					.additionalProperties(fromValue).build();
			BetaTool.InputSchema inputSchema = BetaTool.InputSchema.builder().properties(properties)
					.required(requiredProps).build();
			toolMap.put(BetaTool.builder().name(name).description(description).inputSchema(inputSchema), function);
		}
	}

	/** Creates and configures an Anthropic client. @return configured client */
	protected AnthropicClient getClient() {
		String baseUrl = getConfigurator().get(ANTHROPIC_BASE_URL, null);
		String privateKey = getConfigurator().get(ANTHROPIC_API_KEY);
		Long timeout = timeoutSec != null ? timeoutSec : getConfigurator().getLong("GENAI_TIMEOUT", 0L);
		Builder clientBuilder = AnthropicOkHttpClient.builder();
		if (privateKey.startsWith("sk-"))
			clientBuilder.apiKey(privateKey);
		else
			clientBuilder.authToken(privateKey);
		if (baseUrl != null)
			clientBuilder.baseUrl(baseUrl);
		if (timeout != null && timeout > 0) {
			Duration ofSeconds = Duration.ofSeconds(timeout);
			clientBuilder.timeout(
					Timeout.builder().request(ofSeconds).read(ofSeconds).write(ofSeconds).connect(ofSeconds).build());
		}
		clientBuilder.maxRetries(3);
		return clientBuilder.build();
	}
}
