package org.machanism.machai.ai.provider;

import java.io.File;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Prompt;

/**
 * Contract for a generative-AI provider integration.
 * <p>
 * A {@code Genai} represents a concrete implementation (for example OpenAI,
 * Gemini, a local model, etc.) capable of:
 * </p>
 * <ul>
 * <li>Collecting prompts and system instructions for a conversation,</li>
 * <li>Attaching local or remote files for provider-side processing,</li>
 * <li>Registering tool functions that may be invoked during a run.</li>
 * </ul>
 *
 * <p>
 * Implementations may keep session state between calls. Use {@link #clear()} to
 * reset conversation state.
 * </p>
 *
 * <h2>Typical usage</h2>
 * 
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
	 * Initializes the provider with application configuration.
	 *
	 * @param model the model identifier or name to use
	 * @param conf  configuration source
	 */
	void init(String model, Configurator conf);

	/**
	 * Adds a user prompt to the current session.
	 *
	 * @param text the prompt text
	 */
	void prompt(String text);

	/**
	 * Clears any stored files and session/provider state.
	 * <p>
	 * This resets the conversation and any accumulated context.
	 * </p>
	 */
	void clear();

	/**
	 * Sets system/session instructions for the current conversation.
	 *
	 * @param instructions instruction text
	 */
	void instructions(String instructions);

	/**
	 * Executes the provider to produce a response based on the accumulated prompts
	 * and state.
	 *
	 * @return the provider response as a string
	 */
	String perform();

	/**
	 * Enables logging of provider inputs to the given directory.
	 *
	 * @param bindexTempDir directory used for writing log files
	 */
	void inputsLog(File bindexTempDir);

	/**
	 * Registers a set of tool functions that may be invoked during a run.
	 *
	 * @param tools the {@link FunctionTools} instance containing tool methods
	 */
	void addTools(FunctionTools tools);

	/**
	 * Scans the provided {@link FunctionTools} instance for methods annotated with
	 * {@link Prompt}, and registers each prompt for use during a run.
	 *
	 * @param tools the {@link FunctionTools} instance whose methods will be scanned
	 *              for {@link Prompt} annotations
	 */
	void addPrompts(FunctionTools tools);

	/**
	 * Scans the provided {@link FunctionTools} instance for methods annotated with
	 * {@code Resource}, and registers each resource for use during a run.
	 * <p>
	 * This method inspects the given class instance to register executable resource
	 * utilities that can be dynamically called by the AI model during generation
	 * processes.
	 * </p>
	 *
	 * @param tools the {@link FunctionTools} instance whose methods will be scanned
	 *              for resource annotations
	 */
	void addResources(FunctionTools tools);

	/**
	 * Sets the working directory for the provider, which may be used by tool
	 * handlers.
	 *
	 * @param projectDir the project directory
	 */
	void setProjectDir(File projectDir);

	/**
	 * Configures whether tool invocation errors should be returned to the model for
	 * conversational recovery or propagated as exceptions.
	 *
	 * @param errorHandling {@code true} to return tool errors as response text;
	 *                      {@code false} to propagate them immediately
	 */
	void setErrorHandling(boolean errorHandling);

	/**
	 * Configures the list of tool names that are enabled and allowed to be used by
	 * the AI provider.
	 * <p>
	 * If the specified array is {@code null} or empty, all registered tools are
	 * typically enabled by default. Only tools whose identifiers are present in
	 * this collection will be active during provider execution.
	 * </p>
	 *
	 * @param tools the array of unique tool names to enable; if {@code null} or
	 *              empty, all tools are enabled
	 */
	void setEnabledTools(String[] tools);
}