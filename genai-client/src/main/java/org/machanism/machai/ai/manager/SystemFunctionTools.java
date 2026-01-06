package org.machanism.machai.ai.manager;

import java.io.File;

/**
 * Provides unified access to file and command function tools for GenAIProvider environments.
 * <p>
 * Wrapper around {@link FileFunctionTools} and {@link CommandFunctionTools} to simplify attachment and setup of tools for providers.
 * <p>
 * Usage example:
 * <pre>
 *   SystemFunctionTools tools = new SystemFunctionTools(new File("/tmp"));
 *   tools.applyTools(provider);
 * </pre>
 *
 * @author Viktor Tovstyi
 * @guidance
 */
public class SystemFunctionTools {

    private FileFunctionTools fileFunctionTools;
    private CommandFunctionTools commandFunctionTools;

    /**
     * Initializes unified system function tools for a working directory.
     * @param workingDir Directory for both command and file tool operations
     */
    public SystemFunctionTools(File workingDir) {
        super();
        fileFunctionTools = new FileFunctionTools(workingDir);
        commandFunctionTools = new CommandFunctionTools(workingDir);
    }

    /**
     * Attaches both file and command function tool capabilities to the specified GenAIProvider.
     * @param provider GenAIProvider instance
     */
    public void applyTools(GenAIProvider provider) {
        fileFunctionTools.applyTools(provider);
        commandFunctionTools.applyTools(provider);
    }

    /**
     * Sets working directory for both wrapped tool types.
     * @param workingDir Directory for operations
     */
    public void setWorkingDir(File workingDir) {
        fileFunctionTools.setWorkingDir(workingDir);
        commandFunctionTools.setWorkingDir(workingDir);
    }
}
