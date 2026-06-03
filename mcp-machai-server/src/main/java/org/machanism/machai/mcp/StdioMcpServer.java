package org.machanism.machai.mcp;

import java.util.ArrayList;
import java.util.List;

import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.FunctionToolsLoader;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import tools.jackson.databind.json.JsonMapper;

/**
 * StdioMcpServer sets up and runs a Model Context Protocol (MCP) server that
 * communicates via standard input and output (STDIO).
 * <p>
 * This server loads GenAI tools, configures server capabilities, and exposes
 * the MCP API over STDIO for integration with other processes.
 * </p>
 * 
 * @since 1.1.15
 * @author Viktor Tovstyi
 */
public class StdioMcpServer {

	/** The MCP server specification for single-session sync operation. */
	private final SyncSpecification<SingleSessionSyncSpecification> server;

	/** Loader for registering function-based tools. */
	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	/**
	 * Constructs a new StdioMcpServer with the given name and version.
	 *
	 * @param name    the server name to report in the MCP API
	 * @param version the server version to report in the MCP API
	 */
	public StdioMcpServer(String name, String version) {
		McpServerTransportProvider transportProvider = new StdioServerTransportProvider(
				new JacksonMcpJsonMapper(new JsonMapper()));

		server = McpServer.sync(transportProvider)
				.serverInfo(name, version)
				.capabilities(McpSchema.ServerCapabilities.builder()
						.tools(true)
						.resources(true, false)
						.prompts(true)
						.logging()
						.build());
	}

	/**
	 * Loads and registers GenAI tools with the MCP server.
	 * <p>
	 * This method uses a {@link FunctionToolsLoader} to apply tools to the server
	 * using a {@link GenericGenaiAdapter}.
	 * </p>
	 */
	public void tools() {
		List<SyncToolSpecification> toolSpecifications = new ArrayList<>();
		GenericGenaiAdapter<io.modelcontextprotocol.server.McpSyncServerExchange, SyncToolSpecification> stdioAdapter = new GenericGenaiAdapter<>(
				toolSpecifications, new StdioToolSpecificationBuilder());

		functionToolsLoader.applyTools(stdioAdapter, new PropertiesConfigurator(), getClass());
		server.tools(toolSpecifications);
	}

	/**
	 * Builds and returns the configured {@link McpSyncServer} instance.
	 * <p>
	 * Also registers a shutdown hook to ensure the server is closed gracefully on
	 * JVM exit.
	 * </p>
	 *
	 * @return the built MCP sync server
	 */
	public McpSyncServer build() {
		McpSyncServer mcpSyncServer = server.build();
		Thread shutdownHook = new Thread(() -> {
			mcpSyncServer.close();
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		return mcpSyncServer;
	}

}