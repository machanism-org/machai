package org.machanism.machai.mcp.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;
import org.machanism.machai.mcp.server.AbstractMcpServer.ToolSpecificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.core.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * A generic adapter for integrating GenAI tools with different server
 * implementations.
 * <p>
 * This class abstracts the process of registering tools and their schemas,
 * allowing for flexible integration with various server types by parameterizing
 * the exchange and specification types.
 *
 * @param <TExchange>      the type representing the server exchange/context
 * @param <TSpecification> the type representing the tool specification
 * @since 1.2.0
 * @author Viktor Tovstyi
 */
class GenericGenaiAdapter<TExchange, TSpecification> extends AbstractAIProvider {

	private final Logger log = LoggerFactory.getLogger(GenericGenaiAdapter.class);

	private final List<TSpecification> toolSpecifications;
	private final ToolSpecificationBuilder<TExchange> builder;

	/**
	 * Constructs a new GenericGenaiAdapter.
	 *
	 * @param toolSpecifications the list to which tool specifications will be added
	 * @param builder            the builder responsible for creating tool and
	 *                           specification objects
	 */
	GenericGenaiAdapter(List<TSpecification> toolSpecifications, ToolSpecificationBuilder<TExchange> builder) {
		this.toolSpecifications = toolSpecifications;
		this.builder = builder;
	}

	/**
	 * Register tool implementation for the adapter.
	 * 
	 * If you need to implement a custom tool use
	 * {@link org.machanism.machai.ai.tools.FunctionTools}.
	 *
	 * @param name        the name of the tool
	 * @param description the description of the tool
	 * @param function    the function to execute when the tool is called
	 * @param paramsDesc  the parameter descriptions for the tool, each in the
	 *                    format "name:type:required:description"
	 */
	@Override
	protected void addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc) {
		Map<String, JsonValue> properties = new LinkedHashMap<>();
		List<String> required = new ArrayList<>();

		if (paramsDesc != null) {
			for (ParamDescriptor pDesc : paramsDesc) {
				addPropDescription(properties, required, pDesc);
			}
		}

		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, Object> schema = new HashMap<>();
		schema.put("type", "object");
		schema.put("properties", properties);
		schema.put("required", required);

		BiFunction<TExchange, CallToolRequest, McpSchema.CallToolResult> callHandler = (exchange, args) -> {
			String result;
			boolean isError = false;
			try {
				Map<String, Object> arguments = args.arguments();

				if (exchange instanceof McpSyncServerExchange exch) {
					String sessionId = null;
					sessionId = exch.sessionId();
					arguments.put(ToolFunction.SESSION_ID_PARAM_NAME, sessionId);
				}

				JsonNode params = mapper.convertValue(arguments, JsonNode.class);
				Object apply = function.apply(params, getProjectDir(), getConfigurator());
				if (apply instanceof String) {
					result = (String) apply;
				} else {
					result = mapper.writeValueAsString(apply);
				}

			} catch (Exception e) {
				log.error("Failed to execute tool '{}': {}", name, e.getMessage(), ExceptionUtils.getRootCause(e));
				result = e.getMessage();
				isError = true;
			}

			return McpSchema.CallToolResult.builder()
					.addTextContent(result)
					.isError(isError)
					.build();
		};

		String title = toHumanReadable(name);
		Tool tool = io.modelcontextprotocol.spec.McpSchema.Tool.builder(name, schema).title(title)
				.description(description).build();

		@SuppressWarnings("unchecked")
		TSpecification spec = (TSpecification) builder.buildSpecification(tool, callHandler);

		log.info("Registered tool '{}': {}", name,
				StringUtils.abbreviate(spec.toString(), AbstractAIProvider.LOG_LINE_LENG)
						.replace(AbstractAIProvider.LINE_SEPARATOR, " ")
						.replace("\r", ""));

		toolSpecifications.add(spec);
	}

	/**
	 * Adds a property description to the tool schema.
	 *
	 * @param properties the map of property names to their JSON schema values
	 * @param required   the list of required property names
	 * @param pDesc      the parameter description in the format
	 *                   "name:type:required:description"
	 */
	private void addPropDescription(Map<String, JsonValue> properties, List<String> required, ParamDescriptor pDesc) {
		if (pDesc.isRequired()) {
			required.add(pDesc.getName());
		}

		Map<String, Object> value = new HashMap<>();
		value.put("type", pDesc.getType());
		value.put("description", pDesc.getDescription());

		if ("array".equals(pDesc.getType())) {
			value.put("items", Map.of("type", "string"));
		}

		JsonValue requiredVal = JsonValue.from(value);
		properties.put(pDesc.getName(), requiredVal);
	}

	/**
	 * Converts a function tool name in Python style (snake_case or camelCase) to a
	 * human-readable format. Examples: "my_function_tool" -> "My Tool Tool"
	 * "doSomething_cool" -> "Do Something Cool" "anotherToolName" -> "Another Tool
	 * Name"
	 *
	 * @param toolName the function tool name to convert
	 * @return the human-readable format
	 */
	public static String toHumanReadable(String toolName) {
		if (toolName == null || toolName.isEmpty()) {
			return "";
		}

		String spaced = toolName.replace('_', ' ');

		// Insert spaces before uppercase letters (for camelCase)
		spaced = spaced.replaceAll("([a-z])([A-Z])", "$1 $2");

		// Capitalize each word
		String[] words = spaced.split("\\s+");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (!word.isEmpty()) {
				result.append(Character.toUpperCase(word.charAt(0)));
				if (word.length() > 1) {
					result.append(word.substring(1).toLowerCase());
				}
				result.append(' ');
			}
		}
		return result.toString().trim();
	}

	@Override
	public String perform() {
		return null;
	}

}