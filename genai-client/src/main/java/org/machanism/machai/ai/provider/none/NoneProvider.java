package org.machanism.machai.ai.provider.none;

import java.io.File;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ToolFunction;

public class NoneProvider implements Genai {

	/**
	 * Appends the given text to the prompt buffer.
	 *
	 * <p>
	 * Each call appends the provided text followed by a blank line.
	 *
	 * @param text prompt text to append
	 */
	@Override
	public void prompt(String text) {
	}

	/**
	 * Clears the accumulated prompt buffer.
	 */
	@Override
	public void clear() {
	}

	/**
	 * Registers a tool.
	 *
	 * <p>
	 * This provider does not support tools; the registration is ignored.
	 *
	 * @param name        tool name
	 * @param description tool description
	 * @param function    tool implementation
	 * @param paramsDesc  optional parameter descriptions
	 */
	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
	}

	/**
	 * Sets instructions to be written during {@link #perform()} (when
	 * {@link #inputsLog(File)} is set).
	 *
	 * @param instructions instruction text
	 */
	@Override
	public void instructions(String instructions) {
	}

	/**
	 * Writes instructions (when set) and accumulated prompts to local files when
	 * {@link #inputsLog(File)} is set.
	 *
	 * <p>
	 * If the configured {@code inputsLog} has a parent folder, it is created when
	 * missing. If the configured {@code inputsLog} has no parent, the user
	 * directory is used as the target folder for writing {@code instructions.txt}.
	 *
	 * <p>
	 * After writing, the internal prompt buffer is cleared.
	 *
	 * @return {@code null}
	 */
	@Override
	public String perform() {
		return null;
	}

	/**
	 * Configures the file used for logging prompts.
	 *
	 * @param inputsLog log file to write during {@link #perform()}
	 */
	@Override
	public void inputsLog(File inputsLog) {
	}

	/**
	 * Configures the working directory.
	 *
	 * <p>
	 * This provider does not use a working directory; the configuration is ignored.
	 *
	 * @param workingDir ignored
	 */
	@Override
	public void setWorkingDir(File workingDir) {
		// No-op.
	}

	@Override
	public void init(String model, Configurator conf) {
		// TODO Auto-generated method stub

	}
}
