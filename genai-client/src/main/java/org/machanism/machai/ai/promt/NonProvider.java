package org.machanism.machai.ai.promt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

import org.machanism.machai.ai.manager.GenAIProvider;

import com.fasterxml.jackson.databind.JsonNode;

public class NonProvider implements GenAIProvider {

	@Override
	public GenAIProvider prompt(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenAIProvider promptFile(File file, String bundleMessageName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenAIProvider addFile(File file) throws IOException, FileNotFoundException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public GenAIProvider addFile(URL fileUrl) throws IOException, FileNotFoundException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public List<Float> embedding(String text) {
		throw new IllegalArgumentException("NonProvider doesn't support embedding generation.");
	}

	@Override
	public GenAIProvider clear() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public GenAIProvider addTool(String name, String description, Function<JsonNode, Object> function,
			String... paramsDesc) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public GenAIProvider instructions(String instructions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenAIProvider promptBundle(ResourceBundle promptBundle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String perform(boolean callLLM) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenAIProvider inputsLog(File bindexTempDir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenAIProvider model(String chatModelName) {
		return this;
	}

}
