package org.machanism.machai.mcp.maven.tools;

import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility functions for managing the MCP server lifecycle. Implements
 * {@link FunctionTools} to expose server control operations as callable
 * functions.
 */
public class MCPServerTools implements FunctionTools {

	/**
	 * Delay in milliseconds before the server process exits after shutdown is
	 * initiated.
	 */
	private static final int EXIT_DELAY = 1000;

	/**
	 * Logger instance for server events and diagnostics.
	 */
	private final Logger log = LoggerFactory.getLogger(MCPServerTools.class);

	/**
	 * Stops the MCP server by initiating a delayed shutdown.
	 * <p>
	 * This method logs the shutdown event, waits for a predefined delay, and then
	 * exits the JVM with the specified exit code.
	 * </p>
	 *
	 * @param exitCode the exit code to use when shutting down the server (default
	 *                 is 0)
	 * @return a message indicating that the shutdown has been initiated
	 */
	@Tool(name = "stop_mcp_server", description = "Stop the mcp server.")
	public String stopMcpServer(
			@Param(name = "exit_code", description = "The exit code.", defaultValue = "0") int exitCode) {
		log.info("MCP server is stopping with exit code {}...", exitCode);
		new Thread(() -> {
			try {
				Thread.sleep(EXIT_DELAY);
			} catch (InterruptedException e) {
				log.error("Shutdown delay interrupted", e);
			}
			System.exit(exitCode);
		}).start();
		return "MCP server shutdown initiated.";
	}

}