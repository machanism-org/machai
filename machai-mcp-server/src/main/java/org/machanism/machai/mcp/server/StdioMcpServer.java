package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.Role;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import tools.jackson.databind.json.JsonMapper;

/**
 * StdioMcpServer sets up and runs a Model Context Protocol (MCP) server that
 * communicates via standard input and output (STDIO).
 * <p>
 * This server loads GenAI tools, configures server capabilities, and exposes
 * the MCP API over STDIO for integration with other processes.
 * </p>
 * 
 * @since 1.2.0
 * @author Viktor Tovstyi
 */
class StdioMcpServer extends AbstractMcpServer {

	private final Logger log = LoggerFactory.getLogger(StdioMcpServer.class);

	/** The MCP server specification for single-session sync operation. */
	private final SyncSpecification<SingleSessionSyncSpecification> server;

	/** Loader for registering function-based tools. */
	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	class StdioToolSpecificationBuilder implements ToolSpecificationBuilder<McpSyncServerExchange> {

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

	/**
	 * Constructs a new StdioMcpServer with the given name and version.
	 *
	 * @param name    the server name to report in the MCP API
	 * @param version the server version to report in the MCP API
	 */
	StdioMcpServer(String name, String version) {
		McpServerTransportProvider transportProvider = new StdioServerTransportProvider(
				new JacksonMcpJsonMapper(new JsonMapper()));

		server = McpServer.sync(transportProvider)
				.serverInfo(name, version)
				.capabilities(McpSchema.ServerCapabilities.builder()
						.tools(true)
						.prompts(true)
						.logging()
						.build());
	}

	/**
	 * Loads and registers GenAI tools with the MCP server.
	 * <p>
	 * This method uses a {@link FunctionToolsLoader} to apply tools to the server
	 * using a {@link GenericGenaiAdapter}.
	 * </p>
	 */
	void tools(Configurator config) {
		List<SyncPromptSpecification> prompts = new ArrayList<>();
		List<SyncToolSpecification> toolSpecifications = new ArrayList<>();
		GenericGenaiAdapter<io.modelcontextprotocol.server.McpSyncServerExchange, SyncToolSpecification> stdioAdapter = new GenericGenaiAdapter<>(
				toolSpecifications, new StdioToolSpecificationBuilder()) {

			@Override
			protected void addPrompt(String name, String description, ToolFunction function, Role role,
					ParamDescriptor... paramsDesc) {

				List<PromptArgument> arguments = new ArrayList<>();
				for (ParamDescriptor param : paramsDesc) {
					String paramName = param.getName();
					String title = toHumanReadable(paramName);
					arguments.add(PromptArgument
							.builder(paramName)
							.title(title)
							.description(param.getDescription())
							.required(param.isRequired())
							.build());
				}

				String promptTitle = toHumanReadable(name);
				McpSchema.Prompt prompt = McpSchema.Prompt.builder(name)
						.description(description)
						.title(promptTitle)
						.arguments(arguments)
						.build();

				BiFunction<McpSyncServerExchange, McpSchema.GetPromptRequest, McpSchema.GetPromptResult> promptHandler = (
						e, r) -> {
					List<PromptMessage> promptMessageList = new ArrayList<>();

					Map<String, Object> args = r.arguments();

					ObjectMapper mapper = new ObjectMapper();
					JsonNode params = mapper.convertValue(args, JsonNode.class);
					try {
						Object apply = function.apply(params, getProjectDir(), config);
						if (apply instanceof String) {
							addPrompt(promptMessageList, (String) apply, role, args);
						} else if (apply instanceof List) {
							@SuppressWarnings("unchecked")
							List<Object> list = (List<Object>) apply;
							for (Object p : list) {
								addPrompt(promptMessageList, (String) p, role, args);
							}

						} else {
							addPrompt(promptMessageList, mapper.writeValueAsString(apply), role, args);
						}

					} catch (Exception e1) {
						log.error("Failed to execute tool '{}': {}", name, e1.getMessage(),
								ExceptionUtils.getRootCause(e1));
						addPrompt(promptMessageList, e1.getMessage(), role, args);
					}

					return McpSchema.GetPromptResult
							.builder(promptMessageList)
							.build();
				};

				prompts.add(new SyncPromptSpecification(prompt, promptHandler));

			}

			private void addPrompt(List<PromptMessage> promptMessageList, String text, Role role,
					Map<String, Object> args) {
				PromptMessage promptMessage = PromptMessage
						.builder(io.modelcontextprotocol.spec.McpSchema.Role.valueOf(role.name()),
								TextContent.builder(text).build())
						.build();
				promptMessageList.add(promptMessage);
			}

		};
		stdioAdapter.init(null, config);
		stdioAdapter.setProjectDir(getProjectDir());

		functionToolsLoader.applyTools(stdioAdapter, McpServer.class);
		server.tools(toolSpecifications);
		server.prompts(prompts);
	}

	/**
	 * Builds and returns the configured {@link McpSyncServer} instance.
	 * <p>
	 * Also registers a shutdown hook to ensure the server is closed gracefully on
	 * JVM exit.
	 * </p>
	 *
	 * @return the built MCP sync server
	 */
	@Override
	void start() {
		McpSyncServer mcpSyncServer = server.build();
		Thread shutdownHook = new Thread(() -> {
			mcpSyncServer.close();
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

}