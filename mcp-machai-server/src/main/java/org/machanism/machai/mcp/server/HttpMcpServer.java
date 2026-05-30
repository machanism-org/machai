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

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.StatelessSyncSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;

public class HttpMcpServer {

	private final StatelessSyncSpecification server;

	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	public HttpMcpServer(McpStatelessServerTransport transportProvider, String name, String version) {

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
		functionToolsLoader.applyTools(new GenaiAdapterExt(toolSpecifications), new PropertiesConfigurator());
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
		HttpServletStatelessServerTransport transportProvider = HttpServletStatelessServerTransport.builder().build();

		String version = Objects.toString(HttpMcpServer.class.getPackage().getImplementationVersion(), "last");
		HttpMcpServer mcpServer = new HttpMcpServer(transportProvider,
				args.length > 0 ? args[0] : "mcp-machai-server",
				version);
		mcpServer.tools();
		mcpServer.build();

		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setName("server");

		Server server = new Server(threadPool);

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(Integer.parseInt(args[1]));
		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(transportProvider), "/*");

		server.setHandler(context);
		server.start();
	}
}