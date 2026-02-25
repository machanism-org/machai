package org.machanism.machai.ai.provider.openai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.Usage;
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
import com.openai.errors.BadRequestException;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
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
import com.openai.models.responses.ResponseUsage;
import com.openai.models.responses.Tool;

/* @guidance: add to the OpenAIProvider class javadoc add information about configuration variable. */

/**
 * OpenAI-backed {@link GenAIProvider} implementation.
 *
 * <p>
 * Configuration variables used by {@link #init(Configurator)}:
 * </p>
 * <ul>
 * <li>{@code chatModel}: model identifier passed to the OpenAI Responses
 * API.</li>
 * <li>{@code OPENAI_API_KEY}: API key (required).</li>
 * <li>{@code OPENAI_BASE_URL}: optional base URL for OpenAI-compatible
 * endpoints.</li>
 * <li>{@code GENAI_TIMEOUT}: optional request timeout (seconds); when {@code 0}
 * or missing, SDK defaults are used.</li>
 * <li>{@code MAX_OUTPUT_TOKENS}: optional max output token limit (defaults to
 * {@link #MAX_OUTPUT_TOKENS}).</li>
 * <li>{@code MAX_TOOL_CALLS}: optional max tool call limit (defaults to
 * {@link #MAX_TOOL_CALLS}).</li>
 * </ul>
 */
public class OpenAIProvider implements GenAIProvider {

	private static final String EMBEDDING_MODEL = "text-embedding-005";

	/** Logger instance for this provider. */
	private static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

	public static final int MAX_TOOL_CALLS = 100;

	public static final int MAX_OUTPUT_TOKENS = 65536;

	/** OpenAI client for API interactions. */
	private static OpenAIClient client;

	/** Active model identifier used in {@link #perform()}. */
	private String chatModel;

	/** Maps tools to handler functions. */
	private Map<Tool, Function<Object[], Object>> toolMap = new HashMap<>();

	/** Optional log file for input data. */
	private File inputsLog;

	/** Working directory passed to tool handlers as contextual information. */
	private File workingDir;

	/** Request timeout in seconds; {@code 0} means SDK defaults are used. */
	private long timeoutSec;

	/** Accumulated request input items for the current conversation. */
	private List<ResponseInputItem> inputs = new ArrayList<ResponseInputItem>();

	/**
	 * Latest usage metrics captured from the most recent {@link #perform()} call.
	 */
	private Usage lastUsage = new Usage(0, 0, 0);

	/**
	 * Builder used to create {@link ResponseCreateParams} for {@link #perform()}.
	 */
	private Builder builder;

	/** Optional instructions applied to the request. */
	private String instructions;

	private Long maxOutputTokens;

	private Long maxToolCalls;

	/**
	 * Initializes the provider from the given configuration.
	 *
	 * <p>
	 * The implementation reads {@code OPENAI_BASE_URL} and {@code OPENAI_API_KEY}
	 * and creates an {@link OpenAIClient}.
	 * </p>
	 *
	 * @param config provider configuration (must contain {@code OPENAI_API_KEY})
	 */
	@Override
	public void init(Configurator config) {

		chatModel = config.get("chatModel");
		maxOutputTokens = config.getLong("MAX_OUTPUT_TOKENS", MAX_OUTPUT_TOKENS);
		maxToolCalls = config.getLong("MAX_TOOL_CALLS", MAX_TOOL_CALLS);

		if (client == null) {
			String baseUrl = config.get("OPENAI_BASE_URL");
			String privateKey = config.get("OPENAI_API_KEY");
			timeoutSec = config.getLong("GENAI_TIMEOUT", 0);

			OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();
			clientBuilder.apiKey(privateKey);
			if (baseUrl != null) {
				clientBuilder.baseUrl(baseUrl);
			}
			if (timeoutSec > 0) {
				Duration ofSeconds = Duration.ofSeconds(timeoutSec);
				Timeout timeout = Timeout.builder().request(ofSeconds).read(ofSeconds).write(ofSeconds).build();
				clientBuilder.timeout(timeout);
			}
			client = clientBuilder.build();
		}

		builder = ResponseCreateParams.builder().model(chatModel);
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
	 * Uploads a file to OpenAI and adds its server-side reference to the current
	 * request inputs.
	 *
	 * @param file local file to upload
	 * @throws IOException           if reading file fails
	 * @throws FileNotFoundException if file is missing
	 */
	@Override
	public void addFile(File file) throws IOException, FileNotFoundException {
		try (FileInputStream input = new FileInputStream(file)) {
			FileCreateParams params = FileCreateParams.builder().file(IOUtils.toByteArray(input))
					.purpose(FilePurpose.USER_DATA).build();
			FileObject fileObject = getClient().files().create(params);

			ResponseInputFile.Builder inputFileBuilder = ResponseInputFile.builder().fileId(fileObject.id());

			Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
					.addContent(inputFileBuilder.build()).build();
			inputs.add(ResponseInputItem.ofMessage(message));
		}
	}

	/**
	 * Adds a file input by URL.
	 *
	 * @param fileUrl the URL of the input file
	 * @throws IOException           on network error
	 * @throws FileNotFoundException if the file cannot be found
	 */
	@Override
	public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
		ResponseInputFile.Builder inputFileBuilder = ResponseInputFile.builder().fileUrl(fileUrl.toString());

		Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
				.addContent(inputFileBuilder.build()).build();
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
		builder.maxToolCalls(maxToolCalls);
		builder.maxOutputTokens(maxOutputTokens);
		builder.instructions(instructions);
		builder.inputOfResponse(inputs);

		logInputs();

		ResponseCreateParams responseCreateParams = builder.tools(new ArrayList<Tool>(toolMap.keySet())).build();
		logger.debug("Sending request to LLM service.");

		try {
			Response response = getClient().responses().create(responseCreateParams);

			logger.debug("Received response from LLM service.");
			captureUsage(response.usage());

			return parseResponse(response);
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		}
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
			GenAIProviderManager.addUsage(lastUsage);
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
		String text = null;
		Response current = response;

		while (current != null) {
			boolean anyToolCalls = false;
			List<ResponseOutputItem> output = current.output();
			for (ResponseOutputItem item : output) {
				if (item.isFunctionCall()) {
					anyToolCalls = true;
					ResponseFunctionToolCall functionCall = item.asFunctionCall();
					inputs.add(ResponseInputItem.ofFunctionCall(functionCall));

					Object value = callFunction(functionCall);
					Object callFunction = ObjectUtils.defaultIfNull(value, StringUtils.EMPTY);

					inputs.add(ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
							.callId(functionCall.callId()).outputAsJson(callFunction).build()));
				}
				if (item.isMessage()) {
					ResponseOutputMessage outMessage = item.asMessage();
					List<Content> contentList = outMessage.content();
					for (Content content : contentList) {
						if (content.outputText().isPresent()) {
							String candidate = content.outputText().get().text();
							if (StringUtils.isNotBlank(candidate)) {
								text = candidate;
								logger.debug("LLM Response: {}", text);
							}
						}
					}
				}
			}

			if (!anyToolCalls) {
				break;
			}

			builder.inputOfResponse(inputs);
			builder.instructions(instructions);
			ResponseCreateParams followUpParams = builder.build();
			logger.debug("Sending follow-up request to LLM service for tool call resolution.");
			current = getClient().responses().create(followUpParams);
			captureUsage(current.usage());
		}

		return text;
	}

	/**
	 * Requests an embedding vector for the given input text.
	 *
	 * @param text       input to embed
	 * @param dimensions
	 * @return embedding as a list of {@code float} values, or {@code null} when
	 *         {@code text} is {@code null}
	 */
	@Override
	public List<Double> embedding(String text, long dimensions) {
		try {
			List<Double> embedding = null;
			if (text != null) {
				EmbeddingCreateParams params = EmbeddingCreateParams.builder().input(text).model(EMBEDDING_MODEL)
						.dimensions(dimensions).build();
				CreateEmbeddingResponse response = getClient().embeddings().create(params);
				
				//com.openai.models.embeddings.CreateEmbeddingResponse.Usage usage = response.usage();

				embedding = response.data().get(0).embedding().stream().map(Double::valueOf)
						.collect(Collectors.toList());
			}

			return embedding;
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		}
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
		Object[] arguments = new Object[2];
		try {
			arguments[0] = new ObjectMapper().readTree(functionCall.arguments());
			arguments[1] = workingDir;
			Set<Entry<Tool, Function<Object[], Object>>> entrySet = toolMap.entrySet();
			Object result = null;
			for (Entry<Tool, Function<Object[], Object>> entry : entrySet) {
				if (StringUtils.equals(name, entry.getKey().asFunction().name())) {
					result = entry.getValue().apply(arguments);
					break;
				}
			}
			return result;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Writes the current request inputs to {@link #inputsLog} when logging is
	 * enabled.
	 */
	private void logInputs() {
		if (inputsLog != null) {
			File parentFile = inputsLog.getParentFile();
			if (parentFile != null && !parentFile.exists()) {
				parentFile.mkdirs();
			}
			try (Writer streamWriter = new FileWriter(inputsLog, false)) {
				streamWriter.write(StringUtils.defaultString(instructions));

				for (ResponseInputItem responseInputItem : inputs) {
					String inputText = "";
					if (responseInputItem.isMessage()) {
						ResponseInputContent responseInputContent = responseInputItem.asMessage().content().get(0);
						if (responseInputContent.isValid()) {
							if (responseInputContent.isInputText()) {
								inputText = responseInputContent.inputText().get().text();
							} else if (responseInputContent.isInputFile()) {
								inputText = "Add resource by URL: "
										+ responseInputContent.inputFile().get().fileUrl().get();
							}
						} else {
							inputText = "Data invalid: " + responseInputItem;
						}
						streamWriter.write(inputText);
						streamWriter.write("\n\n");
					}
				}
				logger.debug("LLM Inputs: {}", inputsLog);
			} catch (IOException e) {
				logger.error("Failed to save LLM inputs log to file: {}", inputsLog, e);
			}
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
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
		Map<String, Map<String, String>> fromValue = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode requiredProps = mapper.createArrayNode();
		if (paramsDesc != null) {
			for (String pDesc : paramsDesc) {
				String[] desc = StringUtils.splitPreserveAllTokens(pDesc, ":");
				if (desc.length >= 3 && StringUtils.equals(desc[2], "required")) {
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
	 * Closes the underlying OpenAI client.
	 */
	@Override
	public void close() {
		getClient().close();
	}

	/**
	 * Returns the underlying OpenAI client.
	 *
	 * @return OpenAI client
	 */
	protected OpenAIClient getClient() {
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

	/**
	 * Indicates whether the provider instance is safe for concurrent use.
	 *
	 * @return {@code false}; this implementation stores per-request mutable state
	 */
	@Override
	public boolean isThreadSafe() {
		return false;
	}

}
