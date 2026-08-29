package org.machanism.machai.mcp.maven.tools;

import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.mcp.server.McpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility functions for managing the MCP server lifecycle. Implements
 * {@link FunctionTools} to expose server control operations as callable
 * functions.
 */
@SupportedFor(McpServer.class)
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
	@Tool(name = "stop-mcp-server", description = "Immediately initiates shutdown of the MCP server with the specified exit code. "
			+ "Use this tool to safely stop the server. The default exit code is 0 (normal termination).")
	public String stopMcpServer(
			@Param(name = "exit-code", description = "Optional. The exit code to use when stopping the server. "
					+ "Default is 0 for normal shutdown.", defaultValue = "0") int exitCode) {
		log.info("MCP server is stopping with exit code {}...", exitCode);
		new Thread(() -> {
			try {
				waitBeforeExit();
			} catch (InterruptedException e) {
				log.error("Shutdown delay interrupted", e);
				// Sonar java:S2142: preserve the caller's interruption request.
				Thread.currentThread().interrupt();
			}
			logUsage();
			exit(exitCode);
		}).start();
		return "MCP server shutdown initiated.";
	}

	/** Waits for the shutdown grace period. */
	protected void waitBeforeExit() throws InterruptedException {
		Thread.sleep(EXIT_DELAY);
	}

	/** Records final usage statistics before shutdown. */
	protected void logUsage() {
		UsageStatistics.logUsage();
	}

	/** Ends the JVM after shutdown has been initiated. */
	protected void exit(int exitCode) {
		System.exit(exitCode);
	}

}
