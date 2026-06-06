package org.machanism.machai.gw.tools;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides function tools for managing project-specific context variables.
 * <p>
 * This class registers tools for setting, retrieving, pushing, and popping
 * variables in a project context, enabling stateful data sharing across acts
 * and episodes.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class ProjectContextFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(ProjectContextFunctionTools.class);

	/** Map of project directories to their context variable maps. */
	private static Map<File, Map<String, Object>> contextProjectMap = new HashMap<>();

	/**
	 * Registers project context management tools with the given GenAI provider.
	 * <ul>
	 * <li><b>put_project_context_variable</b>: Sets or updates a variable in the
	 * project-specific context.</li>
	 * <li><b>get_project_context_variable</b>: Retrieves the value of a variable
	 * from the project-specific context.</li>
	 * <li><b>push_project_context_variable</b>: Pushes a value to a project context
	 * variable, converting it to a list if needed.</li>
	 * <li><b>pop_project_context_variable</b>: Removes and returns a value from a
	 * project context variable, supporting LIFO and FIFO modes.</li>
	 * </ul>
	 *
	 * @param provider the GenAI provider to register tools with
	 */
	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"put_project_context_variable",
				"Sets or updates a variable in the project-specific context. Use this to store or update a named "
						+ "variable associated with a particular project, making it available for act execution or "
						+ "prompt templates. It can be used to pass a variable to the next episode of an act.",
				ProjectContextFunctionTools::putProjectContextVariable,
				"name:string:required:The name of the context variable.",
				"value:string:required:The value to assign to the context variable.");

		provider.addTool(
				"get_project_context_variable",
				"Retrieves the value of a variable from the project-specific context. Use this to access a named "
						+ "variable associated with a particular project for act execution or prompt templates.",
				ProjectContextFunctionTools::getProjectContextVariable,
				"name:string:required:The name of the context variable to retrieve.");

		provider.addTool(
				"push_project_context_variable",
				"Pushes a value to a project context variable. If the variable exists and is a string, it is converted to a list. Otherwise, the value is appended.",
				ProjectContextFunctionTools::pushProjectContextVariable,
				"name:string:required:The name of the context variable.",
				"value:string:required:The value to push to the context variable.");

		provider.addTool(
				"pop_project_context_variable",
				"Removes and returns a value from a project context variable. If the variable is a string, it is removed and returned. If it is a list, "
						+ "the value is removed in LIFO (last-in, first-out) or FIFO (first-in, first-out) mode.",
				ProjectContextFunctionTools::popProjectContextVariable,
				"name:string:required:The name of the context variable.",
				"mode:string:optional:Pop mode, either 'LIFO' (default) or 'FIFO'.");
	}

	/**
	 * Sets or updates a variable in the project-specific context.
	 *
	 * @param props      JSON node containing 'name' and 'value' properties
	 * @param projectDir the project directory
	 * @return a confirmation message or error
	 */
	public static String putProjectContextVariable(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Put project context variable: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), projectDir);
		}

		try {
			String name = props.get("name").asText();
			String value = props.get("value").asText();

			put(projectDir, name, value);

			return "Context variable '" + name + "' set to '" + value + "' for project: " + projectDir;

		} catch (Exception e) {
			return "Failed to set context variable: " + e.getMessage();
		}
	}

	public static void put(File projectDir, String name, Object value) throws JsonProcessingException {
		Map<String, Object> context = contextProjectMap.computeIfAbsent(projectDir, key -> new HashMap<>());
		String result;
		if (value instanceof String) {
			result = (String) value;
		} else {
			result = new ObjectMapper().writeValueAsString(value);
		}

		context.put(name, result);
	}

	/**
	 * Retrieves the value of a variable from the project-specific context.
	 *
	 * @param props      JSON node containing 'name' property
	 * @param projectDir the project directory
	 * @return the value of the context variable, or a message if not found
	 */
	public static Object getProjectContextVariable(JsonNode props, File projectDir) {

		Object result;
		try {
			String name = props.get("name").asText();

			Map<String, Object> context = contextProjectMap.get(projectDir);
			if (context == null) {
				result = "No context found for project: " + projectDir;
			} else {
				String value = String.valueOf(context.get(name));
				if (value == null) {
					result = "Context variable '" + name + "' not found for project: " + projectDir;
				} else {
					result = value;
				}
			}
		} catch (Exception e) {
			result = "Failed to get context variable: " + e.getMessage();
		}

		if (logger.isInfoEnabled()) {
			logger.info("Get project context variable: {}, {}, Value: {}", props,
					projectDir, StringUtils.abbreviate(String.valueOf(result), 80));
		}

		return result;
	}

	/**
	 * Pushes a value to a project context variable. If the variable exists and is a
	 * string, it is converted to a list. Otherwise, the value is appended.
	 *
	 * @param props      JSON node containing 'name' and 'value' properties
	 * @param projectDir the project directory
	 * @return a confirmation message or error
	 */
	public static Object pushProjectContextVariable(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Push project context variable: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), projectDir);
		}
		try {
			String name = props.get("name").asText();
			String value = props.get("value").asText();

			Map<String, Object> context = contextProjectMap.computeIfAbsent(projectDir, key -> new HashMap<>());
			Object existing = context.get(name);

			if (existing == null) {
				// No variable yet, create a new list
				List<Object> list = new java.util.ArrayList<>();
				list.add(value);
				context.put(name, list);
			} else if (existing instanceof String) {
				// Convert string to list
				List<Object> list = new java.util.ArrayList<>();
				list.add(existing);
				list.add(value);
				context.put(name, list);
			} else if (existing instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) existing;
				list.add(value);
			} else {
				return "Unsupported variable type for '" + name + "': " + existing.getClass();
			}

			return "Pushed value '" + value + "' to context variable '" + name + "' for project: " + projectDir;
		} catch (Exception e) {
			return "Failed to push context variable: " + e.getMessage();
		}
	}

	/**
	 * Removes and returns a value from a project context variable. If the variable
	 * is a string, it is removed and returned. If it is a list, the value is
	 * removed in LIFO (last-in, first-out) or FIFO (first-in, first-out) mode.
	 *
	 * @param props      JSON node containing 'name' property and optional 'mode'
	 *                   property
	 * @param projectDir the project directory
	 * @return the removed value, or a message if not found or unsupported
	 */
	public static Object popProjectContextVariable(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Pop project context variable: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), projectDir);
		}
		try {
			String name = props.get("name").asText();
			String mode = props.has("mode") ? props.get("mode").asText() : "LIFO"; // Default to LIFO

			Map<String, Object> context = contextProjectMap.get(projectDir);
			if (context == null) {
				return "No context found for project: " + projectDir;
			}
			Object existing = context.get(name);
			if (existing == null) {
				return "Context variable '" + name + "' not found for project: " + projectDir;
			}

			if (existing instanceof List) {
				List<?> list = (List<?>) existing;
				if (list.isEmpty()) {
					return "Context variable '" + name + "' is an empty list for project: " + projectDir;
				}
				Object value;
				if ("FIFO".equalsIgnoreCase(mode)) {
					value = list.remove(0); // Remove first element
				} else {
					value = list.remove(list.size() - 1); // Remove last element (LIFO)
				}
				// If list becomes size 1, convert back to string for simplicity
				if (list.size() == 1) {
					context.put(name, list.get(0));
				} else if (list.isEmpty()) {
					context.remove(name);
				}
				return value;
			} else if (existing instanceof String) {
				context.remove(name);
				return existing;
			} else {
				return "Unsupported variable type for '" + name + "': " + existing.getClass();
			}
		} catch (Exception e) {
			return "Failed to pop context variable: " + e.getMessage();
		}
	}
}