package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.FunctionToolsLoader;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import tools.jackson.databind.json.JsonMapper;

public class StdioMcpServer {

	private final SyncSpecification server;

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
		List<McpStatelessServerFeatures.SyncToolSpecification> toolSpecifications = new ArrayList<>();
		functionToolsLoader.applyTools(new GenaiAdapterExt(toolSpecifications), new PropertiesConfigurator());
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

		String version = Objects.toString(StdioMcpServer.class.getPackage().getImplementationVersion(), "last");
		StdioMcpServer mcpServer = new StdioMcpServer(transportProvider,
				args.length > 0 ? args[0] : "mcp-machai-server",
				version);
		mcpServer.tools();
		mcpServer.build();
	}
}