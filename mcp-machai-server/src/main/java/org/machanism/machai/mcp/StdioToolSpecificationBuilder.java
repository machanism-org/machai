package org.machanism.machai.mcp;

import java.util.function.BiFunction;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Builder for creating tool and specification objects for the STDIO MCP server.
 * <p>
 * Implements {@link ToolSpecificationBuilder} for the
 * {@code McpSyncServerExchange} exchange type, producing tool specifications
 * compatible with {@code McpServerFeatures.SyncToolSpecification}.
 * </p>
 * 
 * @since 1.1.15
 * @author Viktor Tovstyi
 */
public class StdioToolSpecificationBuilder implements ToolSpecificationBuilder<McpSyncServerExchange> {

	/**
	 * Builds a {@code SyncToolSpecification} for the STDIO MCP server.
	 *
	 * @param tool        the tool object (should be a {@link McpSchema.Tool})
	 * @param callHandler the handler function for tool invocation
	 * @return a built {@code SyncToolSpecification} object
	 */
	@Override
	public SyncToolSpecification buildSpecification(Object tool,
			BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> callHandler) {
		return SyncToolSpecification.builder()
				.tool((McpSchema.Tool) tool)
				.callHandler(callHandler)
				.build();
	}

}