package org.machanism.machai.ai.none;

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

public class NoneProvider implements GenAIProvider {
	private static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

	private StringBuilder prompts = new StringBuilder();
	private String instructions;

	private File inputsLog;

	@Override
	public void prompt(String text) {
		prompts.append(text);
		prompts.append("\r\n\r\n");
	}

	@Override
	public void promptFile(File file, String bundleMessageName) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void addFile(File file) throws IOException, FileNotFoundException {
		// TODO Auto-generated method stub
	}

	@Override
	public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Float> embedding(String text) {
		throw new IllegalArgumentException("NonProvider doesn't support embedding generation.");
	}

	@Override
	public void clear() {
		prompts = new StringBuilder();
	}

	@Override
	public void addTool(String name, String description, Function<JsonNode, Object> function,
			String... paramsDesc) {
	}

	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	@Override
	public String perform() {
		if (inputsLog != null) {
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
				streamWriter.write(prompts.toString());
				logger.info("LLM Inputs: {}", inputsLog);

			} catch (IOException e) {
				logger.error("Failed to save LLM inputs log to file: {}", inputsLog, e);
			}
		}

		return null;
	}

	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
	}

	@Override
	public void model(String chatModelName) {
	}

}
