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

import com.fasterxml.jackson.databind.JsonNode;

public class ProjectContextFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(ProjectContextFunctionTools.class);

	private static Map<File, Map<String, Object>> contextProjectMap = new HashMap<>();

	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"put_project_context_variable",
				"Sets or updates a variable in the project-specific context. Use this to store or update a named "
						+ "variable associated with a particular project, making it available for act execution or "
						+ "prompt templates. It can be used to pass a variable to the next episode of an act.",
				this::putProjectContextVariable,
				"name:string:required:The name of the context variable.",
				"value:string:required:The value to assign to the context variable.");

		provider.addTool(
				"get_project_context_variable",
				"Retrieves the value of a variable from the project-specific context. Use this to access a named "
						+ "variable associated with a particular project for act execution or prompt templates.",
				this::getProjectContextVariable,
				"name:string:required:The name of the context variable to retrieve.");

		provider.addTool(
				"push_project_context_variable",
				"Pushes a value to a project context variable. If the variable exists and is a string, it is converted to a list. Otherwise, the value is appended.",
				this::pushProjectContextVariable,
				"name:string:required:The name of the context variable.",
				"value:string:required:The value to push to the context variable.");

		provider.addTool(
			    "pop_project_context_variable",
			    "Removes and returns a value from a project context variable. If the variable is a string, it is removed and returned. If it is a list, "
			    + "the value is removed in LIFO (last-in, first-out) or FIFO (first-in, first-out) mode.",
			    this::popProjectContextVariable,
			    "name:string:required:The name of the context variable.",
			    "mode:string:optional:Pop mode, either 'LIFO' (default) or 'FIFO'."
			);	}

	/**
	 * Sets a variable in the act context.
	 * 
	 * @param params The first argument is expected to be a JsonNode containing
	 *               'name' and 'value' properties.
	 * @return A confirmation message or error.
	 */
	public Object putProjectContextVariable(JsonNode props, File workingDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Put project context variable: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}

		try {
			String name = props.get("name").asText();
			String value = props.get("value").asText();

			Map<String, Object> context = contextProjectMap.computeIfAbsent(workingDir, key -> new HashMap<>());
			context.put(name, value);

			return "Context variable '" + name + "' set to '" + value + "' for project: " + workingDir;

		} catch (Exception e) {
			return "Failed to set context variable: " + e.getMessage();
		}
	}

	/**
	 * Retrieves the value of a variable from the project-specific context.
	 * 
	 * @param params The first argument is expected to be a JsonNode containing
	 *               'name' property. The second argument is a File representing the
	 *               project directory.
	 * @return The value of the context variable, or a message if not found.
	 */
	public Object getProjectContextVariable(JsonNode props, File workingDir) {

		Object result;
		try {
			String name = props.get("name").asText();

			Map<String, Object> context = contextProjectMap.get(workingDir);
			if (context == null) {
				result = "No context found for project: " + workingDir;
			} else {
				String value = String.valueOf(context.get(name));
				if (value == null) {
					result = "Context variable '" + name + "' not found for project: " + workingDir;
				} else {
					result = value;
				}
			}
		} catch (Exception e) {
			result = "Failed to get context variable: " + e.getMessage();
		}

		if (logger.isInfoEnabled()) {
			logger.info("Get project context variable: {}, {}, Value: {}", props,
					workingDir, StringUtils.abbreviate(String.valueOf(result), 80));
		}

		return result;
	}

	public Object pushProjectContextVariable(JsonNode props, File workingDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Push project context variable: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}
		try {
			String name = props.get("name").asText();
			String value = props.get("value").asText();

			Map<String, Object> context = contextProjectMap.computeIfAbsent(workingDir, key -> new HashMap<>());
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
				((List<Object>) existing).add(value);
			} else {
				return "Unsupported variable type for '" + name + "': " + existing.getClass();
			}

			return "Pushed value '" + value + "' to context variable '" + name + "' for project: " + workingDir;
		} catch (Exception e) {
			return "Failed to push context variable: " + e.getMessage();
		}
	}

	public Object popProjectContextVariable(JsonNode props, File workingDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Pop project context variable: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}
		try {
			String name = props.get("name").asText();
			String mode = props.has("mode") ? props.get("mode").asText() : "LIFO"; // Default to LIFO

			Map<String, Object> context = contextProjectMap.get(workingDir);
			if (context == null) {
				return "No context found for project: " + workingDir;
			}
			Object existing = context.get(name);
			if (existing == null) {
				return "Context variable '" + name + "' not found for project: " + workingDir;
			}

			if (existing instanceof List) {
				List<?> list = (List<?>) existing;
				if (list.isEmpty()) {
					return "Context variable '" + name + "' is an empty list for project: " + workingDir;
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
