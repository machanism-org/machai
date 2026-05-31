package org.machanism.machai.mcp;

import java.util.ArrayList;
import java.util.List;

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

public class RemoteMcpServer {

	private final StatelessSyncSpecification server;

	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	private HttpServletStatelessServerTransport transportProvider;

	public RemoteMcpServer(String name, String version) {
		transportProvider = HttpServletStatelessServerTransport.builder().build();

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

	public void start(int port) throws Exception {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setName("server");

		Server server = new Server(threadPool);

		ServerConnector connector = new ServerConnector(server);

		connector.setPort(port);

		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(transportProvider), "/*");

		server.setHandler(context);
		server.start();
	}

}