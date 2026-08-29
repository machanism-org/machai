package org.machanism.machai.mcp.server;

/**
 * Indicates that an MCP server could not be started.
 */
public class McpServerStartupException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the startup failure as its cause.
     *
     * @param cause underlying server startup failure
     */
    public McpServerStartupException(Exception cause) {
        super("Unable to start the MCP server.", cause);
    }
}
