package org.machanism.machai.mcp;

import java.util.Map;
import java.util.function.BiFunction;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Builder for creating tool and specification objects for the Remote MCP server.
 * <p>
 * Implements {@link ToolSpecificationBuilder} for the {@code McpTransportContext} exchange type,
 * producing tool specifications compatible with {@code McpStatelessServerFeatures.SyncToolSpecification}.
 * </p>
 * @since 1.1.15
 */
public class RemoteToolSpecificationBuilder
        implements ToolSpecificationBuilder<io.modelcontextprotocol.common.McpTransportContext> {

    /**
     * Builds a {@link McpSchema.Tool} instance with the given name and schema.
     *
     * @param name   the tool name
     * @param schema the tool schema as a map
     * @return a built {@link McpSchema.Tool} object
     */
    @Override
    public Object buildTool(String name, Map<String, Object> schema) {
        return io.modelcontextprotocol.spec.McpSchema.Tool.builder(name, schema).build();
    }

    /**
     * Builds a {@code SyncToolSpecification} for the Remote MCP server.
     *
     * @param tool        the tool object (should be a {@link McpSchema.Tool})
     * @param callHandler the handler function for tool invocation
     * @return a built {@code SyncToolSpecification} object
     */
    @Override
    public Object buildSpecification(Object tool,
            BiFunction<io.modelcontextprotocol.common.McpTransportContext, McpSchema.CallToolRequest, McpSchema.CallToolResult> callHandler) {
        return io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification.builder()
                .tool((McpSchema.Tool) tool)
                .callHandler(callHandler)
                .build();
    }
}