package org.machanism.machai.mcp.server;

import java.util.function.BiFunction;

import io.modelcontextprotocol.spec.McpSchema;

public interface ToolSpecificationBuilder<TExchange> {
	Object buildTool(String name, Object schema);

	Object buildSpecification(Object tool,
			BiFunction<TExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> callHandler);
}
