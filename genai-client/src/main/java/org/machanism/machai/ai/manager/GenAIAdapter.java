package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import org.machanism.macha.core.commons.configurator.Configurator;

public class GenAIAdapter implements GenAIProvider {

	protected GenAIProvider provider;

	public GenAIAdapter() {
		super();
	}

	protected void setProvider(GenAIProvider provider) {
		this.provider = provider;
	}

	@Override
	public void init(Configurator conf) {
		provider.init(conf);
	}

	@Override
	public void prompt(String text) {
		provider.prompt(text);
	}

	@Override
	public void promptFile(File file, String bundleMessageName) throws IOException {
		provider.promptFile(file, bundleMessageName);
	}

	@Override
	public void addFile(File file) throws IOException, FileNotFoundException {
		provider.addFile(file);
	}

	@Override
	public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
		provider.addFile(fileUrl);
	}

	@Override
	public List<Float> embedding(String text) {
		return provider.embedding(text);
	}

	@Override
	public void clear() {
		provider.clear();
	}

	@Override
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
		provider.addTool(name, description, function, paramsDesc);
	}

	@Override
	public void instructions(String instructions) {
		provider.instructions(instructions);
	}

	@Override
	public String perform() {
		return provider.perform();
	}

	@Override
	public void inputsLog(File bindexTempDir) {
		provider.inputsLog(bindexTempDir);
	}

	@Override
	public void setWorkingDir(File workingDir) {
		provider.setWorkingDir(workingDir);
	}

	@Override
	public Usage usage() {
		return provider.usage();
	}

	@Override
	public void close() {
		provider.close();
	}

}