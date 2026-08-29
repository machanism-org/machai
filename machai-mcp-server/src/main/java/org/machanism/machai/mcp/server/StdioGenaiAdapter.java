package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.Role;
import org.machanism.machai.ai.tools.ToolFunction;
import org.machanism.machai.mcp.server.AbstractMcpServer.ToolSpecificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Adapts Machai tools and prompts to the synchronous STDIO MCP transport.
 *
 * @since 1.2.0
 */
public class StdioGenaiAdapter extends GenericGenaiAdapter<McpSyncServerExchange, SyncToolSpecification> {

	/** Logger used to report prompt execution failures. */
	private final Logger log = LoggerFactory.getLogger(StdioGenaiAdapter.class);

	/** Prompt specifications registered by this adapter. */
	private List<SyncPromptSpecification> prompts = new ArrayList<>();

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
				Object apply = function.apply(params, getProjectDir(), getConfigurator());
				if (apply instanceof String stringResult) {
					addPrompt(promptMessageList, stringResult, role);
				} else if (apply instanceof List) {
					@SuppressWarnings("unchecked")
					List<Object> list = (List<Object>) apply;
					for (Object promptValue : list) {
						addPrompt(promptMessageList, String.valueOf(promptValue), role);
					}

				} else {
					addPrompt(promptMessageList, mapper.writeValueAsString(apply), role);
				}

			} catch (Exception e1) {
				log.error("Failed to execute tool '{}': {}", name, e1.getMessage(),
						ExceptionUtils.getRootCause(e1));
				addPrompt(promptMessageList, e1.getMessage(), role);
			}

			return McpSchema.GetPromptResult
					.builder(promptMessageList)
					.build();
		};

		getPrompts().add(new SyncPromptSpecification(prompt, promptHandler));

	}

	/**
	 * Adds a generated text value to the prompt result.
	 *
	 * @param promptMessageList list receiving the new prompt message
	 * @param text message text
	 * @param role MCP role for the message
	 */
	private void addPrompt(List<PromptMessage> promptMessageList, String text, Role role) {
		PromptMessage promptMessage = PromptMessage
				.builder(io.modelcontextprotocol.spec.McpSchema.Role.valueOf(role.name()),
						TextContent.builder(text).build())
				.build();
		promptMessageList.add(promptMessage);
	}

	/**
	 * Returns the prompts registered with this adapter.
	 *
	 * @return mutable list of synchronous prompt specifications
	 */
	public List<SyncPromptSpecification> getPrompts() {
		return prompts;
	}

}
