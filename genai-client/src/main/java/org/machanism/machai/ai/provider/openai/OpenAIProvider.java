package org.machanism.machai.ai.provider.openai;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.provider.Genai;
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
import com.openai.services.blocking.ModelService;

/* @guidance: add to the OpenAIProvider class javadoc add information about configuration variable. */

/**
 * OpenAI-backed {@link Genai} implementation.
 *
 * <p>
 * This provider adapts the MachAI provider abstraction to the OpenAI Java SDK
 * Responses API. It supports prompting, file inputs, tool/function calling, and
 * embedding generation.
 * </p>
 *
 * <h2>Configuration</h2>
 * <p>
 * Configuration values are read from the {@link Configurator} passed to
 * {@link #init(Configurator)}.
 * </p>
 * <ul>
 * <li>{@code chatModel} (required): model identifier passed to the OpenAI
 * Responses API (for example, {@code gpt-4.1} or {@code gpt-4o}).</li>
 * <li>{@code OPENAI_API_KEY} (required): API key used to authenticate with the
 * OpenAI API.</li>
 * <li>{@code OPENAI_BASE_URL} (optional): base URL for OpenAI-compatible
 * endpoints. If unset, the SDK default base URL is used.</li>
 * <li>{@code GENAI_TIMEOUT} (optional): request timeout in seconds. If missing,
 * {@code 0}, or negative, the SDK default timeouts are used. Defaults to
 * {@value #TIMEOUT_SEC} seconds.</li>
 * <li>{@code MAX_OUTPUT_TOKENS} (optional): maximum number of output tokens.
 * Defaults to {@value #MAX_OUTPUT_TOKENS}.</li>
 * <li>{@code MAX_TOOL_CALLS} (optional): maximum number of tool calls the model
 * may issue in a single response loop. A value of {@code 0} leaves the limit
 * unset.</li>
 * <li>{@code embedding.model} (optional): embedding model identifier used by
 * {@link #embedding(String, long)}. If unset, embedding generation may fail due
 * to missing model selection.</li>
 * </ul>
 */
public class OpenAIProvider implements Genai {

	/** Logger instance for this provider. */
	private static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

	/** Default maximum number of tokens the model may generate. */
	public static final long MAX_OUTPUT_TOKENS = 18000;

	/**
	 * Default request timeout in seconds used when {@code GENAI_TIMEOUT} is unset.
	 */
	private static final long TIMEOUT_SEC = 600;

	/** Active model identifier used in {@link #perform()}. */
	private String chatModel;

	/** Maps tools to handler functions. */
	private final Map<Tool, ToolFunction> toolMap = new HashMap<>();

	/** Optional log file for input data. */
	private File inputsLog;

	/** Working directory passed to tool handlers as contextual information. */
	private File workingDir;

	/** Request timeout in seconds; {@code 0} means SDK defaults are used. */
	private long timeoutSec;

	/** Accumulated request input items for the current conversation. */
	private final List<ResponseInputItem> inputs = new ArrayList<>();

	/**
	 * Latest usage metrics captured from the most recent {@link #perform()} call.
	 */
	private Usage lastUsage = new Usage(0, 0, 0);

	/** Optional instructions applied to the request. */
	private String instructions;

	/** Maximum number of output tokens for responses. */
	private Long maxOutputTokens;

	/** Maximum number of tool calls permitted per response. */
	private Long maxToolCalls;

	private Configurator config;

	private String embeddingModel;

	/**
	 * Initializes the provider from the given configuration.
	 *
	 * @param config provider configuration (must contain {@code OPENAI_API_KEY} and
	 *               {@code chatModel})
	 */
	@Override
	public void init(Configurator config) {
		this.config = config;
		chatModel = config.get("chatModel");

		maxOutputTokens = config.getLong("MAX_OUTPUT_TOKENS", MAX_OUTPUT_TOKENS);
		maxToolCalls = config.getLong("MAX_TOOL_CALLS", 0L);
		embeddingModel = config.get("embedding.model", null);
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
	 * @return the final model response text (may be {@code null} if no text was
	 *         produced)
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
	 * Extracts token usage from the response and stores it as {@link #usage()}.
	 *
	 * @param usage optional usage information from the OpenAI response
	 */
	private void captureUsage(Optional<ResponseUsage> usage) {
		if (usage.isPresent()) {
			ResponseUsage responseUsage = usage.get();
			long inputTokens = responseUsage.inputTokens();
			long inputCachedTokens = responseUsage.inputTokensDetails().cachedTokens();
			long outputTokens = responseUsage.outputTokens();

			lastUsage = new Usage(inputTokens, inputCachedTokens, outputTokens);
			GenaiProviderManager.addUsage(lastUsage);
		} else {
			lastUsage = new Usage(0, 0, 0);
		}
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
				return text;
			}

			ResponseCreateParams params = createResponseBuilder(this.inputs);

			logger.debug("Sending follow-up request to LLM service for tool call resolution.");
			current = getClient().responses().create(params);
			captureUsage(current.usage());
		}

		return null;
	}

	private void handleFunctionCall(ResponseFunctionToolCall functionCall) {
		inputs.add(ResponseInputItem.ofFunctionCall(functionCall));

		Object value = callFunction(functionCall);
		Object callFunction = ObjectUtils.getIfNull(value, StringUtils.EMPTY);

		inputs.add(ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
				.callId(functionCall.callId()).outputAsJson(callFunction).build()));
	}

	private String extractReasoningText(ResponseOutputItem item) {
		if (!item.isReasoning()) {
			return null;
		}
		ResponseReasoningItem reasoningItem = item.asReasoning();
		Optional<List<com.openai.models.responses.ResponseReasoningItem.Content>> maybeContent = reasoningItem
				.content();
		return maybeContent.map(this::firstNonBlankReasoning).orElse(null);
	}

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

		return builder.tools(new ArrayList<>(toolMap.keySet())).build();
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
			EmbeddingCreateParams params = EmbeddingCreateParams.builder().input(text).model(embeddingModel)
					.dimensions(dimensions).build();
			CreateEmbeddingResponse response = getClient().embeddings().create(params);

			embedding = response.data().get(0).embedding().stream().map(Double::valueOf).collect(Collectors.toList());
		}

		return embedding;
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
	private Object callFunction(ResponseFunctionToolCall functionCall) {
		String name = functionCall.name();
		try {
			JsonNode params = new ObjectMapper().readTree(functionCall.arguments());
			File file = workingDir;
			Set<Entry<Tool, ToolFunction>> entrySet = toolMap.entrySet();
			Object result = null;
			for (Entry<Tool, ToolFunction> entry : entrySet) {
				if (hasSameToolName(name, entry.getKey())) {
					result = safelyInvokeTool(name, entry.getValue(), params, file);
					break;
				}
			}
			return result;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private boolean hasSameToolName(String toolName, Tool tool) {
		// Sonar java:S1874: avoid deprecated StringUtils equality helpers.
		return normalize(toolName).equals(normalize(tool.asFunction().name()));
	}

	private String normalize(String value) {
		return StringUtils.defaultString(value).toLowerCase(Locale.ROOT);
	}

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

	private void logInputs(Writer streamWriter) throws IOException {
		streamWriter.write(StringUtils.defaultString(instructions));
		streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
		streamWriter.write("-----------------------------------------");
		streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
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
				streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
				streamWriter.write("-----------------------------------------");
				streamWriter.write(Genai.PARAGRAPH_SEPARATOR);
			}
		}
		logger.debug("LLM Inputs: {}", inputsLog);
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
	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
		Map<String, Map<String, String>> fromValue = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode requiredProps = mapper.createArrayNode();
		if (paramsDesc != null) {
			for (String pDesc : paramsDesc) {
				String[] desc = StringUtils.splitPreserveAllTokens(pDesc, ":");
				if (desc.length >= 3 && isRequiredParameter(desc[2])) {
					requiredProps.add(desc[0]);
				}
				Map<String, String> value = new HashMap<>();
				value.put("type", desc[1]);
				value.put("description", desc.length > 3 ? desc[3] : StringUtils.EMPTY);
				fromValue.put(desc[0], value);
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

	private boolean isRequiredParameter(String parameterFlag) {
		// Sonar java:S1874: avoid deprecated StringUtils equality helpers.
		return normalize(parameterFlag).equals("required");
	}

	/**
	 * Sets system-level instructions applied to subsequent requests.
	 *
	 * @param instructions instruction text (may be {@code null} to clear)
	 */
	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Enables request input logging to the given file.
	 *
	 * @param inputsLog file for input logging (may be {@code null} to disable)
	 */
	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
	}

	/**
	 * Sets the working directory passed to tool handlers.
	 *
	 * @param workingDir working directory (may be {@code null})
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
	 * Returns the underlying OpenAI client.
	 *
	 * @return OpenAI client
	 */
	protected OpenAIClient getClient() {
		String baseUrl = config.get("OPENAI_BASE_URL");
		String privateKey = config.get("OPENAI_API_KEY");
		timeoutSec = config.getLong("GENAI_TIMEOUT", TIMEOUT_SEC);

		OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();
		clientBuilder.apiKey(privateKey);
		if (baseUrl != null) {
			clientBuilder.baseUrl(baseUrl);
		}
		if (timeoutSec > 0) {
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
	 * Returns the configured request timeout.
	 *
	 * @return timeout in seconds; {@code 0} indicates the SDK default
	 */
	public long getTimeout() {
		return timeoutSec;
	}

	/**
	 * Sets a request timeout (in seconds) for new clients created by this provider.
	 *
	 * @param timeout timeout in seconds; use {@code 0} to use SDK defaults
	 */
	public void setTimeout(long timeout) {
		this.timeoutSec = timeout;
	}

}
