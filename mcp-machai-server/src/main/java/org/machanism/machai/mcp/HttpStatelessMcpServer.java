package org.machanism.machai.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.StatelessSyncSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities.Builder;

/**
 * HttpStatelessMcpServer sets up and runs a Model Context Protocol (MCP) server
 * that listens for HTTP requests on a specified port.
 * <p>
 * This server loads GenAI tools, configures server capabilities, and exposes
 * the MCP API over HTTP using Jetty.
 * </p>
 * 
 * @since 1.2.0
 * @author Viktor Tovstyi
 */
public class HttpStatelessMcpServer extends AbstractHttpMcpServer {

	private final Logger log = LoggerFactory.getLogger(HttpStatelessMcpServer.class);

	/** The MCP server specification for stateless sync operation. */
	private final StatelessSyncSpecification server;

	/** Loader for registering function-based tools. */
	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	/** HTTP transport provider for the MCP server. */
	private HttpServletStatelessServerTransport transportProvider;

	public class HttpStatelessToolSpecificationBuilder implements ToolSpecificationBuilder<McpTransportContext> {

		/**
		 * Builds a {@code SyncToolSpecification} for the Remote MCP server.
		 *
		 * @param tool        the tool object (should be a {@link McpSchema.Tool})
		 * @param callHandler the handler function for tool invocation
		 * @return a built {@code SyncToolSpecification} object
		 */
		@Override
		public SyncToolSpecification buildSpecification(Object tool,
				BiFunction<McpTransportContext, CallToolRequest, CallToolResult> callHandler) {
			return SyncToolSpecification.builder()
					.tool((McpSchema.Tool) tool)
					.callHandler(callHandler)
					.build();
		}
	}

	/**
	 * Constructs a new HttpStatelessMcpServer with the given name and version.
	 *
	 * @param name    the server name to report in the MCP API
	 * @param version the server version to report in the MCP API
	 */
	public HttpStatelessMcpServer(String name, String version) {
		super();
		log.info("Initializing HttpStatelessMcpServer: name={}, version={}", name, version);

		transportProvider = HttpServletStatelessServerTransport.builder().build();

		Builder tools = McpSchema.ServerCapabilities.builder()
				.resources(true, false)
				.prompts(true)
				.tools(true);

		server = McpServer.sync(transportProvider)
				.serverInfo(Implementation.builder(name, version)
						.websiteUrl(MACHAI_MACHANISM_HOMEPAGE)
						.description("")
						.icons(List.of(io.modelcontextprotocol.spec.McpSchema.Icon
								.builder(MACHAI_MACHANISM_ICON).build()))
						.build())
				.capabilities(tools.build());

		setTransportProvider(transportProvider);
	}

	/**
	 * Loads and registers GenAI tools with the MCP server.
	 * <p>
	 * This method uses a {@link FunctionToolsLoader} to apply tools to the server
	 * using a {@link GenericGenaiAdapter}.
	 * </p>
	 */
	public void tools() {
		log.info("Registering GenAI tools with MCP server...");

		List<McpStatelessServerFeatures.SyncToolSpecification> toolSpecifications = new ArrayList<>();
		GenericGenaiAdapter<io.modelcontextprotocol.common.McpTransportContext, McpStatelessServerFeatures.SyncToolSpecification> httpAdapter = new GenericGenaiAdapter<>(
				toolSpecifications, new HttpStatelessToolSpecificationBuilder());
		httpAdapter.init(null, new PropertiesConfigurator());
		httpAdapter.setProjectDir(getProjectDir());

		functionToolsLoader.applyTools(httpAdapter, new PropertiesConfigurator(), getClass());
		server.tools(toolSpecifications);
	}

	/**
	 * Starts the HTTP server and listens for incoming MCP requests on the specified
	 * port.
	 *
	 * @throws Exception if the server fails to start
	 */
	@Override
	public void start() throws Exception {
		server.build();

		log.info("Starting MCP HTTP server on port {}...", getPort());

		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setName("server");

		Server server = new Server(threadPool);

		ServerConnector connector = new ServerConnector(server);

		connector.setPort(getPort());

		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(transportProvider), "/*");

		server.setHandler(context);
		server.start();
		log.info("MCP HTTP server started and listening on port {}.", getPort());

		server.join();
	}

}