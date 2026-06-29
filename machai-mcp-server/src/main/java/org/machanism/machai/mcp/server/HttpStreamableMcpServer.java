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

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.StreamableSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities.Builder;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * HttpStatelessMcpServer sets up and runs a Model Context Protocol (MCP) server
 * that listens for HTTP requests on a specified port.
 * <p>
 * This server loads GenAI tools, configures server capabilities, and exposes
 * the MCP API over HTTP using Jetty.
 * </p>
 * 
 * @since 1.2.0
 * @author Viktor Tovstyi
 */
public class HttpStreamableMcpServer extends AbstractHttpMcpServer {

	private final Logger log = LoggerFactory.getLogger(HttpStreamableMcpServer.class);

	/** The MCP server specification for stateless sync operation. */
	private final SyncSpecification<StreamableSyncSpecification> server;

	/** Loader for registering function-based tools. */
	private FunctionToolsLoader functionToolsLoader = new FunctionToolsLoader();

	/** HTTP transport provider for the MCP server. */
	private HttpServletStreamableServerTransportProvider transportProvider;

	/**
	 * Builds streamable MCP tool specifications from generic tool definitions.
	 */
	public class HttpStreamableToolSpecificationBuilder implements ToolSpecificationBuilder<McpSyncServerExchange> {

		/**
		 * Builds a {@code SyncToolSpecification} for the Remote MCP server.
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
	 * Constructs a new HttpStatelessMcpServer with the given name and version.
	 *
	 * @param name    the server name to report in the MCP API
	 * @param version the server version to report in the MCP API
	 */
	public HttpStreamableMcpServer(String name, String version) {
		super();
		log.info("Initializing HttpStatelessMcpServer: name={}, version={}", name, version);

		transportProvider = HttpServletStreamableServerTransportProvider.builder().build();

		Builder tools = McpSchema.ServerCapabilities.builder()
				.prompts(true)
				.tools(true);

		server = McpServer.sync(transportProvider)
				.serverInfo(Implementation.builder(name, version)
						.websiteUrl(MACHAI_MACHANISM_HOMEPAGE)
						.description("")
						.icons(List.of(io.modelcontextprotocol.spec.McpSchema.Icon
								.builder(MACHAI_MACHANISM_ICON).build()))
						.build())
				.capabilities(tools.build());

		setTransportProvider(transportProvider);
	}

	/**
	 * Loads and registers GenAI tools with the MCP server.
	 * <p>
	 * This method uses a {@link FunctionToolsLoader} to apply tools to the server
	 * using a {@link GenericGenaiAdapter}.
	 * </p>
	 */
	@Override
	public void tools(Configurator config) {
		log.info("Registering GenAI tools with MCP server...");

		List<SyncToolSpecification> toolSpecifications = new ArrayList<>();
		List<SyncPromptSpecification> prompts = new ArrayList<>();

		GenericGenaiAdapter<McpSyncServerExchange, SyncToolSpecification> httpAdapter = new GenericGenaiAdapter<>(
				toolSpecifications, new HttpStreamableToolSpecificationBuilder()) {

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
						exch, req) -> {
					List<PromptMessage> promptMessageList = new ArrayList<>();

					Map<String, Object> args = req.arguments();

					if (exch instanceof McpSyncServerExchange) {
						String sessionId = null;
						sessionId = ((McpSyncServerExchange) exch).sessionId();
						args.put(ToolFunction.SESSION_ID_PARAM_NAME, sessionId);
					}

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

				prompts.add(new SyncPromptSpecification(
						prompt, promptHandler));
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
		httpAdapter.init(null, config);
		httpAdapter.setProjectDir(getProjectDir());

		functionToolsLoader.applyTools(httpAdapter, McpServer.class);
		server.tools(toolSpecifications);

		server.prompts(prompts);
	}

	/**
	 * Starts the HTTP server and listens for incoming MCP requests on the specified
	 * port.
	 *
	 * @throws Exception if the server fails to start
	 */
	@Override
	public void start() throws Exception {
		server.build();
		super.start();
	}

}
