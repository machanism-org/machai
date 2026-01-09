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

/**
 * The {@code NoneProvider} class is an implementation of the {@code GenAIProvider} interface,
 * intended for use as a request logger when integration with AI services is not required or available.
 * <p>
 * This class does not interact with any external AI services or large language models (LLMs).
 * {@code NoneProvider} stores requests in input files located in the {@code inputsLog} folder,
 * which can be viewed or processed later in another process. Operations that necessarily require
 * access to GenAI services will throw an exception or do nothing if it is not a critical action.
 * </p>
 *
 * <p>
 * Typical use cases for {@code NoneProvider} include:
 * <ul>
 *   <li><b>Environments where AI services are disabled</b> (e.g., due to security or compliance requirements).</li>
 *   <li><b>Testing scenarios</b> where interaction with AI must be simulated or skipped.</li>
 *   <li><b>Default fallback</b> when no other provider is configured.</li>
 * </ul>
 * </p>
 *
 * <p>
 * By using {@code NoneProvider}, you can maintain consistent application behavior and interface compatibility
 * even when generative AI features are not used.
 * </p>
 *
 * <p>
 * This class does not interact with any AI services or LLMs. All operations are either non-operations
 * or throw exceptions when appropriate. Intended for environments where AI services are disabled,
 * for testing, or as a default backup scenario.
 * </p>
 *
 * @see GenAIProvider
 * @author Viktor Tovstyi
 */
public class NoneProvider implements GenAIProvider {
	/**
	 * The name identifying this provider.
	 */
	public static final String NAME = "None";

	private static Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);

	private StringBuilder prompts = new StringBuilder();
	private String instructions;

	private File inputsLog;

	/**
	 * Appends the given text to the prompt buffer.
	 *
	 * @param text the prompt text to add
	 */
	@Override
	public void prompt(String text) {
		prompts.append(text);
		prompts.append("\r\n\r\n");
	}

	/**
	 * Placeholder for adding a prompt from a file. No operation in this
	 * implementation.
	 *
	 * @param file              the file containing prompt text
	 * @param bundleMessageName unused, present for interface compatibility
	 * @throws IOException never thrown in this implementation
	 */
	@Override
	public void promptFile(File file, String bundleMessageName) throws IOException {
		// No-op in NoneProvider
	}

	/**
	 * Placeholder for adding a file as input. No operation in this implementation.
	 *
	 * @param file the file to add
	 * @throws IOException           never thrown in this implementation
	 * @throws FileNotFoundException never thrown in this implementation
	 */
	@Override
	public void addFile(File file) throws IOException, FileNotFoundException {
		// No-op in NoneProvider
	}

	/**
	 * Placeholder for adding a file via URL. No operation in this implementation.
	 *
	 * @param fileUrl the URL to add
	 * @throws IOException           never thrown in this implementation
	 * @throws FileNotFoundException never thrown in this implementation
	 */
	@Override
	public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
		// No-op in NoneProvider
	}

	/**
	 * Not supported for NoneProvider.
	 *
	 * @param text input for embedding
	 * @return never returns normally
	 * @throws IllegalArgumentException always thrown for this provider
	 */
	@Override
	public List<Float> embedding(String text) {
		throw new IllegalArgumentException("NoneProvider doesn't support embedding generation.");
	}

	/**
	 * Clears all accumulated prompts.
	 */
	@Override
	public void clear() {
		prompts = new StringBuilder();
	}

	/**
	 * No-op for adding tools in this stub provider.
	 *
	 * @param name        tool name
	 * @param description tool description
	 * @param function    tool function implementation
	 * @param paramsDesc  optional parameter descriptions
	 */
	@Override
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
		// No-op in NoneProvider
	}

	/**
	 * Stores the instructions to be used by the provider (if any).
	 *
	 * @param instructions the instruction text
	 */
	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Writes the prompts and instructions (if present) to files if
	 * {@code inputsLog} is set. Always returns {@code null}.
	 *
	 * @return {@code null} (No actual AI operation performed)
	 */
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
					logger.debug("LLM Instruction: {}", file);
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
		clear();
		return null;
	}

	/**
	 * Sets the file to log inputs.
	 *
	 * @param inputsLog the file used for logging
	 */
	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
	}

	/**
	 * Stub for choosing a model. No operation for NoneProvider.
	 *
	 * @param chatModelName the model name (ignored)
	 */
	@Override
	public void model(String chatModelName) {
		// No-op in NoneProvider
	}

	@Override
	public void setWorkingDir(File workingDir) {
	}

	public String getPrompts() {
		return prompts.toString();
	}

}
