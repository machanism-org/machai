package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

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
		// Sonar java:S1186 - this provider is a placeholder; fail fast until implemented.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void prompt(String text) {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void addFile(File file) throws IOException {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void addFile(URL fileUrl) throws IOException {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public String perform() {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void clear() {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void instructions(String instructions) {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	public void promptBundle(ResourceBundle promptBundle) {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void inputsLog(File inputsLog) {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void setWorkingDir(File workingDir) {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public Usage usage() {
		// Sonar java:S1186 - not implemented yet.
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public List<Double> embedding(String text, long dimensions) {
		// Sonar java:S1168 - return empty collection instead of null
		return Collections.emptyList();
	}
}
