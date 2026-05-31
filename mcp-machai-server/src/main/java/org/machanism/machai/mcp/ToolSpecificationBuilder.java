package org.machanism.machai.mcp;

import java.util.Map;
import java.util.function.BiFunction;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Interface for building tool and tool specification objects for MCP servers.
 * <p>
 * Implementations of this interface are responsible for constructing tool definitions
 * and their corresponding specification objects, parameterized by the server's exchange type.
 *
 * @param <TExchange> the type representing the server exchange/context
 */
public interface ToolSpecificationBuilder<TExchange> {

    /**
     * Builds a tool object with the given name and schema.
     *
     * @param name   the tool name
     * @param schema the tool schema as a map
     * @return a tool object (implementation-specific type)
     */
    Object buildTool(String name, Map<String, Object> schema);

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