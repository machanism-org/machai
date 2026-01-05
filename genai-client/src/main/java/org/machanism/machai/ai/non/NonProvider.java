package org.machanism.machai.ai.non;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.openAI.OpenAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class NonProvider implements GenAIProvider {
	private static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

	private StringBuilder prompts = new StringBuilder();
	private String instructions;

	private File inputsLog;

	@Override
	public GenAIProvider prompt(String text) {
		prompts.append(text);
		prompts.append("\r\n\r\n");
		return this;
	}

	@Override
	public GenAIProvider promptFile(File file, String bundleMessageName) throws IOException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public GenAIProvider addFile(File file) throws IOException, FileNotFoundException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public GenAIProvider addFile(URL fileUrl) throws IOException, FileNotFoundException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public List<Float> embedding(String text) {
		throw new IllegalArgumentException("NonProvider doesn't support embedding generation.");
	}

	@Override
	public GenAIProvider clear() {
		prompts = new StringBuilder();
		return this;
	}

	@Override
	public GenAIProvider addTool(String name, String description, Function<JsonNode, Object> function,
			String... paramsDesc) {
		return this;
	}

	@Override
	public GenAIProvider instructions(String instructions) {
		this.instructions = instructions;
		return this;
	}

	@Override
	public String perform() {
		File parentFile = inputsLog.getParentFile();
		if (parentFile != null) {
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
		} else {
			parentFile = SystemUtils.getUserDir();
		}

		if (instructions != null) {
			File file = new File(parentFile, "instructions.txt");
			try (Writer streamWriter = new FileWriter(file, false)) {
				streamWriter.write(instructions);
				logger.info("LLM Instruction: {}", file);

			} catch (IOException e) {
				logger.error("Failed to save LLM inputs log to file: {}", inputsLog, e);
			}
		}

		try (Writer streamWriter = new FileWriter(inputsLog, false)) {
			streamWriter.write(inputsLog.toString());
			logger.info("LLM Inputs: {}", inputsLog);

		} catch (IOException e) {
			logger.error("Failed to save LLM inputs log to file: {}", inputsLog, e);
		}

		return null;
	}

	@Override
	public GenAIProvider inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
		return this;
	}

	@Override
	public GenAIProvider model(String chatModelName) {
		return this;
	}

}
