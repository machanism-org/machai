package org.machanism.machai.ai.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.machanism.machai.ai.manager.GenAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies a standard set of local system tools to a {@link GenAIProvider}.
 *
 * <p>
 * This class is a small convenience wrapper that installs both
 * {@link FileFunctionTools} (file read/write/list operations) and
 * {@link CommandFunctionTools} (shell command execution) onto a provider
 * instance via
 * {@link GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.
 *
 * <h2>Usage</h2>
 * 
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 * new SystemFunctionTools().applyTools(provider);
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
public class SystemFunctionTools {

	private static final Logger logger = LoggerFactory.getLogger(SystemFunctionTools.class);

	private static final SystemFunctionTools INSTANCE = new SystemFunctionTools();

	private final List<FunctionTools> functionTools = new ArrayList<>();

	/**
	 * Private constructor to prevent external instantiation. Creates a tool
	 * installer that applies both file and command tools.
	 */
	private SystemFunctionTools() {
		ServiceLoader<FunctionTools> functionToolServiceLoader = ServiceLoader.load(FunctionTools.class);
		for (FunctionTools functionTool : functionToolServiceLoader) {
			functionTools.add(functionTool);
			logger.info("Added FunctionTool: {}", functionTool.getClass().getName());
		}
	}

	/**
	 * Returns the singleton instance of SystemFunctionTools.
	 *
	 * @return the singleton instance
	 */
	public static SystemFunctionTools getInstance() {
		return INSTANCE;
	}

	/**
	 * Attaches both file and command tool capabilities to the specified provider.
	 *
	 * @param provider the provider instance to augment with tool functions
	 */
	public void applyTools(GenAIProvider provider) {
		for (FunctionTools functionTool : functionTools) {
			functionTool.applyTools(provider);
		}
	}
}
