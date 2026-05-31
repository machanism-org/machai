package org.machanism.machai.mcp.server;

import java.util.Map;
import java.util.function.BiFunction;

import io.modelcontextprotocol.spec.McpSchema;

public class RemoteToolSpecificationBuilder
		implements ToolSpecificationBuilder<io.modelcontextprotocol.common.McpTransportContext> {
	@Override
	public Object buildTool(String name, Object schema) {
		return io.modelcontextprotocol.spec.McpSchema.Tool.builder(name, (Map<String, Object>) schema).build();
	}

	@Override
	public Object buildSpecification(Object tool,
			BiFunction<io.modelcontextprotocol.common.McpTransportContext, McpSchema.CallToolRequest, McpSchema.CallToolResult> callHandler) {
		return io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification.builder()
				.tool((McpSchema.Tool) tool)
				.callHandler(callHandler)
				.build();
	}
}