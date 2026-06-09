package org.machanism.machai.ai.provider.openai;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonString;
import com.openai.core.JsonValue;
import com.openai.core.Timeout;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.models.Model;
import com.openai.models.responses.FunctionTool;
import com.openai.models.responses.FunctionTool.Parameters;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputFile;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputItem.Message;
import com.openai.models.responses.ResponseInputItem.Message.Role;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputMessage.Content;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseReasoningItem;
import com.openai.models.responses.ResponseUsage;
import com.openai.models.responses.Tool;
import com.openai.models.responses.WebSearchTool;
import com.openai.models.responses.WebSearchTool.UserLocation;
import com.openai.services.blocking.ModelService;

/**
 * OpenAI-backed {@link Genai} implementation.
 *
 * &lt;p&gt;This provider adapts the MachAI provider abstraction to the OpenAI
 * Java SDK Responses API. It supports prompting, file inputs, tool/function
 * calling, optional web search and MCP tools, and embedding generation.
 * &lt;/p&gt;
 *
 * <h2>Configuration</h2> &lt;p&gt;Configuration values are read from the
 * {@link Configurator} passed to {@link #init(String, Configurator)}.
 * &lt;/p&gt;&lt;ul&gt;
 * <li>{@code chatModel} (required): model identifier passed to the OpenAI
 * Responses API, for example {@code gpt-4.1} or {@code gpt-4o}.</li>
 * <li>{@code OPENAI_API_KEY} (required): API key used to authenticate with the
 * OpenAI API.</li>
 * <li>{@code OPENAI_BASE_URL} (optional): base URL for OpenAI-compatible
 * endpoints. If unset, the SDK default base URL is used.</li>
 * <li>{@code GENAI_TIMEOUT} (optional): request timeout in seconds. If missing,
 * {@code 0}, or negative, the SDK default timeouts are used.</li>
 * <li>{@code MAX_OUTPUT_TOKENS} (optional): maximum number of output tokens.
 * Defaults to {@value #MAX_OUTPUT_TOKENS}.</li>
 * <li>{@code MAX_TOOL_CALLS} (optional): maximum number of tool calls the model
 * may issue in a single response loop. A value of {@code 0} leaves the limit
 * unset.</li>
 * <li>{@code WebSearchTool.type} (optional): enables the built-in web search
 * tool when present.</li>
 * <li>{@code WebSearchTool.city} (optional): city value for the web search user
 * location.</li>
 * <li>{@code WebSearchTool.country} (optional): country value for the web
 * search user location.</li>
 * <li>{@code WebSearchTool.region} (optional): region value for the web search
 * user location.</li>
 * <li>{@code MCP.url}, {@code MCP.label}, {@code MCP.description},
 * {@code MCP.authorization} (optional): registers an MCP server tool.</li>
 * <li>{@code MCP_1.url}, {@code MCP_1.label}, {@code MCP_1.description},
 * {@code MCP_1.authorization} and similarly numbered groups (optional):
 * registers additional MCP server tools.</li> &lt;/ul&gt;
 */
public class OpenAIProvider extends AbstractAIProvider implements EmbeddingProvider {

	/** Logger instance for this provider. */
	static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

	/**
	 * Configuration/environment key used by OpenAI-compatible clients to provide
	 * the API key.
	 *
	 * <p>
	 * For CodeMie this value is set to the retrieved OAuth 2.0 access token.
	 * </p>
	 */
	public static final String OPENAI_API_KEY = "OPENAI_API_KEY";

	public static final String OPENAI_BASE_URL_NAME = "OPENAI_BASE_URL";

	/** Maps tools to handler functions. */
	final Map<Tool, ToolFunction> toolMap = new HashMap<>();

	/** Accumulated request input items for the current conversation. */
	final List<ResponseInputItem> inputs = new ArrayList<>();

	/**
	 * Adds the built-in OpenAI web search tool when configured.
	 */
	protected void addWebSearch(String type, String city, String country, String region) {
		UserLocation.Builder location = UserLocation.builder();
		location.type(WebSearchTool.UserLocation.Type.APPROXIMATE);

		if (DEFAULT_WEBSEARCH_TYPE_NAME.equals(type)) {
			type = "web_search_preview";
		}

		com.openai.models.responses.WebSearchTool.Builder webSearch = WebSearchTool.builder()
				.type(WebSearchTool.Type.of(type));

		if (city != null) {
			location.city(city);
		}

		if (country != null) {
			location.country(country);
		}

		if (region != null) {
			location.region(region);
		}

		webSearch.userLocation(location.build());
		Tool tool = Tool.ofWebSearch(webSearch.build());
		toolMap.put(tool, null);
	}

	/**
	 * Adds one or more MCP server tools from configuration.
	 */
	protected void addMcpServer(String name, String url, String authorization, String description) {
		com.openai.models.responses.Tool.Mcp.Builder builder = Tool.Mcp.builder();
		builder.serverLabel(name);
		builder.serverUrl(url);

		if (description != null) {
			builder.serverDescription(description);
		}
		if (authorization != null) {
			builder.authorization(authorization);
		}

		Tool tool = Tool.ofMcp(builder.build());
		toolMap.put(tool, null);
	}

	/**
	 * Registers a function tool for the current provider instance.
	 *
	 * <p>
	 * The {@code paramsDesc} entries must follow the format
	 * {@code name:type:required:description}. The parameter schema passed to OpenAI
	 * is a JSON Schema object of type {@code object}.
	 * </p>
	 *
	 * @param name        tool function name
	 * @param description tool description
	 * @param function    handler callback for tool execution
	 * @param paramsDesc  parameter descriptors in the format
	 *                    {@code name:type:required:description}
	 */
	public void addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc) {
		Map<String, Map<String, String>> fromValue = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode requiredProps = mapper.createArrayNode();
		if (paramsDesc != null) {
			for (ParamDescriptor pDesc : paramsDesc) {
				if (!PROJECT_DIR_PARAM_NAME.equals(pDesc.getName())) {
					if (pDesc.isRequired()) {
						requiredProps.add(pDesc.getName());
					}
					Map<String, String> value = new HashMap<>();
					value.put("type", pDesc.getType());
					value.put("description", pDesc.getDescription());
					fromValue.put(pDesc.getName(), value);
				}
			}
		}
		JsonValue propsVal = JsonValue.fromJsonNode(mapper.convertValue(fromValue, JsonNode.class));
		JsonValue requiredVal = JsonValue.fromJsonNode(requiredProps);
		Parameters params = Parameters.builder().putAdditionalProperty("properties", propsVal)
				.putAdditionalProperty("type", JsonString.of("object")).putAdditionalProperty("required", requiredVal)
				.build();
		FunctionTool.Builder toolBuilder = FunctionTool.builder().name(name).description(description);
		toolBuilder.parameters(params);
		Tool tool = Tool.ofFunction(toolBuilder.strict(false).build());
		toolMap.put(tool, function);
	}

	/**
	 * Adds a text prompt to the current request input.
	 *
	 * @param text the prompt string
	 */
	@Override
	public void prompt(String text) {
		Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
				.addInputTextContent(text).build();
		inputs.add(ResponseInputItem.ofMessage(message));
	}

	/**
	 * Executes a request using the currently configured model, inputs, and tools.
	 *
	 * <p>
	 * If the response contains tool calls, the provider executes the matching tool
	 * handlers and continues the conversation until a final text response is
	 * produced.
	 * </p>
	 *
	 * @return the final model response text, or {@code null} if no text was
	 *         produced
	 */
	@Override
	public String perform() {
		logInputs();
		ResponseCreateParams params = createResponseBuilder(inputs);

		logger.debug("Sending request to LLM service.");

		Response response = getClient().responses().create(params);
		captureUsage(response.usage());

		String result = parseResponse(response);
		logger.debug("Received response from LLM service.");
		return result;
	}

	/**
	 * Parses the given response and handles function tool calls.
	 *
	 * <p>
	 * Tool calls are executed via {@link #callFunction(ResponseFunctionToolCall)}.
	 * If tool calls are present, the method creates and sends a follow-up request
	 * with both the tool call and tool output attached, and repeats until the model
	 * returns a final message.
	 * </p>
	 *
	 * @param response response object
	 * @return response string, potentially after one or more tool call iterations
	 */
	private String parseResponse(Response response) {
		String result = null;
		Response current = response;
		while (current != null) {
			boolean anyToolCalls = false;
			String text = null;
			for (ResponseOutputItem item : current.output()) {
				if (item.isFunctionCall()) {
					anyToolCalls = true;
					handleFunctionCall(item.asFunctionCall());
				}
				String reasoning = extractReasoningText(item);
				if (StringUtils.isNotBlank(reasoning)) {
					text = reasoning;
				}
				String messageText = extractMessageText(item);
				if (StringUtils.isNotBlank(messageText)) {
					text = messageText;
				}
			}

			if (!anyToolCalls) {
				result = text;
				break;

			} else {
				ResponseCreateParams params = createResponseBuilder(this.inputs);

				logger.debug("Sending follow-up request to LLM service for tool call resolution.");
				current = getClient().responses().create(params);
				captureUsage(current.usage());
			}
		}

		return result;
	}

	/**
	 * Records a function call in the conversation, executes it, and appends the
	 * resulting function output item.
	 *
	 * @param functionCall the function call returned by the model
	 */
	private void handleFunctionCall(ResponseFunctionToolCall functionCall) {
		inputs.add(ResponseInputItem.ofFunctionCall(functionCall));

		String value = callFunction(functionCall);

		inputs.add(ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
				.callId(functionCall.callId()).outputAsJson(value).build()));
	}

	/**
	 * Extracts reasoning text from a response output item when present.
	 *
	 * @param item response output item
	 * @return reasoning text, or {@code null} when unavailable
	 */
	private String extractReasoningText(ResponseOutputItem item) {
		if (!item.isReasoning()) {
			return null;
		}
		ResponseReasoningItem reasoningItem = item.asReasoning();
		Optional<List<com.openai.models.responses.ResponseReasoningItem.Content>> maybeContent = reasoningItem
				.content();
		return maybeContent.map(this::firstNonBlankReasoning).orElse(null);
	}

	/**
	 * Returns the first non-blank reasoning fragment.
	 *
	 * @param contents reasoning content fragments
	 * @return first non-blank reasoning text, or {@code null} when none exists
	 */
	private String firstNonBlankReasoning(List<com.openai.models.responses.ResponseReasoningItem.Content> contents) {
		for (com.openai.models.responses.ResponseReasoningItem.Content content : contents) {
			String reasoning = content.text();
			if (StringUtils.isNotBlank(reasoning)) {
				logger.info("LLM Reasoning: {}", reasoning);
				return reasoning;
			}
		}
		return null;
	}

	/**
	 * Extracts text content from a message output item.
	 *
	 * @param item response output item
	 * @return message text, or {@code null} when unavailable
	 */
	private String extractMessageText(ResponseOutputItem item) {
		if (!item.isMessage()) {
			return null;
		}
		ResponseOutputMessage outMessage = item.asMessage();
		for (Content content : outMessage.content()) {
			Optional<com.openai.models.responses.ResponseOutputText> maybeOutputText = content.outputText();
			if (maybeOutputText.isPresent()) {
				ResponseOutputText responseOutputText = maybeOutputText.get();
				if (responseOutputText != null) {
					try {
						String candidate = responseOutputText.text();
						if (StringUtils.isNotBlank(candidate)) {
							return candidate;
						}
					} catch (Exception e) {
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Builds the request parameters for the OpenAI Responses API.
	 *
	 * @param inputs input items to send
	 * @return immutable request parameters
	 */
	private ResponseCreateParams createResponseBuilder(List<ResponseInputItem> inputs) {
		Builder builder = ResponseCreateParams.builder().model(chatModel);

		if (maxToolCalls > 0) {
			builder.maxToolCalls(maxToolCalls);
		}
		builder.maxOutputTokens(maxOutputTokens);
		builder.instructions(instructions);
		builder.inputOfResponse(inputs);

		if (Strings.CS.startsWithAny(chatModel, "gpt-5.5")) {
			builder.reasoning(Reasoning.builder().effort(ReasoningEffort.NONE).build());
		}

		builder.tools(new ArrayList<>(toolMap.keySet()));
		return builder.build();
	}

	/**
	 * Executes a function tool call by finding a registered tool with the same name
	 * and delegating to its handler.
	 *
	 * <p>
	 * The handler is invoked with an {@code Object[]} of length 2:
	 * </p>
	 * <ol>
	 * <li>index {@code 0}: parsed JSON arguments as a Jackson {@link JsonNode}</li>
	 * <li>index {@code 1}: the configured {@link #setWorkingDir(File)} value</li>
	 * </ol>
	 *
	 * @param functionCall call details
	 * @return result from function handler, or {@code null} if no matching tool is
	 *         registered
	 * @throws IllegalArgumentException if the tool call arguments cannot be parsed
	 */
	private String callFunction(ResponseFunctionToolCall functionCall) {
		String name = functionCall.name();
		try {
			JsonNode params = new ObjectMapper().readTree(functionCall.arguments());
			File file = projectDir;
			Set<Entry<Tool, ToolFunction>> entrySet = toolMap.entrySet();
			String result = null;
			for (Entry<Tool, ToolFunction> entry : entrySet) {
				if (entry.getValue() != null
						&& normalize(name).equals(normalize(normalize(entry.getKey().asFunction().name())))) {
					result = safelyInvokeTool(name, entry.getValue(), params, file);
					break;
				}
			}
			return result;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Clears all accumulated inputs for the next request.
	 */
	@Override
	public void clear() {
		inputs.clear();
	}

	/**
	 * Returns the underlying OpenAI client.
	 *
	 * @return OpenAI client
	 */
	public OpenAIClient getClient() {
		String baseUrl = config.get("OPENAI_BASE_URL");
		String privateKey = config.get("OPENAI_API_KEY");
		timeoutSec = config.getLong("GENAI_TIMEOUT", 0L);

		OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();
		clientBuilder.apiKey(privateKey);
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

		OpenAIClient client = clientBuilder.build();

		if (StringUtils.isBlank(chatModel)) {
			ModelService models = client.models();
			List<String> items = models.list().items().stream().map(Model::id).collect(Collectors.toList());
			throw new IllegalArgumentException(
					"LLM Model name is required. Model list: " + StringUtils.join(items, ", "));
		}
		return client;
	}

	/**
	 * Extracts token usage from the response and stores it as {@link #usage()}.
	 *
	 * @param usage optional usage information from the OpenAI response
	 */
	protected void captureUsage(Optional<ResponseUsage> usage) {
		if (usage.isPresent()) {
			ResponseUsage responseUsage = usage.get();
			long inputTokens = responseUsage.inputTokens();
			long inputCachedTokens = responseUsage.inputTokensDetails().cachedTokens();
			long outputTokens = responseUsage.outputTokens();

			lastUsage = new Usage(inputTokens, inputCachedTokens, outputTokens);
			UsageStatistics.addUsage(chatModel, lastUsage);
		} else {
			lastUsage = new Usage(0, 0, 0);
		}
	}

	protected void logInputsSpec(Writer streamWriter) throws IOException {
		for (ResponseInputItem responseInputItem : inputs) {
			String inputText = "";
			if (responseInputItem.isMessage()) {
				ResponseInputContent responseInputContent = responseInputItem.asMessage().content().get(0);
				if (responseInputContent.isValid()) {
					if (responseInputContent.isInputText()) {
						inputText = responseInputContent.inputText().map(t -> t.text()).orElse(StringUtils.EMPTY);
					} else if (responseInputContent.isInputFile()) {
						String url = responseInputContent.inputFile().flatMap(ResponseInputFile::fileUrl)
								.orElse(StringUtils.EMPTY);
						inputText = "Add resource by URL: " + url;
					}
				} else {
					inputText = "Data invalid: " + responseInputItem;
				}
				streamWriter.write(inputText);
				streamWriter.write(AbstractAIProvider.LOG_SECTION_SEPARATOR);
			}
		}
	}

	/**
	 * Requests an embedding vector for the given input text.
	 *
	 * @param text       input to embed
	 * @param dimensions number of dimensions requested from the embedding model
	 * @return embedding as a list of {@code double} values, or {@code null} when
	 *         {@code text} is {@code null}
	 */
	@Override
	public List<Double> embedding(String text, long dimensions) {
		List<Double> embedding = null;
		if (text != null) {
			EmbeddingCreateParams params = EmbeddingCreateParams.builder().input(text).model(chatModel)
					.dimensions(dimensions).build();
			CreateEmbeddingResponse response = getClient().embeddings().create(params);

			embedding = response.data().get(0).embedding().stream().map(Double::valueOf).collect(Collectors.toList());
		}

		return embedding;
	}

}
