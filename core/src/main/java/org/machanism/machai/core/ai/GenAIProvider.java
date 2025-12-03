package org.machanism.machai.core.ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
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
import com.openai.models.ChatModel;
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

public class GenAIProvider {
	private static Logger logger = LoggerFactory.getLogger(GenAIProvider.class);

	private OpenAIClient client;
	private ChatModel chatModel;

	private Map<Tool, Function<JsonNode, Object>> toolMap = new HashMap<>();
	private List<ResponseInputItem> inputs = new ArrayList<ResponseInputItem>();
	private boolean debugMode;

	public GenAIProvider(ChatModel chatModel) {
		super();
		String uri = System.getenv("OPENAI_API_KEY");
		if (uri == null || uri.isEmpty()) {
			throw new RuntimeException("OPENAI_API_KEY env variable is not set or is empty.");
		}

		client = OpenAIOkHttpClient.builder().fromEnv().build();
		this.chatModel = chatModel;
	}

	public GenAIProvider prompt(String text) {
		Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
				.addInputTextContent(text).build();
		inputs.add(ResponseInputItem.ofMessage(message));
		return this;
	}

	public GenAIProvider promptFile(String description, File file) throws IOException, FileNotFoundException {
		String type = FilenameUtils.getExtension(file.getName());
		try (FileInputStream input = new FileInputStream(file)) {
			String fileData = IOUtils.toString(input, "UTF8");
			prompt(description + "```" + type + "\n" + fileData + "```");
		}
		return this;
	}

	public void addFile(File file) throws IOException, FileNotFoundException {
		try (FileInputStream input = new FileInputStream(file)) {
			FileCreateParams params = FileCreateParams.builder().file(IOUtils.toByteArray(input))
					.purpose(FilePurpose.USER_DATA).build();
			FileObject fileObject = client.files().create(params);

			com.openai.models.responses.ResponseInputFile.Builder builder = ResponseInputFile.builder()
					.fileId(fileObject.id());

			Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
					.addContent(builder.build()).build();
			inputs.add(ResponseInputItem.ofMessage(message));
		}
	}

	public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
		com.openai.models.responses.ResponseInputFile.Builder builder = ResponseInputFile.builder()
				.fileUrl(fileUrl.toString());

		Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
				.addContent(builder.build()).build();
		inputs.add(ResponseInputItem.ofMessage(message));
	}

	public String perform() {
		Builder builder = ResponseCreateParams.builder().model(chatModel);
		builder.tools(new ArrayList(toolMap.keySet()));
		builder.inputOfResponse(inputs);
		Response response = client.responses().create(builder.build());

		List<ResponseOutputItem> output = response.output();
		boolean fcall = false;
		ResponseInputItem asReasoning = null;
		for (ResponseOutputItem item : output) {
			logger.debug(">>>>" + item);

			if (item.isFunctionCall()) {
				if (asReasoning != null) {
					inputs.add(asReasoning);
				}
				ResponseFunctionToolCall functionCall = item.asFunctionCall();

				inputs.add(ResponseInputItem.ofFunctionCall(functionCall));
				Object value = callFunction(functionCall);

				Object callFunction = ObjectUtils.defaultIfNull(value, StringUtils.EMPTY);
				ResponseInputItem ofOutput = ResponseInputItem.ofFunctionCallOutput(
						ResponseInputItem.FunctionCallOutput.builder()
								.callId(functionCall.callId())
								.outputAsJson(callFunction)
								.build());
				inputs.add(ofOutput);
				fcall = true;
			}
			if (item.isMessage()) {
				asReasoning = null;
				ResponseOutputMessage outMessage = item.asMessage();
				List<Content> contentList = outMessage.content();
				for (Content content : contentList) {
					String text = content.toString();
					logger.info(text);
					Message message = com.openai.models.responses.ResponseInputItem.Message.builder().role(Role.USER)
							.addInputTextContent(text).build();
					inputs.add(ResponseInputItem.ofMessage(message));
				}
			}
			if (item.isReasoning()) {
				ResponseReasoningItem reasoningItem = item.asReasoning();
				asReasoning = ResponseInputItem.ofReasoning(reasoningItem);
				for (Summary summary : reasoningItem.summary()) {
					logger.info(summary.text());
				}

			}
		}

		if (fcall) {
			return perform();
		}

		for (ResponseOutputItem responseOutputItem : output) {
			Optional<ResponseOutputMessage> messageOpt = responseOutputItem.message();
			if (messageOpt.isPresent()) {
				Content content = messageOpt.get().content().get(0);
				String responseText = content.outputText().get().text();
				return responseText;
			}
		}

		return null;
	}

	public List<Float> embedding(String text) {
		List<Float> embedding = null;
		if (text != null) {
			EmbeddingModel embModel = EmbeddingModel.TEXT_EMBEDDING_ADA_002;
			EmbeddingCreateParams params = EmbeddingCreateParams.builder().input(text).model(embModel).build();
			CreateEmbeddingResponse response = client.embeddings().create(params);
			embedding = response.data().get(0).embedding();
		}
		return embedding;
	}

	private Object callFunction(ResponseFunctionToolCall functionCall) {
		String name = functionCall.name();
		JsonNode arguments;
		try {
			arguments = new ObjectMapper().readTree(functionCall.arguments());

			Set<Entry<Tool, Function<JsonNode, Object>>> entrySet = toolMap.entrySet();

			Object result = null;
			for (Entry<Tool, Function<JsonNode, Object>> entry : entrySet) {
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

	public void saveInput(File file) throws IOException {
		logger.info("Bindex inputs file: " + file);
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}
		try (Writer streamWriter = new FileWriter(file, false)) {
			for (ResponseInputItem responseInputItem : inputs) {
				String inputText = "";
				ResponseInputContent responseInputContent = responseInputItem.asMessage().content().get(0);
				if (responseInputContent.isValid()) {
					if (responseInputContent.isInputText()) {
						inputText = responseInputContent.inputText().get().text();
					} else if (responseInputContent.isInputFile()) {
						inputText = "Add resource by URL: " + responseInputContent.inputFile().get().fileUrl().get();
					}
				} else {
					inputText = "Data invalid: " + responseInputItem;
				}
				streamWriter.write(inputText);
				streamWriter.write("\n\n");
			}
		}
	}

	public void clear() {
		inputs.clear();
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public void addTool(String name, String description, Function<JsonNode, Object> function, String... paramsDesc) {

		Map<String, Map<String, String>> fromValue = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode requiregProps = mapper.createArrayNode();
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

		JsonValue propsVal = JsonValue.fromJsonNode(mapper.convertValue(fromValue, JsonNode.class));
		JsonValue requiredVal = JsonValue.from(mapper.createArrayNode());

		Parameters params = Parameters.builder()
				.putAdditionalProperty("properties", propsVal)
				.putAdditionalProperty("type", JsonString.of("object"))
				.putAdditionalProperty("required", requiredVal)
				.build();

		com.openai.models.responses.FunctionTool.Builder toolBuilder = FunctionTool.builder()
				.name(name)
				.description(description);

		if (params != null) {
			toolBuilder.parameters(params);
		}

		Tool tool = Tool.ofFunction(toolBuilder.strict(false).build());
		toolMap.put(tool, function);
	}

}
