package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for generic AI providers (GenAIProvider) supporting prompts, file
 * operations, and tool augmentation.
 * <p>
 * Implementations should provide concrete behavior for each method, supporting
 * model-specific logic, resource/file handling, embeddings, and extensibility
 * through function tools.
 * <p>
 * Usage example:
 * 
 * <pre>
 * provider.prompt("Hello!");
 * provider.addFile(new File("some.txt"));
 * List&lt;Float&gt; emb = provider.embedding("any text");
 * </pre>
 *
 * @author Viktor Tovstyi
 */
public interface GenAIProvider {

	/**
	 * Sends a prompt string to the provider.
	 * 
	 * @param text The input prompt string
	 */
	void prompt(String text);

	/**
	 * Sends a prompt using the contents of a file.
	 * 
	 * @param file              The input file
	 * @param bundleMessageName The message identifier or bundle
	 * @throws IOException If file cannot be read
	 */
	void promptFile(File file, String bundleMessageName) throws IOException;

	/**
	 * Adds a file for processing.
	 * 
	 * @param file File to be added
	 * @throws IOException           For IO/file errors
	 * @throws FileNotFoundException If file cannot be found
	 */
	void addFile(File file) throws IOException, FileNotFoundException;

	/**
	 * Adds a file via a URL (downloads/uses remote file).
	 * 
	 * @param fileUrl URL for file
	 * @throws IOException           For IO/file errors
	 * @throws FileNotFoundException If remote file cannot be found
	 */
	void addFile(URL fileUrl) throws IOException, FileNotFoundException;

	/**
	 * Returns the embedding vector for the supplied string using the underlying
	 * model.
	 * 
	 * @param text Input string to embed
	 * @return List of floats representing the embedding
	 */
	List<Float> embedding(String text);

	/**
	 * Clears all internal state and stored files for this provider instance.
	 */
	void clear();

	/**
	 * Adds a custom function tool to the provider for runtime invocation.
	 * 
	 * @param name        Tool/function name
	 * @param description Description of function/tool
	 * @param function    The function accepting a JsonNode and returning an Object
	 * @param paramsDesc  Parameter descriptions
	 */
	void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc);

	/**
	 * Sets or updates instruction text for the provider session.
	 * 
	 * @param instructions Usage/setup instructions
	 */
	void instructions(String instructions);

	/**
	 * Performs the main action, typically runs or triggers model output.
	 * 
	 * @return Output result or response
	 */
	String perform();

	/**
	 * Logs input events, optionally into a temporary directory.
	 * 
	 * @param bindexTempDir Directory for writing logs
	 */
	void inputsLog(File bindexTempDir);

	/**
	 * Selects the model name for this provider instance.
	 * 
	 * @param chatModelName The name of the model in use
	 */
	void model(String chatModelName);

	void setWorkingDir(File workingDir);
}
