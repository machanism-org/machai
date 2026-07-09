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

public class StdioGenaiAdapter extends GenericGenaiAdapter<McpSyncServerExchange, SyncToolSpecification> {

	private final Logger log = LoggerFactory.getLogger(StdioGenaiAdapter.class);

	private List<SyncPromptSpecification> prompts = new ArrayList<>();

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

		getPrompts().add(new SyncPromptSpecification(prompt, promptHandler));

	}

	private void addPrompt(List<PromptMessage> promptMessageList, String text, Role role,
			Map<String, Object> args) {
		PromptMessage promptMessage = PromptMessage
				.builder(io.modelcontextprotocol.spec.McpSchema.Role.valueOf(role.name()),
						TextContent.builder(text).build())
				.build();
		promptMessageList.add(promptMessage);
	}

	/**
	 * @return the prompts
	 */
	public List<SyncPromptSpecification> getPrompts() {
		return prompts;
	}

}
