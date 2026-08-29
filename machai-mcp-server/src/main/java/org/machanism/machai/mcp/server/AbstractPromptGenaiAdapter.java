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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Common prompt-registration support for MCP GenAI adapters.
 *
 * @param <E> server exchange or transport context type
 * @param <S> transport-specific tool specification type
 * @param <P> transport-specific prompt specification type
 * @since 1.2.0
 */
abstract class AbstractPromptGenaiAdapter<E, S, P> extends GenericGenaiAdapter<E, S> {

	private final Logger log = LoggerFactory.getLogger(AbstractPromptGenaiAdapter.class);
	private final List<P> prompts = new ArrayList<>();

	AbstractPromptGenaiAdapter(List<S> toolSpecifications, ToolSpecificationBuilder<E> toolSpecificationBuilder) {
		super(toolSpecifications, toolSpecificationBuilder);
	}

	private static final class PromptExecutionException extends Exception {

		private static final long serialVersionUID = 1L;

		private PromptExecutionException(Exception cause) {
			super(cause.getMessage(), cause);
		}
	}

	@Override
	protected void addPrompt(String name, String description, ToolFunction function, Role role,
			ParamDescriptor... paramsDesc) {
		List<PromptArgument> arguments = new ArrayList<>();
		for (ParamDescriptor parameter : paramsDesc) {
			String parameterName = parameter.getName();
			arguments.add(PromptArgument.builder(parameterName)
					.title(toHumanReadable(parameterName))
					.description(parameter.getDescription())
					.required(parameter.isRequired())
					.build());
		}

		McpSchema.Prompt prompt = McpSchema.Prompt.builder(name)
				.description(description)
				.title(toHumanReadable(name))
				.arguments(arguments)
				.build();

		BiFunction<E, McpSchema.GetPromptRequest, McpSchema.GetPromptResult> promptHandler = (exchange, request) -> {
			List<PromptMessage> messages = new ArrayList<>();
			Map<String, Object> argumentsMap = request.arguments();
			addSessionIdentifier(exchange, argumentsMap);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode parameters = mapper.convertValue(argumentsMap, JsonNode.class);
			try {
				Object result = executePrompt(function, parameters);
				addPromptMessages(messages, result, mapper, role);
			} catch (PromptExecutionException | JsonProcessingException exception) {
				log.error("Failed to execute prompt '{}': {}", name, exception.getMessage(),
						ExceptionUtils.getRootCause(exception));
				addPromptMessage(messages, exception.getMessage(), role);
			}
			return McpSchema.GetPromptResult.builder(messages).build();
		};

		prompts.add(buildPromptSpecification(prompt, promptHandler));
	}

	private Object executePrompt(ToolFunction function, JsonNode parameters) throws PromptExecutionException {
		try {
			return function.apply(parameters, getProjectDir(), getConfigurator());
		} catch (Exception exception) {
			// Sonar java:S112: the dependency exposes only Exception; contain it in a domain-specific exception.
			throw new PromptExecutionException(exception);
		}
	}

	/**
	 * Adds transport-specific session data to prompt arguments when available.
	 *
	 * @param exchange server exchange or transport context
	 * @param arguments prompt arguments
	 */
	protected void addSessionIdentifier(E exchange, Map<String, Object> arguments) {
		// Stateless transports do not provide a session identifier.
	}

	private void addPromptMessages(List<PromptMessage> messages, Object result, ObjectMapper mapper, Role role) throws JsonProcessingException {
		if (result instanceof String text) {
			addPromptMessage(messages, text, role);
		} else if (result instanceof List<?> values) {
			for (Object value : values) {
				addPromptMessage(messages, String.valueOf(value), role);
			}
		} else {
			addPromptMessage(messages, mapper.writeValueAsString(result), role);
		}
	}

	private void addPromptMessage(List<PromptMessage> messages, String text, Role role) {
		messages.add(PromptMessage.builder(io.modelcontextprotocol.spec.McpSchema.Role.valueOf(role.name()),
				TextContent.builder(text).build()).build());
	}

	protected abstract P buildPromptSpecification(McpSchema.Prompt prompt,
			BiFunction<E, McpSchema.GetPromptRequest, McpSchema.GetPromptResult> promptHandler);

	/**
	 * Returns the prompts registered with this adapter.
	 *
	 * @return mutable list of transport-specific prompt specifications
	 */
	public List<P> getPrompts() {
		return prompts;
	}
}
