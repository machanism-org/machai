package org.machanism.machai.mcp.server;

import java.util.Map;
import java.util.function.BiFunction;

import io.modelcontextprotocol.spec.McpSchema;

public class StdioToolSpecificationBuilder
		implements ToolSpecificationBuilder<io.modelcontextprotocol.server.McpSyncServerExchange> {

	@Override
	public Object buildTool(String name, Object schema) {
		return io.modelcontextprotocol.spec.McpSchema.Tool.builder(name, (Map<String,Object>)schema).build();
	}

	@Override
	public Object buildSpecification(Object tool,
			BiFunction<io.modelcontextprotocol.server.McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> callHandler) {
		return io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification.builder()
				.tool((McpSchema.Tool) tool)
				.callHandler(callHandler)
				.build();
	}

}
