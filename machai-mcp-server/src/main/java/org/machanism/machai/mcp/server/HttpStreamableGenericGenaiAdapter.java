package org.machanism.machai.mcp.server;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.machanism.machai.ai.tools.ToolFunction;
import org.machanism.machai.mcp.server.AbstractMcpServer.ToolSpecificationBuilder;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Adapts Machai tools and prompts to the streamable HTTP MCP transport.
 *
 * @since 1.2.0
 */
public class HttpStreamableGenericGenaiAdapter
		extends AbstractPromptGenaiAdapter<McpSyncServerExchange, SyncToolSpecification, SyncPromptSpecification> {

	/**
	 * Creates an adapter backed by the supplied tool specification collection.
	 *
	 * @param toolSpecifications collection receiving generated tool specifications
	 * @param builder builder that creates transport-specific tool specifications
	 */
	HttpStreamableGenericGenaiAdapter(List<SyncToolSpecification> toolSpecifications,
			ToolSpecificationBuilder<McpSyncServerExchange> builder) {
		super(toolSpecifications, builder);
	}

	@Override
	protected void addSessionIdentifier(McpSyncServerExchange exchange, Map<String, Object> arguments) {
		if (exchange != null) {
			arguments.put(ToolFunction.SESSION_ID_PARAM_NAME, exchange.sessionId());
		}
	}

	@Override
	protected SyncPromptSpecification buildPromptSpecification(McpSchema.Prompt prompt,
			BiFunction<McpSyncServerExchange, McpSchema.GetPromptRequest, McpSchema.GetPromptResult> promptHandler) {
		return new SyncPromptSpecification(prompt, promptHandler);
	}
}
