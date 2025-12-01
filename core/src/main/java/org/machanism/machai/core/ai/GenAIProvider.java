package org.machanism.machai.core.ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseCreateParams.Input;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputFile;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputItem.Message;
import com.openai.models.responses.ResponseInputItem.Message.Role;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputMessage.Content;
import com.openai.models.responses.Tool;

public class GenAIProvider {
	private static Logger logger = LoggerFactory.getLogger(GenAIProvider.class);

	private OpenAIClient client;
	private ChatModel chatModel;

	private ArrayList<Tool> tools = new ArrayList<Tool>();
	private ArrayList<ResponseInputItem> inputs = new ArrayList<ResponseInputItem>();
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
		Builder builder = ResponseCreateParams.builder().model(chatModel).input(Input.ofResponse(inputs));
		builder.tools(tools);

		String outputTest = null;
		if (!debugMode) {
			Response response = client.responses().create(builder.build());

			List<ResponseInputItem> funcInputs = new ArrayList<>();
			List<ResponseInputItem> reasoningInputs = new ArrayList<>();
			response.output().forEach(item -> {
				if (item.isFunctionCall()) {
					ResponseFunctionToolCall functionCall = item.asFunctionCall();

					funcInputs.add(ResponseInputItem.ofFunctionCall(functionCall));
					Object callFunction = ObjectUtils.defaultIfNull(callFunction(functionCall), StringUtils.EMPTY);
					funcInputs.add(ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
							.callId(functionCall.callId()).outputAsJson(callFunction).build()));
				}
				if (item.isReasoning()) {
					ResponseInputItem reasoning = ResponseInputItem.ofReasoning(item.asReasoning());
					reasoningInputs.add(reasoning);
				}
			});

			if (!funcInputs.isEmpty()) {
				inputs.addAll(reasoningInputs);
				inputs.addAll(funcInputs);
				builder.input(ResponseCreateParams.Input.ofResponse(inputs));
				response = client.responses().create(builder.build());
			}

			List<ResponseOutputItem> output = response.output();

			for (ResponseOutputItem responseOutputItem : output) {
				Optional<ResponseOutputMessage> messageOpt = responseOutputItem.message();
				if (messageOpt.isPresent()) {
					Content content = messageOpt.get().content().get(0);
					String responseText = content.outputText().get().text();
					outputTest = responseText;
					break;
				}
			}
		}

		clear();
		return outputTest;
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
		System.out.println(">>>>" + functionCall);
		return null;
	}

	public void saveInput(File file) throws IOException {
		logger.info("Bindex inputs file: " + file);
		file.getParentFile().mkdirs();
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

}
