package org.machanism.machai.ai.manager;

/**
 * Applies a standard set of local system tools to a {@link GenAIProvider}.
 *
 * <p>This class is a small convenience wrapper that installs both {@link FileFunctionTools} (file
 * read/write/list operations) and {@link CommandFunctionTools} (shell command execution) onto a provider
 * instance via {@link GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 * new SystemFunctionTools().applyTools(provider);
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
public class SystemFunctionTools {

	/** Provides file system utility methods for GenAIProvider environments. */
	private final FileFunctionTools fileFunctionTools;

	/** Provides shell command utility methods for GenAIProvider environments. */
	private final CommandFunctionTools commandFunctionTools;

	/**
	 * Creates a tool installer that applies both file and command tools.
	 */
	public SystemFunctionTools() {
		this.fileFunctionTools = new FileFunctionTools();
		this.commandFunctionTools = new CommandFunctionTools();
	}

	/**
	 * Attaches both file and command tool capabilities to the specified provider.
	 *
	 * @param provider the provider instance to augment with tool functions
	 */
	public void applyTools(GenAIProvider provider) {
		fileFunctionTools.applyTools(provider);
		commandFunctionTools.applyTools(provider);
	}

}
