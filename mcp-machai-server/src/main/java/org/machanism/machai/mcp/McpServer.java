package org.machanism.machai.mcp;

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

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import tools.jackson.databind.json.JsonMapper;

public class McpServer {

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

		if (cmd.hasOption("p")) {
			HttpServletStatelessServerTransport transportProvider = HttpServletStatelessServerTransport.builder()
					.build();

			RemoteMcpServer mcpServer = new RemoteMcpServer(transportProvider,
					name,
					version);

			mcpServer.tools();
			mcpServer.build();

			QueuedThreadPool threadPool = new QueuedThreadPool();
			threadPool.setName("server");

			Server server = new Server(threadPool);

			ServerConnector connector = new ServerConnector(server);

			Integer port = cmd.getParsedOptionValue("p");
			connector.setPort(port);

			server.addConnector(connector);

			ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/");
			context.addServlet(new ServletHolder(transportProvider), "/*");

			server.setHandler(context);
			server.start();
		} else {
			McpServerTransportProvider transportProvider = new StdioServerTransportProvider(
					new JacksonMcpJsonMapper(new JsonMapper()));

			StdioMcpServer mcpServer = new StdioMcpServer(transportProvider, name, version);
			mcpServer.tools();
			mcpServer.build();
		}
	}

}
