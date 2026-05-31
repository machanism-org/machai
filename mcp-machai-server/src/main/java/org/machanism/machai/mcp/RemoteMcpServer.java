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

/**
 * RemoteMcpServer sets up and runs a Model Context Protocol (MCP) server
 * that listens for HTTP requests on a specified port.
 * <p>
 * This server loads GenAI tools, configures server capabilities, and
 * exposes the MCP API over HTTP using Jetty.
 * </p>
 * @since 1.1.15
 */
public class RemoteMcpServer {

    /** The MCP server specification for stateless sync operation. */
    private final StatelessSyncSpecification server;

    /** Loader for registering function-based tools. */
    private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

    /** HTTP transport provider for the MCP server. */
    private HttpServletStatelessServerTransport transportProvider;

    /**
     * Constructs a new RemoteMcpServer with the given name and version.
     *
     * @param name    the server name to report in the MCP API
     * @param version the server version to report in the MCP API
     */
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

    /**
     * Loads and registers GenAI tools with the MCP server.
     * <p>
     * This method uses a {@link FunctionToolsLoader} to apply tools
     * to the server using a {@link GenericGenaiAdapter}.
     * </p>
     */
    public void tools() {
        List<McpStatelessServerFeatures.SyncToolSpecification> toolSpecifications = new ArrayList<>();
        GenericGenaiAdapter<io.modelcontextprotocol.common.McpTransportContext, McpStatelessServerFeatures.SyncToolSpecification> httpAdapter = new GenericGenaiAdapter<>(
                toolSpecifications, new RemoteToolSpecificationBuilder());

        functionToolsLoader.applyTools(httpAdapter, new PropertiesConfigurator());
        server.tools(toolSpecifications);
    }

    /**
     * Builds and returns the configured {@link McpStatelessSyncServer} instance.
     *
     * @return the built MCP stateless sync server
     */
    public McpStatelessSyncServer build() {
        return server.build();
    }

    /**
     * Starts the HTTP server and listens for incoming MCP requests on the specified port.
     *
     * @param port the TCP port to listen on
     * @throws Exception if the server fails to start
     */
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