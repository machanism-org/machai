package org.machanism.machai.mcp;

import java.util.function.BiFunction;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Builder for creating tool and specification objects for the Remote MCP
 * server.
 * <p>
 * Implements {@link ToolSpecificationBuilder} for the
 * {@code McpTransportContext} exchange type, producing tool specifications
 * compatible with {@code McpStatelessServerFeatures.SyncToolSpecification}.
 * </p>
 * 
 * @since 1.1.15
 * @author Viktor Tovstyi
 */
public class RemoteToolSpecificationBuilder implements ToolSpecificationBuilder<McpTransportContext> {

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