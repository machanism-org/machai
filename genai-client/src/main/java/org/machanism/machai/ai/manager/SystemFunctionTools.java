package org.machanism.machai.ai.manager;

/**
 * Provides unified access to file and command function tools for GenAIProvider
 * environments.
 * <p>
 * Wrapper around {@link FileFunctionTools} and {@link CommandFunctionTools} to
 * simplify attachment and setup of tools for providers.
 * <p>
 * Usage example:
 * 
 * <pre>
 * SystemFunctionTools tools = new SystemFunctionTools(new File("/tmp"));
 * tools.applyTools(provider);
 * </pre>
 *
 * @author Viktor Tovstyi
 */
public class SystemFunctionTools {

	private FileFunctionTools fileFunctionTools;
	private CommandFunctionTools commandFunctionTools;

	/**
	 * Initializes unified system function tools for a working directory.
	 * 
	 * @param workingDir Directory for both command and file tool operations
	 */
	public SystemFunctionTools() {
		super();
		fileFunctionTools = new FileFunctionTools();
		commandFunctionTools = new CommandFunctionTools();
	}

	/**
	 * Attaches both file and command function tool capabilities to the specified
	 * GenAIProvider.
	 * 
	 * @param provider GenAIProvider instance
	 */
	public void applyTools(GenAIProvider provider) {
		fileFunctionTools.applyTools(provider);
		commandFunctionTools.applyTools(provider);
	}

}
