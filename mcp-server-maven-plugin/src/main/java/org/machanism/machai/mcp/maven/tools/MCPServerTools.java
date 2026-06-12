package org.machanism.machai.mcp.maven.tools;

import org.machanism.machai.ai.tools.Function;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCPServerTools implements FunctionTools {

	private static final int EXIT_DELAY = 1000;
	/**
	 * Logger instance for server events and diagnostics.
	 */
	private final Logger log = LoggerFactory.getLogger(MCPServerTools.class);

	@Function(name = "stop_mcp_server", description = "Stop the mcp server.")
	public String stopMcpServer(@Param(name = "exitCode", description = "The exit code.", defaultValue = "0") int exitCode) {
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
