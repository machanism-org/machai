package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

/**
 * Interface for generic AI providers supplying prompt handling, file
 * operations, embeddings, and dynamic extension via function tools within the
 * Machanism AI client framework.
 * <p>
 * Implementations of this interface must provide concrete logic for interacting
 * with different AI models, allowing prompt submission as text or files,
 * managing internal resources, supporting file operations (including remote
 * files via URL), computing embeddings, and adding custom tool functions at
 * runtime.
 * <p>
 * <b>Main Features:</b>
 * <ul>
 * <li>Prompt support for both text and file-based input.</li>
 * <li>Resource management for files and model-specific state.</li>
 * <li>Extensibility through custom tool functions accepting
 * <code>JsonNode</code> arguments.</li>
 * <li>Ability to clear internal state and select model/instructions
 * dynamically.</li>
 * <li>Working directory configuration and input event logging.</li>
 * </ul>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo");
 * provider.prompt("Hello!");
 * provider.addFile(new File("some.txt"));
 * List&lt;Float&gt; embedding = provider.embedding("any text");
 * </pre>
 *
 * @author Viktor Tovstyi
 */
public interface GenAIProvider {

	/**
	 * Sends a prompt string to the underlying AI provider.
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
	 * Adds a file resource for AI provider processing.
	 *
	 * @param file File to be added
	 * @throws IOException           For IO/file errors
	 * @throws FileNotFoundException If file cannot be found
	 */
	void addFile(File file) throws IOException, FileNotFoundException;

	/**
	 * Adds a remote file via URL for processing by the provider.
	 *
	 * @param fileUrl URL of the file
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
	 * Clears all stored files and provider state.
	 */
	void clear();

	/**
	 * Adds a custom function tool to the provider for runtime invocation.
	 *
	 * @param name        Tool/function name
	 * @param description Description of function/tool
	 * @param function    Function accepting Object arguments and returning a result
	 * @param paramsDesc  Parameter descriptions
	 */
	void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc);

	/**
	 * Sets usage or setup instructions for the session.
	 *
	 * @param instructions Instructions text
	 */
	void instructions(String instructions);

	/**
	 * Performs the main provider action (typically triggers model output).
	 *
	 * @return Output result or response
	 */
	String perform();

	/**
	 * Logs input events to a (temporary) directory.
	 *
	 * @param bindexTempDir Directory to write log files
	 */
	void inputsLog(File bindexTempDir);

	/**
	 * Selects the model name for this provider instance.
	 *
	 * @param chatModelName Name of the model to use
	 */
	void model(String chatModelName);

	/**
	 * Configures the working directory for file and tool operations.
	 *
	 * @param workingDir Root working directory
	 */
	void setWorkingDir(File workingDir);

	void close();

	default boolean isThreadSafe() { return true; };
}
