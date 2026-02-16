package org.machanism.machai.ai.manager;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Contract for a generative-AI provider implementation.
 *
 * <p>A {@code GenAIProvider} represents a concrete integration (for example OpenAI, Gemini, local model, etc.)
 * capable of:
 * <ul>
 *   <li>collecting prompts and system instructions for a conversation,</li>
 *   <li>attaching local or remote files for provider-side processing,</li>
 *   <li>computing embedding vectors,</li>
 *   <li>registering tool functions that may be invoked during a run.</li>
 * </ul>
 *
 * <p>Implementations may keep session state between calls. Use {@link #clear()} to reset conversation state.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Hello!");
 * String response = provider.perform();
 *
 * provider.clear();
 * provider.close();
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
public interface GenAIProvider extends Closeable {

	/**
	 * Initializes the provider with application configuration.
	 *
	 * @param conf configuration source
	 */
	void init(Configurator conf);

	/**
	 * Adds a user prompt to the current session.
	 *
	 * @param text the prompt text
	 */
	void prompt(String text);

	/**
	 * Adds a user prompt using the contents of a file.
	 *
	 * @param file              the file containing prompt content
	 * @param bundleMessageName a message identifier associated with the prompt (for example, a resource bundle key)
	 * @throws IOException if the file cannot be read
	 */
	void promptFile(File file, String bundleMessageName) throws IOException;

	/**
	 * Adds a local file resource for provider processing.
	 *
	 * @param file the file to add
	 * @throws IOException           for I/O errors
	 * @throws FileNotFoundException if the file cannot be found
	 */
	void addFile(File file) throws IOException, FileNotFoundException;

	/**
	 * Adds a remote file resource for provider processing.
	 *
	 * @param fileUrl the URL of the file
	 * @throws IOException           for I/O errors
	 * @throws FileNotFoundException if the remote file cannot be found
	 */
	void addFile(URL fileUrl) throws IOException, FileNotFoundException;

	/**
	 * Computes an embedding vector for the provided text.
	 *
	 * @param text the input text
	 * @return the embedding vector
	 */
	List<Float> embedding(String text);

	/**
	 * Clears any stored files and session/provider state.
	 */
	void clear();

	/**
	 * Registers a custom tool function that the provider may invoke at runtime.
	 *
	 * <p>The expected argument structure passed to {@code function} is provider-specific.
	 *
	 * @param name        tool name (unique per provider instance)
	 * @param description human-readable description of the tool
	 * @param function    function implementation; receives an argument array and returns a result
	 * @param paramsDesc  parameter descriptors (format is provider-specific)
	 */
	void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc);

	/**
	 * Sets system/session instructions for the current conversation.
	 *
	 * @param instructions instruction text
	 */
	void instructions(String instructions);

	/**
	 * Executes the provider to produce a response based on the accumulated prompts and state.
	 *
	 * @return the provider response
	 */
	String perform();

	/**
	 * Enables logging of provider inputs to the given directory.
	 *
	 * @param bindexTempDir directory used for writing log files
	 */
	void inputsLog(File bindexTempDir);

	/**
	 * Selects the model name for this provider instance.
	 *
	 * @param chatModelName the model name to use
	 */
	void model(String chatModelName);

	/**
	 * Configures the working directory used for file and tool operations.
	 *
	 * @param workingDir the working directory
	 */
	void setWorkingDir(File workingDir);

	/**
	 * Returns token usage metrics for the most recent {@link #perform()} invocation.
	 *
	 * @return usage metrics; implementations may return zero values if not supported
	 */
	Usage usage();

	/**
	 * Releases any resources held by this provider.
	 */
	@Override
	void close();

	/**
	 * Indicates whether this provider instance is safe for concurrent use.
	 *
	 * @return {@code true} if the instance is thread-safe; {@code false} otherwise
	 */
	default boolean isThreadSafe() {
		return true;
	}
}
