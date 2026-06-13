package org.machanism.machai.gw.tools;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.ToolParam;

import com.fasterxml.jackson.core.JsonProcessingException;
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

	/** Map of project directories to their context variable maps. */
	private static Map<File, Map<String, Object>> contextProjectMap = new HashMap<>();

	/**
	 * Sets or updates a variable in the project-specific context.
	 *
	 * @param props      JSON node containing 'name' and 'value' properties
	 * @param projectDir the project directory
	 * @return a confirmation message or error
	 */
	@Tool(name = "put_project_context_variable", description = "Sets or updates a variable in the project-specific context. Use this to store or update a named "
			+ "variable associated with a particular project, making it available for act execution or "
			+ "prompt templates. It can be used to pass a variable to the next episode of an act.")
	public static String putProjectContextVariable(
			@ToolParam(name = "name", description = "The name of the context variable.") String name,
			@ToolParam(name = "value", description = "The value to assign to the context variable.") String value,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		try {
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
	@Tool(name = "get_project_context_variable", description = "Retrieves the value of a variable from the project-specific context. Use this to access a named "
			+ "variable associated with a particular project for act execution or prompt templates.")
	public static String getProjectContextVariable(
			@ToolParam(name = "name", description = "The name of the context variable to retrieve.") String name,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {

		String result;
		try {
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
	@Tool(name = "push_project_context_variable", description = "Pushes a value to a project context variable. If the variable exists and is a string, it is converted to a list. Otherwise, the value is appended.")
	public static Object pushProjectContextVariable(
			@ToolParam(name = "name", description = "The name of the context variable.") String name,
			@ToolParam(name = "value", description = "The value to push to the context variable.") String value,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		try {
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
	@Tool(name = "pop_project_context_variable", description = "Removes and returns a value from a project context variable. If the variable is a string, it is removed and returned. If it is a list, "
			+ "the value is removed in LIFO (last-in, first-out) or FIFO (first-in, first-out) mode.")
	public static Object popProjectContextVariable(
			@ToolParam(name = "name", description = "The name of the context variable.") String name,
			@ToolParam(name = "mode", description = "Pop mode, either 'LIFO' (default) or 'FIFO'.", defaultValue = "") String mode,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		try {
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