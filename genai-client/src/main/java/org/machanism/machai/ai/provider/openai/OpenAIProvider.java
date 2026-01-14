package org.machanism.machai.ai.provider.openai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
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
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;
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
import com.openai.models.responses.ResponseReasoningItem.Summary;
import com.openai.models.responses.Tool;

/**
 * The {@code OpenAIProvider} class integrates seamlessly with the OpenAI API,
 * serving as a concrete implementation of the {@code GenAIProvider} interface.
 * 
 * This provider enables a wide range of generative AI capabilities, including:
 * <ul>
 * <li>Sending prompts and receiving responses from OpenAI Chat models.</li>
 * <li>Managing files for use in various OpenAI workflows.</li>
 * <li>Performing advanced large language model (LLM) requests, such as text
 * generation, summarization, and question answering.</li>
 * <li>Creating and utilizing vector embeddings for tasks like semantic search
 * and similarity analysis.</li>
 * </ul>
 * 
 * By abstracting the complexities of direct API interaction,
 * {@code OpenAIProvider} allows developers to leverage OpenAI’s powerful models
 * efficiently within their applications. It supports both synchronous and
 * asynchronous operations, and can be easily extended or configured to
 * accommodate different use cases and model parameters.
 * <p>
 * This class provides capabilities to send prompts, manage files, perform LLM
 * requests, and create embeddings using OpenAI Chat models.
 * </p>
 * 
 * <p>
 * <b>Environment Variables:</b><br>
 * The client automatically reads the following environment variables. You must
 * set at least {@code OPENAI_API_KEY}:
 * <ul>
 * <li>{@code OPENAI_API_KEY} (required)</li>
 * <li>{@code OPENAI_ORG_ID} (optional)</li>
 * <li>{@code OPENAI_PROJECT_ID} (optional)</li>
 * <li>{@code OPENAI_BASE_URL} (optional)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Using the CodeMie API:</b><br>
 * To use the CodeMie API, set the following environment variables:
 * <ul>
 * <li>{@code OPENAI_API_KEY}=eyJhbGciOiJSUzI1NiIsInR5c....</li>
 * <li>{@code OPENAI_BASE_URL}=https://codemie.lab.epam.com/code-assistant-api/v1</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Usage example:</b>
 * </p>
 * 
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
 * </pre>
 * 
 * <p>
 * <b>Thread safety:</b> This implementation is NOT thread-safe.
 * </p>
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class OpenAIProvider implements GenAIProvider {

	/** Logger instance for this provider. */
	private static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

	/** OpenAI client for API interactions. */
	private OpenAIClient client;
	/** Active chat model. */
	private String chatModel;

	/** Maps tools to handler functions. */
	private Map<Tool, Function<Object[], Object>> toolMap = new HashMap<>();
	/** List of prompt items for conversation. */
	private List<ResponseInputItem> inputs = new ArrayList<ResponseInputItem>();
	/** Instructions for the model, if any. */
	private String instructions;
	/** ResourceBundle for localized prompt templates. */
	private ResourceBundle promptBundle;

	/** Optional log file for input data. */
	private File inputsLog;

	private File workingDir;

	/**
	 * Adds a text prompt to the current conversation.
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
	 * Adds a prompt from a file, formatting the file content according to a
	 * ResourceBundle message.
	 * 
	 * @param file              the file containing prompt data
	 * @param bundleMessageName key for message in ResourceBundle, or null for raw
	 *                          file content
	 * @throws IOException if reading the file fails
	 */
	@Override
	public void promptFile(File file, String bundleMessageName) throws IOException {
		String type = FilenameUtils.getExtension(file.getName());
		try (FileInputStream input = new FileInputStream(file)) {
			String fileData = IOUtils.toString(input, "UTF8");
			String prompt;
			if (bundleMessageName != null) {
				if (promptBundle != null) {
					prompt = MessageFormat.format(promptBundle.getString(bundleMessageName), file.getName(), type,
							fileData);
				} else {
					prompt = String.format(bundleMessageName, file.getName(), type);
				}
			} else {
				prompt = fileData;
			}
			prompt(prompt);
		}
	}

	/**
	 * Uploads a file to OpenAI and adds its reference as input.
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

			com.openai.models.responses.ResponseInputFile.Builder builder = ResponseInputFile.builder()
					.fileId(fileObject.id());

			Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
					.addContent(builder.build()).build();
			inputs.add(ResponseInputItem.ofMessage(message));
		}
	}

	/**
	 * Adds input from a file by URL.
	 * 
	 * @param fileUrl the URL of the input file
	 * @throws IOException           on network error
	 * @throws FileNotFoundException if the file cannot be found
	 */
	@Override
	public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
		com.openai.models.responses.ResponseInputFile.Builder builder = ResponseInputFile.builder()
				.fileUrl(fileUrl.toString());

		Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
				.addContent(builder.build()).build();
		inputs.add(ResponseInputItem.ofMessage(message));
	}

	/**
	 * Performs a request using current inputs and returns the response string.
	 * 
	 * @return the model response
	 */
	@Override
	public String perform() {
		String result = null;
		Builder builder = ResponseCreateParams.builder().model(chatModel).tools(new ArrayList<Tool>(toolMap.keySet()))
				.inputOfResponse(inputs);

		if (instructions != null) {
			builder.instructions(instructions);
		}

		logInputs();

		Response response = getClient().responses().create(builder.build());
		result = parseResponse(response, instructions);
		clear();
		return result;
	}

	/**
	 * Parses the given response and recursively handles tool calls and reasoning.
	 * 
	 * @param response     response object
	 * @param instructions optional instructions
	 * @return response string, following reasoning and/or tool calls
	 */
	private String parseResponse(Response response, String instructions) {
		String result = null;
		List<ResponseOutputItem> output = response.output();
		boolean fcall = false;
		ResponseInputItem asReasoning = null;
		String text = null;
		for (ResponseOutputItem item : output) {
			if (item.isFunctionCall()) {
				if (asReasoning != null) {
					inputs.add(asReasoning);
					asReasoning = null;
				}
				ResponseFunctionToolCall functionCall = item.asFunctionCall();
				inputs.add(ResponseInputItem.ofFunctionCall(functionCall));
				Object value = callFunction(functionCall);

				Object callFunction = ObjectUtils.defaultIfNull(value, StringUtils.EMPTY);
				ResponseInputItem ofOutput = ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput
						.builder().callId(functionCall.callId()).outputAsJson(callFunction).build());
				inputs.add(ofOutput);
				fcall = true;
			}
			if (item.isMessage()) {
				asReasoning = null;
				ResponseOutputMessage outMessage = item.asMessage();
				List<Content> contentList = outMessage.content();
				for (Content content : contentList) {
					text = content.outputText().get().text();
					Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
							.addInputTextContent(text).build();
					inputs.add(ResponseInputItem.ofMessage(message));
				}
			}
			if (item.isReasoning()) {
				ResponseReasoningItem reasoningItem = item.asReasoning();
				asReasoning = ResponseInputItem.ofReasoning(reasoningItem);
				for (Summary summary : reasoningItem.summary()) {
					String txt = summary.text();
					if (StringUtils.isNotBlank(txt)) {
						logger.info(txt);
					}
				}
			}
		}
		if (fcall) {
			if (StringUtils.isNotBlank(text)) {
				logger.info(text);
			}
			result = perform();
		} else {
			result = text;
		}
		return result;
	}

	/**
	 * Requests embeddings for the given text using the current embedding model.
	 * 
	 * @param text input to embed
	 * @return embedding as a list of {@code float} values
	 */
	@Override
	public List<Float> embedding(String text) {
		List<Float> embedding = null;
		if (text != null) {
			EmbeddingModel embModel = EmbeddingModel.TEXT_EMBEDDING_ADA_002;
			EmbeddingCreateParams params = EmbeddingCreateParams.builder().input(text).model(embModel).build();
			CreateEmbeddingResponse response = getClient().embeddings().create(params);
			embedding = response.data().get(0).embedding();
		}
		return embedding;
	}

	/**
	 * Handles a function tool call from the response by looking for a registered
	 * handler.
	 * 
	 * @param functionCall call details
	 * @return result from function handler, or null if not found
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
	 * Logs the input items to a file, if logging is enabled.
	 */
	private void logInputs() {
		if (inputsLog != null) {
			File parentFile = inputsLog.getParentFile();
			if (parentFile != null && !parentFile.exists()) {
				parentFile.mkdirs();
			}
			try (Writer streamWriter = new FileWriter(inputsLog, false)) {
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
	 * Clears all input items for the next request.
	 */
	@Override
	public void clear() {
		inputs.clear();
	}

	/**
	 * Adds a tool to the current session, providing its function handler.
	 * 
	 * @param name        tool function name
	 * @param description tool description
	 * @param function    handler callback for tool execution
	 * @param paramsDesc  array of parameter descriptions in format
	 *                    name:type:required:description
	 */
	@Override
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
		Map<String, Map<String, String>> fromValue = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode requiregProps = mapper.createArrayNode();
		if (paramsDesc != null) {
			for (String pDesc : paramsDesc) {
				String[] desc = StringUtils.splitPreserveAllTokens(pDesc, ":");
				if (StringUtils.equals(desc[2], "required")) {
					requiregProps.add(desc[0]);
				}
				Map<String, String> value = new HashMap<>();
				value.put("type", desc[1]);
				value.put("description", desc[3]);
				fromValue.put(desc[0], value);
			}
		}
		JsonValue propsVal = JsonValue.fromJsonNode(mapper.convertValue(fromValue, JsonNode.class));
		JsonValue requiredVal = JsonValue.from(mapper.createArrayNode());
		Parameters params = Parameters.builder().putAdditionalProperty("properties", propsVal)
				.putAdditionalProperty("type", JsonString.of("object")).putAdditionalProperty("required", requiredVal)
				.build();
		com.openai.models.responses.FunctionTool.Builder toolBuilder = FunctionTool.builder().name(name)
				.description(description);
		if (params != null) {
			toolBuilder.parameters(params);
		}
		Tool tool = Tool.ofFunction(toolBuilder.strict(false).build());
		toolMap.put(tool, function);
	}

	/**
	 * Sets instructions for the model to use in the session.
	 * 
	 * @param instructions text for model instructions
	 */
	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Set the ResourceBundle for prompts.
	 * 
	 * @param promptBundle ResourceBundle instance
	 */
	public void promptBundle(ResourceBundle promptBundle) {
		this.promptBundle = promptBundle;
	}

	/**
	 * Set the file used for logging model inputs.
	 * 
	 * @param inputsLog file for input logging
	 */
	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
	}

	/**
	 * Initializes the chat model and OpenAI client. Requires OPENAI_API_KEY
	 * environment variable to be set.
	 * 
	 * @param chatModelName the model name (e.g., "gpt-4")
	 * @throws RuntimeException if API key is missing
	 */
	@Override
	public void model(String chatModelName) {
		this.chatModel = chatModelName;
	}

	@Override
	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	@Override
	public void close() {
		getClient().close();
	}

	protected OpenAIClient getClient() {
		if (client == null) {
			com.openai.client.okhttp.OpenAIOkHttpClient.Builder buillder = OpenAIOkHttpClient.builder();
			buillder.fromEnv();
			client = buillder.build();
		}

		return client;
	}

}
