package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.FunctionToolsLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

public class ToolMcpServer {

	private final SyncSpecification server;

	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	public ToolMcpServer(McpServerTransportProvider transportProvider, String name, String version) {

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
		List<McpServerFeatures.SyncToolSpecification> toolSpecifications = new ArrayList<>();
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

	public static void main(String[] args) throws Exception {
		McpServerTransportProvider transportProvider;

		if (args.length < 2) {
			transportProvider = new StdioServerTransportProvider();
		} else {
			transportProvider = new HttpServletSseServerTransportProvider(new ObjectMapper(), "/", "/sse");
		}

		String version = Objects.toString(ToolMcpServer.class.getPackage().getImplementationVersion(), "last");
		ToolMcpServer mcpServer = new ToolMcpServer(transportProvider, args.length > 0 ? args[0] : "mcp-machai-server",
				version);
		mcpServer.tools();
		mcpServer.build();

		if (transportProvider instanceof HttpServletSseServerTransportProvider) {
			QueuedThreadPool threadPool = new QueuedThreadPool();
			threadPool.setName("server");

			Server server = new Server(threadPool);

			ServerConnector connector = new ServerConnector(server);
			connector.setPort(Integer.parseInt(args[1]));
			server.addConnector(connector);

			ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/");
			context.addServlet(new ServletHolder((HttpServletSseServerTransportProvider) transportProvider), "/*");

			server.setHandler(context);
			server.start();
		}
	}
}