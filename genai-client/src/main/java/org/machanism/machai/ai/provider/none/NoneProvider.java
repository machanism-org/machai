package org.machanism.machai.ai.provider.none;

import java.io.File;
import java.io.FileNotFoundException;
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
 *   <li>Unsupported capabilities (for example, {@link #embedding(String)}) throw an exception.</li>
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
	 * @param text prompt text to append
	 */
	@Override
	public void prompt(String text) {
		prompts.append(text);
		prompts.append("\r\n\r\n");
	}

	/**
	 * No-op.
	 *
	 * @param file              file containing prompt text
	 * @param bundleMessageName unused, present for interface compatibility
	 * @throws IOException never thrown by this implementation
	 */
	@Override
	public void promptFile(File file, String bundleMessageName) throws IOException {
		// No-op in NoneProvider
	}

	/**
	 * No-op.
	 *
	 * @param file ignored
	 * @throws IOException           never thrown by this implementation
	 * @throws FileNotFoundException never thrown by this implementation
	 */
	@Override
	public void addFile(File file) throws IOException, FileNotFoundException {
		// No-op in NoneProvider
	}

	/**
	 * No-op.
	 *
	 * @param fileUrl ignored
	 * @throws IOException           never thrown by this implementation
	 * @throws FileNotFoundException never thrown by this implementation
	 */
	@Override
	public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
		// No-op in NoneProvider
	}

	/**
	 * Unsupported by this provider.
	 *
	 * @param text input text
	 * @return never returns normally
	 * @throws UnsupportedOperationException always thrown
	 */
	@Override
	public List<Float> embedding(String text) {
		throw new UnsupportedOperationException("NoneProvider doesn't support embedding generation.");
	}

	/**
	 * Clears accumulated prompts.
	 */
	@Override
	public void clear() {
		prompts = new StringBuilder();
	}

	/**
	 * No-op.
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
	 * No-op.
	 *
	 * @param chatModelName ignored
	 */
	@Override
	public void model(String chatModelName) {
		// No-op.
	}

	/**
	 * No-op.
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
	 * Releases resources held by this provider.
	 */
	@Override
	public void close() {
		// No-op.
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
