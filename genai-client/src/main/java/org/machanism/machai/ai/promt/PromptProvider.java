package org.machanism.machai.ai.promt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

import org.apache.commons.lang.NotImplementedException;
import org.machanism.machai.ai.manager.GenAIProvider;

import com.fasterxml.jackson.databind.JsonNode;

public class PromptProvider implements GenAIProvider {

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
	public void addFile(File file) throws IOException, FileNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Float> embedding(String text) {
		throw new NotImplementedException();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addTool(String name, String description, Function<JsonNode, Object> function, String... paramsDesc) {
		// TODO Auto-generated method stub

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

}
