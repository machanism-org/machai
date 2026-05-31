package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.FunctionToolsLoader;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.StatelessSyncSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
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
		McpStatelessSyncServer mcpSyncServer = server.build();
		Thread shutdownHook = new Thread(() -> {
			mcpSyncServer.close();
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		return mcpSyncServer;
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(new Option("h", "help", false, "Show this help message and exit."));
		options.addOption(new Option("n", "name", true, "Specify the MCP server name."));
		options.addOption(new Option("v", "version", true, "Specify the MCP server version."));
		options.addOption(
				Option.builder().option("p").longOpt("port").hasArg().desc("Specify the MCP listened port number.")
						.type(Integer.class).get());

		CommandLine cmd = new DefaultParser().parse(options, args);
		if (cmd.hasOption('h')) {
			HelpFormatter.builder().setShowSince(false).get().printOptions(options);
			return;
		}

		String name = cmd.getOptionValue('n', "mcp-machai-server");
		String version = cmd.getOptionValue('v',
				Objects.toString(StdioMcpServer.class.getPackage().getImplementationVersion(), "latest"));

		HttpServletStatelessServerTransport transportProvider = HttpServletStatelessServerTransport.builder().build();

		RemoteMcpServer mcpServer = new RemoteMcpServer(transportProvider,
				name,
				version);

		mcpServer.tools();
		mcpServer.build();

		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setName("server");

		Server server = new Server(threadPool);

		ServerConnector connector = new ServerConnector(server);

		if (cmd.hasOption("p")) {
			Integer port = cmd.getParsedOptionValue("p");
			connector.setPort(port);
		}

		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(transportProvider), "/*");

		server.setHandler(context);
		server.start();
	}
}