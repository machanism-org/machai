package org.machanism.machai.mcp.server;

import java.util.List;
import java.util.function.BiFunction;

import org.machanism.machai.mcp.server.AbstractMcpServer.ToolSpecificationBuilder;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Adapts Machai tools and prompts to the synchronous STDIO MCP transport.
 *
 * @since 1.2.0
 */
public class StdioGenaiAdapter
		extends AbstractPromptGenaiAdapter<McpSyncServerExchange, SyncToolSpecification, SyncPromptSpecification> {

	/**
	 * Creates an adapter backed by the supplied tool specification collection.
	 *
	 * @param toolSpecifications collection receiving generated tool specifications
	 * @param builder builder that creates transport-specific tool specifications
	 */
	StdioGenaiAdapter(List<SyncToolSpecification> toolSpecifications,
			ToolSpecificationBuilder<McpSyncServerExchange> builder) {
		super(toolSpecifications, builder);
	}

	@Override
	protected SyncPromptSpecification buildPromptSpecification(McpSchema.Prompt prompt,
			BiFunction<McpSyncServerExchange, McpSchema.GetPromptRequest, McpSchema.GetPromptResult> promptHandler) {
		return new SyncPromptSpecification(prompt, promptHandler);
	}
}
