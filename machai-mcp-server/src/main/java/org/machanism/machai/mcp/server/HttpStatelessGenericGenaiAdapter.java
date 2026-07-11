package org.machanism.machai.mcp.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.Role;
import org.machanism.machai.ai.tools.ToolFunction;
import org.machanism.machai.mcp.server.AbstractMcpServer.ToolSpecificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

public class HttpStatelessGenericGenaiAdapter extends GenericGenaiAdapter<McpTransportContext, SyncToolSpecification> {

	private final Logger log = LoggerFactory.getLogger(HttpStatelessGenericGenaiAdapter.class);
	private List<McpStatelessServerFeatures.SyncPromptSpecification> promptSpecifications = new ArrayList<>();
	private List<McpStatelessServerFeatures.SyncResourceSpecification> resourceSpecifications = new ArrayList<>();

	HttpStatelessGenericGenaiAdapter(List<SyncToolSpecification> toolSpecifications,
			ToolSpecificationBuilder<McpTransportContext> builder) {
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

		BiFunction<McpTransportContext, McpSchema.GetPromptRequest, McpSchema.GetPromptResult> promptHandler = (
				e,
				r) -> {
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

		getPrompts().add(new McpStatelessServerFeatures.SyncPromptSpecification(
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

	@Override
	protected void addResource(URI uri, String description, String mimeType, ToolFunction function,
			ParamDescriptor... paramsDesc) {
		String name;
		name = StringUtils.substringAfterLast(uri.getPath(), "/");
		McpSchema.Resource resource = McpSchema.Resource.builder(uri.toString(), name).build();
		BiFunction<McpTransportContext, McpSchema.ReadResourceRequest, McpSchema.ReadResourceResult> readHandler = (cnx,
				req) -> {
			List<ResourceContents> contents = new ArrayList<>();

			Object result = function.apply(null, projectDir, getConfigurator(), uri);

			String content = String.valueOf(result);
			contents.add(TextResourceContents.builder(uri.toString(), content).mimeType(mimeType).build());

			return McpSchema.ReadResourceResult.builder(contents).build();
		};

		resourceSpecifications.add(new McpStatelessServerFeatures.SyncResourceSpecification(resource, readHandler));
	}

	/**
	 * @return the promptSpecifications
	 */
	public List<McpStatelessServerFeatures.SyncPromptSpecification> getPrompts() {
		return promptSpecifications;
	}

	public List<McpStatelessServerFeatures.SyncResourceSpecification> getResources() {
		return resourceSpecifications;
	}

}
