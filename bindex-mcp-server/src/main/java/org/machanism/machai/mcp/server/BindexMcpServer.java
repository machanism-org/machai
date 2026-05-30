package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class BindexMcpServer {

	private static final Logger log = LoggerFactory.getLogger(BindexMcpServer.class);

	public static void main(String[] args) {
		var transportProvider = new StdioServerTransportProvider();

		List<SyncPromptSpecification> prompts = new ArrayList<>();
		Prompt prompt = new Prompt("analyze", "Code analysis template", null);
		prompts.add(new McpServerFeatures.SyncPromptSpecification(prompt,
				(exchange, request) -> {
					return new GetPromptResult("These my prompts",
							List.of(new PromptMessage(Role.USER, new TextContent("Hello!"))));
				}));

		McpSyncServer server = McpServer.sync(transportProvider)
				.serverInfo("mcp-bindex-server", "1.1.15")
				.capabilities(McpSchema.ServerCapabilities.builder()
						.tools(true)
						.resources(true, false)
						.prompts(true)
						.logging()
						.build())
				.tools(paidInvoicesCount(), paidInvoicesCount())
				.prompts(prompts)
				.build();

		Thread shutdownHook = new Thread(() -> {
			server.close();
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	public static McpServerFeatures.SyncToolSpecification paidInvoicesCount() {
		return noParamsToolSpec("get-libraries",
				"Retrieves the list of recommended libraries.",
				BindexMcpServer::countPaidInvoices);
	}

	public static String countPaidInvoices() {
		return """
				{
				  "a": "hello"
				}
				""";
	}

	private static McpServerFeatures.SyncToolSpecification noParamsToolSpec(String name,
			String description,
			Supplier<String> implementation) {
		var schema = """
				{
				"type": "object",
				"properties": {},
				"required": []
				}
				""";

		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(name, description, schema),
				(exchange, args) -> {
					String result = implementation.get();

					log.info(">>>>>" + result);

					return McpSchema.CallToolResult.builder()
							.addContent(new TextContent(result))
							.isError(false)
							.build();
				});
	}

}