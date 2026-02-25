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
		throw new NotImplementedException();
	}

	@Override
	public void prompt(String text) {

	}

	@Override
	public void addFile(File file) throws IOException {
	}

	@Override
	public void addFile(URL fileUrl) throws IOException {
		prompt("File URL: " + fileUrl.toString());
	}

	@Override
	public String perform() {
		return null;
	}

	@Override
	public void clear() {
	}

	@Override
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
	}

	@Override
	public void instructions(String instructions) {
	}

	public void promptBundle(ResourceBundle promptBundle) {
	}

	@Override
	public void inputsLog(File inputsLog) {
	}

	@Override
	public void setWorkingDir(File workingDir) {
	}

	@Override
	public Usage usage() {
		return null;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	@Override
	public List<Double> embedding(String text, long dimensions) {
		return null;
	}
}