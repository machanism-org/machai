package org.machanism.machai.mcp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
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

	public static void main(String[] args) throws ParseException, IOException {
		Options options = new Options();
		options.addOption(new Option("h", "help", false, "Show this help message and exit."));
		options.addOption(new Option("n", "name", true, "Specify the MCP server name."));
		options.addOption(new Option("v", "version", true, "Specify the MCP server version."));

		CommandLine cmd = new DefaultParser().parse(options, args);
		if (cmd.hasOption('h')) {
			HelpFormatter.builder().setShowSince(false).get().printOptions(options);
			return;
		}

		String name = cmd.getOptionValue("name", "mcp-machai-server");
		String version = cmd.getOptionValue("version",
				Objects.toString(StdioMcpServer.class.getPackage().getImplementationVersion(), "latest"));

		McpServerTransportProvider transportProvider = new StdioServerTransportProvider(
				new JacksonMcpJsonMapper(new JsonMapper()));
		
		StdioMcpServer mcpServer = new StdioMcpServer(transportProvider, name, version);
		mcpServer.tools();
		mcpServer.build();
	}
}