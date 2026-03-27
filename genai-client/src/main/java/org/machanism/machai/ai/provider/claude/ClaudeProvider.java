package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.ai.manager.Usage;

/**
 * Anthropic-backed implementation of MachAI's {@link Genai}
 * abstraction.
 *
 * <p>
 * This provider adapts the Anthropic Java SDK to MachAI's provider interface.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 0.0.11
 */
public class ClaudeProvider implements Genai {

	@Override
	public void init(Configurator config) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void prompt(String text) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void addFile(File file) throws IOException {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void addFile(URL fileUrl) throws IOException {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public String perform() {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void instructions(String instructions) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	public void promptBundle(ResourceBundle promptBundle) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void inputsLog(File inputsLog) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void setWorkingDir(File workingDir) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public Usage usage() {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public List<Double> embedding(String text, long dimensions) {
		return Collections.emptyList();
	}
}
