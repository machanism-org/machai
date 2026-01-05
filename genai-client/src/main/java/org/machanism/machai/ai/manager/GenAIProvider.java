package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

public interface GenAIProvider {

	void prompt(String text);

	void promptFile(File file, String bundleMessageName) throws IOException;

	void addFile(File file) throws IOException, FileNotFoundException;

	void addFile(URL fileUrl) throws IOException, FileNotFoundException;

	List<Float> embedding(String text);

	void clear();

	void addTool(String name, String description, Function<JsonNode, Object> function, String... paramsDesc);

	void instructions(String instructions);

	String perform();

	void inputsLog(File bindexTempDir);

	void model(String chatModelName);

}