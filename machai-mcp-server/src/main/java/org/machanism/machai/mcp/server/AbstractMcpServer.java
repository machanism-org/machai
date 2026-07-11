package org.machanism.machai.mcp.server;

import java.io.File;
import java.util.function.BiFunction;

import org.machanism.macha.core.commons.configurator.Configurator;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Base implementation of a Model Context Protocol (MCP) server.
 * <p>
 * This class provides common hooks, constants, and working directory management
 * for custom MCP server implementations designed to interact with generative-AI models.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public abstract class AbstractMcpServer {

	/**
	 * The homepage URL for the Machai MCP server.
	 */
	public static final String MACHAI_MACHANISM_HOMEPAGE = "https://machai.machanism.org/mcp-machai-server/index.html";

	/**
	 * The icon URL for the Machai MCP server.
	 */
	public static final String MACHAI_MACHANISM_ICON = "https://machai.machanism.org/images/logo-180x180.png";

	/**
	 * Interface for building tool and tool specification objects for MCP servers.
	 * <p>
	 * Implementations of this interface are responsible for constructing tool
	 * definitions and their corresponding specification objects, parameterized by
	 * the server's exchange type.
	 * </p>
	 */
	interface ToolSpecificationBuilder<TExchange> {

		/**
		 * Builds a tool specification object with the given tool and call handler.
		 *
		 * @param tool        the tool object (implementation-specific type)
		 * @param callHandler the handler function for tool invocation
		 * @return a tool specification object (implementation-specific type)
		 */
		Object buildSpecification(Object tool,
				BiFunction<TExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> callHandler);
	}

	private File projectDir;

	/**
	 * Constructs a new {@code AbstractMcpServer}.
	 */
	AbstractMcpServer() {
		super();
	}

	/**
	 * Registers or defines the available tools for this MCP server.
	 * <p>
	 * Subclasses must implement this method to provide tool registration logic.
	 * </p>
	 */
	abstract void tools(Configurator config);

	/**
	 * Starts the MCP server.
	 * <p>
	 * Subclasses must implement this method to provide server startup logic.
	 * </p>
	 *
	 * @throws Exception if the server fails to start
	 */
	abstract void start() throws Exception;

	/**
	 * Sets the project directory used by server tools.
	 *
	 * @param projectDir project directory to use, or {@code null} when
	 *                   request-specific
	 */
	public void setProjectDir(File projectDir) {
		this.projectDir = projectDir;
	}

	/**
	 * Returns the configured project directory.
	 *
	 * @return project directory, or {@code null} when request-specific
	 */
	public File getProjectDir() {
		return projectDir;
	}

}
