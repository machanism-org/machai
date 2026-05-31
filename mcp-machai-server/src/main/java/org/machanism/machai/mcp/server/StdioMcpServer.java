package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

public class StdioMcpServer {

	private final SyncSpecification<SingleSessionSyncSpecification> server;

	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	public StdioMcpServer(McpServerTransportProvider transportProvider, String name, String version) {

		server = McpServer.sync(transportProvider)
				.serverInfo(name, version)
				.capabilities(McpSchema.ServerCapabilities.builder()
						.tools(true)
						.resources(true, false)
						.prompts(true)
						.logging()
						.build());
	}

	public void tools() {
		List<SyncToolSpecification> toolSpecifications = new ArrayList<>();
		GenericGenaiAdapter<io.modelcontextprotocol.server.McpSyncServerExchange, SyncToolSpecification> stdioAdapter = new GenericGenaiAdapter<>(
				toolSpecifications, new StdioToolSpecificationBuilder());

		functionToolsLoader.applyTools(stdioAdapter, new PropertiesConfigurator());
		server.tools(toolSpecifications);
	}

	public McpSyncServer build() {
		McpSyncServer mcpSyncServer = server.build();
		Thread shutdownHook = new Thread(() -> {
			mcpSyncServer.close();
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		return mcpSyncServer;
	}

	public static void main(String[] args) {
		McpServerTransportProvider transportProvider;

		transportProvider = new StdioServerTransportProvider(new JacksonMcpJsonMapper(new JsonMapper()));

		String name = args.length > 0 ? args[0] : "mcp-machai-server";
		String version = StdioMcpServer.class.getPackage().getImplementationVersion();
		if (args.length > 1) {
			version = args[1];
		}

		StdioMcpServer mcpServer = new StdioMcpServer(transportProvider,
				name,
				version);
		mcpServer.tools();
		mcpServer.build();
	}
}