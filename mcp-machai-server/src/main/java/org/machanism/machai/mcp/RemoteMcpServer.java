package org.machanism.machai.mcp;

import java.util.ArrayList;
import java.util.List;

import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.FunctionToolsLoader;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.StatelessSyncSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;

public class RemoteMcpServer {

	private final StatelessSyncSpecification server;

	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	public RemoteMcpServer(McpStatelessServerTransport transportProvider, String name, String version) {

		server = McpServer.sync(transportProvider)
				.serverInfo(name, version)
				.capabilities(McpSchema.ServerCapabilities.builder()
						.tools(true)
						.resources(false, false)
						.prompts(false)
						.logging()
						.build());
	}

	public void tools() {
		List<McpStatelessServerFeatures.SyncToolSpecification> toolSpecifications = new ArrayList<>();
		GenericGenaiAdapter<io.modelcontextprotocol.common.McpTransportContext, McpStatelessServerFeatures.SyncToolSpecification> httpAdapter = new GenericGenaiAdapter<>(
				toolSpecifications, new RemoteToolSpecificationBuilder());

		functionToolsLoader.applyTools(httpAdapter, new PropertiesConfigurator());
		server.tools(toolSpecifications);
	}

	public McpStatelessSyncServer build() {
		return server.build();
	}

}