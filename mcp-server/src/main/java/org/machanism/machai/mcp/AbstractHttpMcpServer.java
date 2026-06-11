package org.machanism.machai.mcp;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServlet;

/**
 * Abstract base class for HTTP-based MCP (Model Context Protocol) server
 * implementations.
 * <p>
 * This class provides the foundational logic for starting and configuring an
 * HTTP server using Jetty, including thread pool management, port
 * configuration, and servlet transport provider setup. Subclasses should
 * provide specific servlet implementations for handling MCP requests.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public abstract class AbstractHttpMcpServer extends AbstractMcpServer {

	/**
	 * Logger instance for server events and diagnostics.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractHttpMcpServer.class);

	/**
	 * The port number on which the HTTP server will listen.
	 */
	private int port;

	/**
	 * The HTTP servlet that acts as the transport provider for handling requests.
	 */
	private HttpServlet transportProvider;

	/**
	 * Constructs a new {@code AbstractHttpMcpServer}.
	 */
	public AbstractHttpMcpServer() {
		super();
	}

	/**
	 * Returns the port number on which the server is configured to listen.
	 *
	 * @return the port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port number for the server to listen on.
	 *
	 * @param port the port number to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Starts the MCP HTTP server with the configured port and transport provider.
	 * <p>
	 * Initializes the Jetty server, configures the thread pool, sets up the server
	 * connector, and registers the provided servlet to handle all incoming
	 * requests.
	 * </p>
	 *
	 * @throws Exception if the server fails to start
	 */
	protected void start() throws Exception {
		log.info("Starting MCP HTTP server on port {}...", getPort());

		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setName("server");

		Server server = new Server(threadPool);

		ServerConnector connector = new ServerConnector(server);

		connector.setPort(getPort());

		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(getTransportProvider()), "/*");

		server.setHandler(context);
		server.start();

		log.info("MCP HTTP server started and listening on port {}.", getPort());
		server.join();
	}

	/**
	 * Returns the HTTP servlet transport provider responsible for handling
	 * requests.
	 *
	 * @return the transport provider servlet
	 */
	public HttpServlet getTransportProvider() {
		return transportProvider;
	}

	/**
	 * Sets the HTTP servlet transport provider for handling requests.
	 *
	 * @param transportProvider the servlet to set as the transport provider
	 */
	public void setTransportProvider(HttpServlet transportProvider) {
		this.transportProvider = transportProvider;
	}

}