package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Contract for a generative-AI provider integration.
 *
 * <p>A {@code Genai} represents a concrete implementation (for example OpenAI, Gemini, a local model, etc.) capable of:
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
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Hello!");
 * String response = provider.perform();
 *
 * provider.clear();
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
public interface Genai {

	/**
	 * Configuration property name indicating whether provider inputs should be logged.
	 */
	String LOG_INPUTS_PROP_NAME = "logInputs";

	/**
	 * Configuration property name for the target GenAI server identifier.
	 */
	String SERVERID_PROP_NAME = "genai.serverId";

	/**
	 * Environment variable name for authenticating with the GenAI provider.
	 */
	String USERNAME_PROP_NAME = "GENAI_USERNAME";

	/**
	 * Environment variable name for authenticating with the GenAI provider.
	 */
	String PASSWORD_PROP_NAME = "GENAI_PASSWORD";

	/**
	 * Line separator used when composing prompts.
	 */
	String LINE_SEPARATOR = "\n";

	/**
	 * Paragraph separator used when composing prompts.
	 */
	String PARAGRAPH_SEPARATOR = "\n\n";

	/**
	 * Functional interface representing a tool callable by a provider during a run.
	 */
	@FunctionalInterface
	interface ToolFunction {

		/**
		 * Executes the tool.
		 *
		 * @param params provider-specific parameters
		 * @return tool result (provider-specific; commonly serialized to JSON)
		 * @throws IOException if tool execution fails
		 */
		Object apply(Object[] params) throws IOException;
	}

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
	 * Computes an embedding vector for the provided text.
	 *
	 * @param text the input text
	 * @param dimensions desired embedding dimensionality (provider-specific)
	 * @return the embedding vector
	 */
	List<Double> embedding(String text, long dimensions);

	/**
	 * Clears any stored files and session/provider state.
	 */
	void clear();

	/**
	 * Registers a custom tool function that the provider may invoke at runtime.
	 *
	 * <p>The expected argument structure passed to {@code function} is provider-specific.
	 *
	 * @param name tool name (unique per provider instance)
	 * @param description human-readable description of the tool
	 * @param function function implementation; receives an argument array and returns a result
	 * @param paramsDesc parameter descriptors (format is provider-specific)
	 */
	void addTool(String name, String description, ToolFunction function, String... paramsDesc);

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

}
