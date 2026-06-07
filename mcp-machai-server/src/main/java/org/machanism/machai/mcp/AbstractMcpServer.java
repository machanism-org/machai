package org.machanism.machai.mcp;

import java.util.function.BiFunction;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Abstract base class for MCP (Model Context Protocol) server implementations.
 * <p>
 * Provides constants for the Machai MCP server homepage and icon, and defines
 * the {@link ToolSpecificationBuilder} interface for constructing tool and tool
 * specification objects for MCP servers.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.1.15
 */
public class AbstractMcpServer {

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
     *
     * @param <TExchange> the type representing the server exchange/context
     * @since 1.1.15
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

    /**
     * Constructs a new {@code AbstractMcpServer}.
     */
    public AbstractMcpServer() {
        super();
    }
}