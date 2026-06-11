package org.machanism.machai.mcp.maven.tools;

import org.machanism.machai.ai.tools.Function;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCPServerTools implements FunctionTools {

	/**
	 * Logger instance for server events and diagnostics.
	 */
	private final Logger log = LoggerFactory.getLogger(MCPServerTools.class);

	@Function(name = "stop_mcp_server", description = "Stop the mcp server.")
	public String stopMcpServer() {
		log.info("MCP server is stopping...");
		// Schedule exit after 1 second
		new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error("Shutdown delay interrupted", e);
			}
			System.exit(0);
		}).start();
		return "MCP server shutdown initiated.";
	}

}
