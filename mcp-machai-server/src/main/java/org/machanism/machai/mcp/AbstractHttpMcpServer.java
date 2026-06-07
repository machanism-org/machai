package org.machanism.machai.mcp;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServlet;

public abstract class AbstractHttpMcpServer extends AbstractMcpServer {

	private final Logger log = LoggerFactory.getLogger(AbstractHttpMcpServer.class);

	private int port;

	private HttpServlet transportProvider;

	public AbstractHttpMcpServer() {
		super();
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

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
	}

	/**
	 * @return the transportProvider
	 */
	public HttpServlet getTransportProvider() {
		return transportProvider;
	}

	/**
	 * @param transportProvider the transportProvider to set
	 */
	public void setTransportProvider(HttpServlet transportProvider) {
		this.transportProvider = transportProvider;
	}

}