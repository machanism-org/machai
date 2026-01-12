package org.machanism.machai.ai.manager;

/**
 * Provides unified access to file and command function tools for GenAIProvider environments.
 * <p>
 * This class serves as a wrapper around {@link FileFunctionTools} and {@link CommandFunctionTools}, enabling
 * simplified integration and setup of file system and shell command capabilities for any GenAIProvider instance.
 * <p>
 * <b>Main Features:</b>
 * <ul>
 *     <li>Streamlined attachment of file and command tools to GenAIProvider instances.</li>
 *     <li>Supports configuration of utility functions for file operations and secure shell command execution.</li>
 * </ul>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * SystemFunctionTools tools = new SystemFunctionTools();
 * tools.applyTools(provider);
 * </pre>
 *
 * @author Viktor Tovstyi
 */
public class SystemFunctionTools {

    /** Provides file system utility methods for GenAIProvider environments. */
    private FileFunctionTools fileFunctionTools;

    /** Provides shell command utility methods for GenAIProvider environments. */
    private CommandFunctionTools commandFunctionTools;

    /**
     * Initializes unified system function tools for a working directory.
     */
    public SystemFunctionTools() {
        super();
        fileFunctionTools = new FileFunctionTools();
        commandFunctionTools = new CommandFunctionTools();
    }

    /**
     * Attaches both file and command function tool capabilities to the specified GenAIProvider.
     *
     * @param provider GenAIProvider instance to be augmented with tool functions
     */
    public void applyTools(GenAIProvider provider) {
        fileFunctionTools.applyTools(provider);
        commandFunctionTools.applyTools(provider);
    }

}
