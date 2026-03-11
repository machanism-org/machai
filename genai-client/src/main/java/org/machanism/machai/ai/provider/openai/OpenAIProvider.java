package org.machanism.machai.ai.provider.openai;

import java.io.File;
import java.io.FileInputStream;
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
import com.openai.models.responses.ResponseReasoningItem;
import com.openai.models.responses.ResponseUsage;
import com.openai.models.responses.Tool;
import com.openai.services.blocking.ModelService;

/* @guidance: add to the OpenAIProvider class javadoc add information about configuration variable. */

/**
 * OpenAI-backed {@link GenAIProvider} implementation.
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
 * {@link #init(Configurator)}. The following keys are used:
 * </p>
 * <ul>
 * <li>{@code chatModel} (required): model identifier passed to the OpenAI
 * Responses API (for example, {@code gpt-4.1} or {@code gpt-4o}).</li>
 * <li>{@code OPENAI_API_KEY} (required): API key used to authenticate with the
 * OpenAI API.</li>
 * <li>{@code OPENAI_BASE_URL} (optional): base URL for OpenAI-compatible
 * endpoints. If unset, the SDK default base URL is used.</li>
 * <li>{@code GENAI_TIMEOUT} (optional): request timeout in seconds. If missing,
 * {@code 0}, or negative, the SDK default timeouts are used.</li>
 * <li>{@code MAX_OUTPUT_TOKENS} (optional): maximum number of output tokens.
 * Defaults to {@link #MAX_OUTPUT_TOKENS}.</li>
 * <li>{@code MAX_TOOL_CALLS} (optional): maximum number of tool calls allowed in
 * a single response. Defaults to {@link #MAX_TOOL_CALLS}.</li>
 * </ul>
 */
public class OpenAIProvider implements GenAIProvider {

	private static final String EMBEDDING_MODEL = "text-embedding-005";

	/** Logger instance for this provider. */
	private static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

	/** Default maximum number of tool calls allowed per response. */
	public static final long MAX_TOOL_CALLS = 200;

	/** Default maximum number of tokens the model may generate. */
	public static final long MAX_OUTPUT_TOKENS = 300000;

	private static final long TIMEOUT_SEC = 600;

	/** Active model identifier used in {@link #perform()}. */
	private String chatModel;

	/** Maps tools to handler functions. */
	private final Map<Tool, Function<Object[], Object>> toolMap = new HashMap<>();

	/** Optional log file for input data. */
	private File inputsLog;

	/** Working directory passed to tool handlers as contextual information. */
	private File workingDir;

	/** Request timeout in seconds; {@code 0} means SDK defaults are used. */
	private long timeoutSec;

	/** Accumulated request input items for the current conversation. */
	private final List<ResponseInputItem> inputs = new ArrayList<>();

	/** Latest usage metrics captured from the most recent {@link #perform()} call. */
	private Usage lastUsage = new Usage(0, 0, 0);

	/** Optional instructions applied to the request. */
	private String instructions;

	/** Maximum number of output tokens for responses. */
	private Long maxOutputTokens;

	/** Maximum number of tool calls permitted per response. */
	private Long maxToolCalls;

	private Configurator config;

	@Override
	public void init(Configurator config) {
		this.config = config;
		chatModel = config.get("chatModel");

		maxOutputTokens = config.getLong("MAX_OUTPUT_TOKENS", MAX_OUTPUT_TOKENS);
		maxToolCalls = config.getLong("MAX_TOOL_CALLS", MAX_TOOL_CALLS);
	}

	@Override
	public void prompt(String text) {
		Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
				.addInputTextContent(text).build();
		inputs.add(ResponseInputItem.ofMessage(message));
	}

	@Override
	public void addFile(File file) throws IOException {
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

	@Override
	public void addFile(URL fileUrl) throws IOException {
		ResponseInputFile.Builder inputFileBuilder = ResponseInputFile.builder().fileUrl(fileUrl.toString());

		Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
				.addContent(inputFileBuilder.build()).build();
		inputs.add(ResponseInputItem.ofMessage(message));
	}

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

	private String parseResponse(Response response) {
		String text = null;
		Response current = response;
		List<ResponseInputItem> responseInputs = new ArrayList<>(this.inputs);
		while (current != null) {
			boolean anyToolCalls = false;
			List<ResponseOutputItem> output = current.output();
			for (ResponseOutputItem item : output) {
				if (item.isFunctionCall()) {
					anyToolCalls = true;
					ResponseFunctionToolCall functionCall = item.asFunctionCall();
					responseInputs.add(ResponseInputItem.ofFunctionCall(functionCall));

					Object value = callFunction(functionCall);
					Object callFunction = ObjectUtils.defaultIfNull(value, StringUtils.EMPTY);

					responseInputs.add(ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
							.callId(functionCall.callId()).outputAsJson(callFunction).build()));
				}
				if (item.isReasoning()) {
					ResponseReasoningItem reasoningItem = item.asReasoning();
					List<com.openai.models.responses.ResponseReasoningItem.Content> contentList = reasoningItem
							.content().orElseGet(java.util.Collections::emptyList);
					for (com.openai.models.responses.ResponseReasoningItem.Content content : contentList) {
						String reasoning = content.text();
						if (StringUtils.isNotBlank(reasoning)) {
							text = reasoning;
							logger.info("LLM Reasoning: {}", text);
						}
					}
				}
				if (item.isMessage()) {
					ResponseOutputMessage outMessage = item.asMessage();
					List<Content> contentList = outMessage.content();
					for (Content content : contentList) {
						content.outputText().ifPresent(outText -> {
							String candidate = outText.text();
							if (StringUtils.isNotBlank(candidate)) {
								logger.info("LLM Response: {}", candidate);
							}
						});
						// SonarQube java:S3655 - Avoid Optional#get() without presence check.
						text = content.outputText().map(outText -> outText.text()).filter(StringUtils::isNotBlank)
								.orElse(text);
					}
				}
			}

			if (!anyToolCalls) {
				break;
			}

			ResponseCreateParams params = createResponseBuilder(responseInputs);

			logger.debug("Sending follow-up request to LLM service for tool call resolution.");
			current = getClient().responses().create(params);
			captureUsage(current.usage());
		}

		return text;
	}

	private ResponseCreateParams createResponseBuilder(List<ResponseInputItem> inputs) {
		Builder builder = ResponseCreateParams.builder().model(chatModel);

		builder.maxToolCalls(maxToolCalls);
		builder.maxOutputTokens(maxOutputTokens);
		builder.instructions(instructions);
		builder.inputOfResponse(inputs);

		return builder.tools(new ArrayList<>(toolMap.keySet())).build();
	}

	@Override
	public List<Double> embedding(String text, long dimensions) {
		List<Double> embedding = null;
		if (text != null) {
			EmbeddingCreateParams params = EmbeddingCreateParams.builder().input(text).model(EMBEDDING_MODEL)
					.dimensions(dimensions).build();
			CreateEmbeddingResponse response = getClient().embeddings().create(params);

			embedding = response.data().get(0).embedding().stream().map(Double::valueOf).collect(Collectors.toList());
		}

		return embedding;
	}

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
					try {
						result = entry.getValue().apply(arguments);
					} catch (Exception e) {
						String errMsg = "Error: The functional tool call failed while executing '" + name
								+ "'. Reason: " + e.getMessage();
						logger.error(errMsg);
						result = errMsg;
					}
					break;
				}
			}
			return result;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

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

	@Override
	public void clear() {
		inputs.clear();
	}

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

	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
	}

	@Override
	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	@Override
	public Usage usage() {
		return lastUsage;
	}

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
			List<String> items = models.list().items().stream().map(i -> i.id()).collect(Collectors.toList());
			// SonarQube java:S1612 - Replace lambda with method reference.
			throw new IllegalArgumentException(
					"LLM Model name is required. Model list: " + StringUtils.join(items, ", "));
		}
		return client;
	}

	public long getTimeout() {
		return timeoutSec;
	}

	public void setTimeout(long timeout) {
		this.timeoutSec = timeout;
	}

}
