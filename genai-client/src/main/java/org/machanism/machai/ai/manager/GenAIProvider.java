package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

public interface GenAIProvider {

	GenAIProvider prompt(String text);

	GenAIProvider promptFile(File file, String bundleMessageName) throws IOException;

	GenAIProvider addFile(File file) throws IOException, FileNotFoundException;

	GenAIProvider addFile(URL fileUrl) throws IOException, FileNotFoundException;

	List<Float> embedding(String text);

	GenAIProvider clear();

	GenAIProvider addTool(String name, String description, Function<JsonNode, Object> function, String... paramsDesc);

	GenAIProvider instructions(String instructions);

	GenAIProvider promptBundle(ResourceBundle promptBundle);

	String perform(boolean callLLM);

	GenAIProvider inputsLog(File bindexTempDir);

	GenAIProvider model(String chatModelName);

}