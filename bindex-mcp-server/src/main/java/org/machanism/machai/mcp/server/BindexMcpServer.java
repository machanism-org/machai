package org.machanism.machai.mcp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.GenaiAdapter;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.ToolFunction;
import org.machanism.machai.bindex.ai.tools.BindexFunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.core.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
				.tools(getTools())
				.prompts(prompts)
				.build();

		Thread shutdownHook = new Thread(() -> {
			server.close();
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	private static List<McpServerFeatures.SyncToolSpecification> getTools() {

		List<McpServerFeatures.SyncToolSpecification> toolSpecifications = new ArrayList<>();

		FunctionTools functionTools = new BindexFunctionTools();
		functionTools.setConfigurator(new PropertiesConfigurator());

		functionTools.applyTools(new GenaiAdapter() {
			@Override
			public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
				log.info("addTool: " + name);

				Map<String, JsonValue> properties = new HashMap<>();
				List<String> required = new ArrayList<>();

				if (paramsDesc != null) {
					for (String pDesc : paramsDesc) {
						String[] desc = StringUtils.splitPreserveAllTokens(pDesc, ":");
						if (desc.length >= 3
								&& StringUtils.defaultString(desc[2]).toLowerCase(Locale.ROOT).equals("required")) {
							required.add(desc[0]);
						}
						Map<String, String> value = new HashMap<>();
						value.put("type", desc[1]);
						value.put("description", desc.length > 3 ? desc[3] : StringUtils.EMPTY);

						JsonValue requiredVal = JsonValue.from(value);
						properties.put(desc[0], requiredVal);
					}
				}

				try {
					ObjectMapper mapper = new ObjectMapper();
					HashMap<String, Object> value = new HashMap<>();
					value.put("type", "object");
					value.put("properties", properties);
					value.put("required", required);

					String schema = mapper.writeValueAsString(value);

					log.info("schema: {}", schema);

					toolSpecifications.add(new McpServerFeatures.SyncToolSpecification(
							new McpSchema.Tool(name, description, schema),
							(exchange, args) -> {
								Object result;
								try {
									JsonNode params = mapper.convertValue(args, JsonNode.class);
									result = function.apply(params, null);
									log.info(">>>>> {}", result);
								} catch (Exception e) {
									log.error("Error", e);
									result = e.getMessage();
								}

								return McpSchema.CallToolResult.builder()
										.addContent(new TextContent(Objects.toString(result)))
										.isError(false)
										.build();
							}));
				} catch (JsonProcessingException e) {
					log.error("Error", e);
					throw new IllegalArgumentException(e);
				}
			}
		});

		return toolSpecifications;
	}

}