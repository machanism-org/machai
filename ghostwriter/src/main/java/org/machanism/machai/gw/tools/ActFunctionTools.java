package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ActFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(CommandFunctionTools.class);

	private static final String TOML_EXTENSION = ".toml";

	private Configurator configurator;

	private Map<File, Map<String, String>> contextProjectMap = new HashMap<>();

	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"build_in_list_acts",
				"Retrieves a list of all available Act templates that can be used with Ghostwriter. Acts are reusable prompt templates stored as TOML files, "
						+ "which define instructions and input templates for common workflows.",
				this::getActList);

		provider.addTool(
				"load_act_details",
				"Loads the details of a specific Act template, including its instructions, input template, and configuration options. Useful for inspecting or editing Act definitions.",
				this::getActDetails,
				"actName:string:required:The name of the Act to load.",
				"custom:boolean:optional:If true, retrieves the Act definition only from the user-defined (custom) acts directory. "
						+ "If false, retrieves only the built-in act. If not specified, retrieves effective user-defined acts.");

		provider.addTool(
				"put_project_context_variable",
				"Sets or updates a variable in the project-specific context. Use this to store or update a named variable associated with a particular project, making it available for act execution or prompt templates. "
						+ "It can be used to pass a variable to the next episode of an act.",
				this::putProjectContextVariable,
				"name:string:required:The name of the context variable.",
				"value:string:required:The value to assign to the context variable.");

		provider.addTool(
				"get_project_context_variable",
				"Retrieves the value of a variable from the project-specific context. Use this to access a named variable associated with a particular project for act execution or prompt templates.",
				this::getProjectContextVariable,
				"name:string:required:The name of the context variable to retrieve.");

		provider.addTool(
				"move_to_episode",
				"Moves to the next episode, or to the episode specified by 'id' if provided. Use this to control episode navigation in the project context.",
				this::moveToEpisode,
				"id:string:optional:The ID of the episode to move to.");

		provider.addTool(
				"repeate_episode",
				"Repeats the current episode. This function terminates the current execution and restarts the same episode, preserving the context.",
				this::repeateEpisode);
	}

	/**
	 * Lists all available Act TOML files in the specified directory or built-in
	 * directory.
	 *
	 * @throws IOException
	 */
	public Object getActList(JsonNode params, File workingDir) throws IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Get act list: {}, {}", StringUtils.abbreviate(String.valueOf(params), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}

		List<String> result = getBaseActList().stream().map(line -> "- `" + line).collect(Collectors.toList());
		return StringUtils.join(result, Genai.LINE_SEPARATOR);
	}

	public Set<String> getBaseActList() throws IOException {
		Set<String> result = new HashSet<>();
		CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();

		URL location = codeSource.getLocation();
		String jarFilePath = location.toString();
		String extension = FilenameUtils.getExtension(jarFilePath);
		if ("jar".equalsIgnoreCase(extension) || "zip".equalsIgnoreCase(extension)) {
			if (Strings.CS.startsWith(jarFilePath, "file:/")) {
				jarFilePath = StringUtils.substringAfter(jarFilePath, "file:/").replace("%20", " ");
			}

			File file = new File(jarFilePath);
			try (ZipFile jarFile = new ZipFile(file)) {
				jarFile.stream().forEach(entry -> {
					String actName = StringUtils.substringBetween(entry.getName(), "acts/", TOML_EXTENSION);
					Map<String, Object> properties = new HashMap<>();
					if (actName != null) {
						try {
							ActProcessor.tryLoadActFromClasspath(properties, actName);
							result.add("`" + actName + "`: " + Objects.toString(properties.get("description")));
						} catch (IOException e) {
							throw new IllegalArgumentException(e);
						}
					}
				});
			}
		}

		return result;
	}

	public Object getActDetails(JsonNode props, File workingDir) throws IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Get act details: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}

		Map<String, Object> properties = new HashMap<>();
		try {
			String actName = props.get("actName").asText();
			String custom = props.has("custom") ? props.get("custom").asText() : null;

			String acts = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
			if (custom == null) {
				ActProcessor.loadAct(actName, properties, acts);
			} else {
				if ("true".equals(custom)) {
					ActProcessor.tryLoadActFromDirectory(properties, actName, acts);
				} else {
					ActProcessor.tryLoadActFromClasspath(properties, actName);
				}
			}
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}

		return properties;
	}

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

			Map<String, String> context = contextProjectMap.computeIfAbsent(workingDir, k -> new HashMap<>());
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

			Map<String, String> context = contextProjectMap.get(workingDir);
			if (context == null) {
				result = "No context found for project: " + workingDir;
			} else {
				String value = context.get(name);
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

	/**
	 * Moves to the next episode, or to the episode specified by 'id' if provided.
	 * 
	 * @param params The first argument is expected to be a JsonNode containing an
	 *               optional 'id' property. The second argument is a File
	 *               representing the project directory.
	 * @return Never returns normally; always throws MoveToEpisodeException.
	 */
	public Object moveToEpisode(JsonNode props, File workingDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Move to episode: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}

		String targetId = props.has("id") ? props.get("id").asText() : null;

		throw new MoveToEpisodeException(targetId);
	}

	/**
	 * Repeats the current episode by throwing a RepeatEpisodeException.
	 * 
	 * @param props      The first argument is expected to be a JsonNode (can be
	 *                   empty or contain context).
	 * @param workingDir The project directory.
	 * @return Never returns normally; always throws RepeatEpisodeException.
	 */
	public Object repeateEpisode(JsonNode props, File workingDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Repeat episode: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}
		throw new RepeatEpisodeException();
	}

	@Override
	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
}
