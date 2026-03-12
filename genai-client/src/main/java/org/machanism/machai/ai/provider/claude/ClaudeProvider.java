package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

import org.apache.commons.lang.NotImplementedException;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.Usage;

/**
 * Anthropic-backed implementation of MachAI's {@link GenAIProvider}
 * abstraction.
 *
 * <p>
 * This provider adapts the Anthropic Java SDK to MachAI's provider interface.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 0.0.11
 */
public class ClaudeProvider implements GenAIProvider {

	@Override
	public void init(Configurator config) {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new NotImplementedException();
	}

	@Override
	public void prompt(String text) {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.prompt is not implemented yet.");
	}

	@Override
	public void addFile(File file) throws IOException {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.addFile(File) is not implemented yet.");
	}

	@Override
	public void addFile(URL fileUrl) throws IOException {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.addFile(URL) is not implemented yet.");
	}

	@Override
	public String perform() {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.perform is not implemented yet.");
	}

	@Override
	public void clear() {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.clear is not implemented yet.");
	}

	@Override
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.addTool is not implemented yet.");
	}

	@Override
	public void instructions(String instructions) {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.instructions is not implemented yet.");
	}

	public void promptBundle(ResourceBundle promptBundle) {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.promptBundle is not implemented yet.");
	}

	@Override
	public void inputsLog(File inputsLog) {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.inputsLog is not implemented yet.");
	}

	@Override
	public void setWorkingDir(File workingDir) {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.setWorkingDir is not implemented yet.");
	}

	@Override
	public Usage usage() {
		// SonarQube java:S1168 - Return an empty object instead of null.
		return new Usage(0, 0, 0);
	}

	@Override
	public List<Double> embedding(String text, long dimensions) {
		// SonarQube java:S1186 - Empty method placeholder until provider is fully implemented.
		throw new UnsupportedOperationException("ClaudeProvider.embedding is not implemented yet.");
	}
}