package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.StreamableSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
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
				.resources(true, false)
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
	public void tools() {
		log.info("Registering GenAI tools with MCP server...");

		List<SyncToolSpecification> toolSpecifications = new ArrayList<>();
		GenericGenaiAdapter<McpSyncServerExchange, SyncToolSpecification> httpAdapter = new GenericGenaiAdapter<>(
				toolSpecifications, new HttpStreamableToolSpecificationBuilder());
		httpAdapter.init(null, new PropertiesConfigurator());
		httpAdapter.setProjectDir(getProjectDir());

		functionToolsLoader.applyTools(httpAdapter, McpServer.class);
		server.tools(toolSpecifications);
	}

	public void prompts(Map<String, String> promptBundle) {
		if (promptBundle != null) {
			List<McpServerFeatures.SyncPromptSpecification> prompts = new ArrayList<>();

			Set<Entry<String, String>> entries = promptBundle.entrySet();
			for (Entry<String, String> entry : entries) {
				io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification spec = getPrompt(
						entry.getKey(), entry.getValue());
				prompts.add(spec);
			}

			server.prompts(prompts);
		}
	}

	private McpServerFeatures.SyncPromptSpecification getPrompt(String key, String proptText) {
		BiFunction<McpSyncServerExchange, McpSchema.GetPromptRequest, McpSchema.GetPromptResult> promptHandler = (e,
				r) -> {
			System.out.println(e);

			return McpSchema.GetPromptResult
					.builder(
							List.of(PromptMessage
									.builder(Role.ASSISTANT,
											TextContent.builder(proptText).build())
									.build()))
					.build();
		};

		McpSchema.Prompt prompt = McpSchema.Prompt.builder(key)
				.build();
		McpServerFeatures.SyncPromptSpecification spec = new McpServerFeatures.SyncPromptSpecification(
				prompt, promptHandler);
		return spec;
	}

	/**
	 * Starts the HTTP server and listens for incoming MCP requests on the specified
	 * port.
	 *
	 * @throws Exception
	 */
	@Override
	public void start() throws Exception {
		server.build();
		super.start();
	}

}