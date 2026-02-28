package org.machanism.machai.ai.provider.none;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op implementation of {@link GenAIProvider}.
 *
 * <p>This provider is intended for environments where no external LLM integration should be used. It accumulates
 * prompt text in memory and can optionally write instructions and prompts to local files when
 * {@link #inputsLog(File)} has been configured.
 *
 * <h2>Key characteristics</h2>
 * <ul>
 *   <li>No network calls are performed.</li>
 *   <li>{@link #perform()} always returns {@code null}.</li>
 *   <li>Unsupported capabilities (for example, {@link #embedding(String, long)}) throw
 *       {@link UnsupportedOperationException}.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = new NoneProvider();
 * provider.inputsLog(new File("./inputsLog/inputs.txt"));
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Describe the weather.");
 * provider.perform();
 * }</pre>
 */
public class NoneProvider implements GenAIProvider {
	/**
	 * Provider name used for identification in configuration.
	 */
	public static final String NAME = "None";

	private static final Logger logger = LoggerFactory.getLogger(NoneProvider.class);

	/**
	 * Buffer holding prompt text appended via {@link #prompt(String)}.
	 */
	private StringBuilder prompts = new StringBuilder();

	/**
	 * Optional instructions text written to {@code instructions.txt} when {@link #perform()} is executed.
	 */
	private String instructions;

	/**
	 * Optional log file; when set, {@link #perform()} writes prompts to this file.
	 */
	private File inputsLog;

	/**
	 * Latest usage metrics captured from the most recent {@link #perform()} call.
	 */
	private final Usage lastUsage = new Usage(0, 0, 0);

	/**
	 * Appends the given text to the prompt buffer.
	 *
	 * <p>Each call appends the provided text followed by a blank line.
	 *
	 * @param text prompt text to append
	 */
	@Override
	public void prompt(String text) {
		prompts.append(text);
		prompts.append("\r\n\r\n");
	}

	/**
	 * Ignores the provided file.
	 *
	 * @param file ignored
	 */
	@Override
	public void addFile(File file) {
		// No-op in NoneProvider
	}

	/**
	 * Ignores the provided URL.
	 *
	 * @param fileUrl ignored
	 */
	@Override
	public void addFile(URL fileUrl) {
		// No-op in NoneProvider
	}

	/**
	 * Indicates that embedding generation is not available for this provider.
	 *
	 * @param text input text
	 * @param dimensions requested embedding size
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public List<Double> embedding(String text, long dimensions) {
		throw new UnsupportedOperationException("NoneProvider doesn't support embedding generation.");
	}

	/**
	 * Clears the accumulated prompt buffer.
	 */
	@Override
	public void clear() {
		prompts = new StringBuilder();
	}

	/**
	 * Registers a tool.
	 *
	 * <p>This provider does not support tools; the registration is ignored.
	 *
	 * @param name        tool name
	 * @param description tool description
	 * @param function    tool implementation
	 * @param paramsDesc  optional parameter descriptions
	 */
	@Override
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
		// No-op in NoneProvider
	}

	/**
	 * Sets instructions to be written during {@link #perform()} (when {@link #inputsLog(File)} is set).
	 *
	 * @param instructions instruction text
	 */
	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Writes instructions (when set) and accumulated prompts to local files when {@link #inputsLog(File)} is set.
	 *
	 * <p>If the configured {@code inputsLog} has a parent folder, it is created when missing. If the configured
	 * {@code inputsLog} has no parent, the user directory is used as the target folder for writing
	 * {@code instructions.txt}.
	 *
	 * <p>After writing, the internal prompt buffer is cleared.
	 *
	 * @return {@code null}
	 */
	@Override
	public String perform() {
		if (inputsLog != null) {
			File parentFile = inputsLog.getParentFile();
			if (parentFile != null) {
				if (!parentFile.exists()) {
					boolean created = parentFile.mkdirs();
					if (!created) {
						logger.warn("Unable to create directory for inputs log: {}", parentFile);
					}
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
					logger.error("Failed to save LLM instructions to file: {}", file, e);
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
	 * Configures the file used for logging prompts.
	 *
	 * @param inputsLog log file to write during {@link #perform()}
	 */
	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
	}

	/**
	 * Configures the working directory.
	 *
	 * <p>This provider does not use a working directory; the configuration is ignored.
	 *
	 * @param workingDir ignored
	 */
	@Override
	public void setWorkingDir(File workingDir) {
		// No-op.
	}

	/**
	 * Returns token usage metrics captured from the most recent {@link #perform()} call.
	 *
	 * <p>Since this provider does not call a model, the returned usage is always zero.
	 *
	 * @return usage metrics; never {@code null}
	 */
	@Override
	public Usage usage() {
		return lastUsage;
	}

	/**
	 * Returns the accumulated prompt text.
	 *
	 * @return prompt text
	 */
	public String getPrompts() {
		return prompts.toString();
	}

	/**
	 * Initializes this provider from configuration.
	 *
	 * <p>This implementation performs no initialization and exists only for interface compatibility.
	 *
	 * @param conf configuration source (ignored)
	 */
	@Override
	public void init(Configurator conf) {
		// No-op.
	}
}
