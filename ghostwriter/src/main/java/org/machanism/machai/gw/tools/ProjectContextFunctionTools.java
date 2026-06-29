package org.machanism.machai.gw.tools;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Tool;

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
	 * <p>
	 * This method stores or updates a named variable associated with a particular project directory,
	 * making it available for act execution or prompt templates. It can be used to pass a variable
	 * to the next episode of an act or to share state between different steps in a workflow.
	 * </p>
	 *
	 * @param name        The name of the context variable to set or update.
	 * @param value       The value to assign to the context variable.
	 * @param projectDir  The project directory with which the context variable is associated.
	 * @return A message indicating whether the context variable was successfully set or if an error occurred.
	 */
	@Tool(name = "put_project_context_variable", description = "Sets or updates a variable in the project-specific context. Use this to store or update a named "
			+ "variable associated with a particular project, making it available for act execution or "
			+ "prompt templates. It can be used to pass a variable to the next episode of an act.")
	public static String putProjectContextVariable(
			@Param(name = "name", description = "The name of the context variable.") String name,
			@Param(name = "value", description = "The value to assign to the context variable.") String value,
			@Param(name = "project_dir", description = "The project dir.") File projectDir) {
		try {
			put(projectDir, name, value);
			return "Context variable '" + name + "' set to '" + value + "' for project: " + projectDir;

		} catch (Exception e) {
			return "Failed to set context variable: " + e.getMessage();
		}
	}

	/**
	 * Sets or updates a variable in the context map for the specified project directory.
	 * <p>
	 * If the value is a {@link String}, it is stored as-is. Otherwise, the value is serialized
	 * to a JSON string using Jackson's {@link ObjectMapper} before being stored. The variable
	 * is associated with the given {@code name} and made available in the context for the specified
	 * {@code projectDir}.
	 * </p>
	 *
	 * @param projectDir the project directory with which the context variable is associated
	 * @param name       the name of the context variable to set or update
	 * @param value      the value to assign to the context variable; if not a string, it will be serialized to JSON
	 * @throws com.fasterxml.jackson.core.JsonProcessingException if the value cannot be serialized to JSON
	 */
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
	 * <p>
	 * This method accesses a named variable associated with a particular project directory,
	 * making it available for act execution or prompt templates. If the context or variable
	 * does not exist, an appropriate message is returned.
	 * </p>
	 *
	 * @param name       The name of the context variable to retrieve.
	 * @param projectDir The project directory with which the context variable is associated.
	 * @return The value of the context variable if found, or a message indicating that the variable or context was not found,
	 *         or an error message if retrieval fails.
	 */
	@Tool(name = "get_project_context_variable", description = "Retrieves the value of a variable from the project-specific context. Use this to access a named "
			+ "variable associated with a particular project for act execution or prompt templates.")
	public static String getProjectContextVariable(
			@Param(name = "name", description = "The name of the context variable to retrieve.") String name,
			@Param(name = "project_dir", description = "The project dir.") File projectDir) {

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
	 * Pushes a value to a project context variable.
	 * <p>
	 * If the variable does not exist, a new list is created and the value is added.
	 * If the variable exists and is a string, it is converted to a list containing the original string and the new value.
	 * If the variable exists and is already a list, the new value is appended to the list.
	 * If the variable exists and is of any other type, an error message is returned.
	 * </p>
	 *
	 * @param name       The name of the context variable.
	 * @param value      The value to push to the context variable.
	 * @param projectDir The project directory with which the context variable is associated.
	 * @return A message indicating the result of the operation, or an error message if the operation fails or the variable type is unsupported.
	 */
	@Tool(name = "push_project_context_variable", description = "Pushes a value to a project context variable. If the variable exists and is a string, it is converted to a list. Otherwise, the value is appended.")
	public static Object pushProjectContextVariable(
			@Param(name = "name", description = "The name of the context variable.") String name,
			@Param(name = "value", description = "The value to push to the context variable.") String value,
			@Param(name = "project_dir", description = "The project dir.") File projectDir) {
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
	 * Removes and returns a value from a project context variable.
	 * <p>
	 * If the variable is a string, it is removed from the context and returned.
	 * If the variable is a list, a value is removed and returned according to the specified mode:
	 * <ul>
	 *   <li><b>LIFO</b> (last-in, first-out, default): removes and returns the last element in the list.</li>
	 *   <li><b>FIFO</b> (first-in, first-out): removes and returns the first element in the list.</li>
	 * </ul>
	 * If the list becomes empty after removal, the variable is removed from the context.
	 * If the list is reduced to a single element, it is converted back to a string for simplicity.
	 * If the variable does not exist or is of an unsupported type, an appropriate message is returned.
	 *
	 * @param name       The name of the context variable.
	 * @param mode       Pop mode, either "LIFO" (default) or "FIFO".
	 * @param projectDir The project directory with which the context variable is associated.
	 * @return The removed value, or a message if the variable does not exist, is empty, or is of an unsupported type, or if an error occurs.
	 */
	@Tool(name = "pop_project_context_variable", description = "Removes and returns a value from a project context variable. If the variable is a string, it is removed and returned. If it is a list, "
			+ "the value is removed in LIFO (last-in, first-out) or FIFO (first-in, first-out) mode.")
	public static Object popProjectContextVariable(
			@Param(name = "name", description = "The name of the context variable.") String name,
			@Param(name = "mode", description = "Pop mode, either 'LIFO' (default) or 'FIFO'.", defaultValue = "") String mode,
			@Param(name = "project_dir", description = "The project dir.") File projectDir) {
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
