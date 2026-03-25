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
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;

import com.fasterxml.jackson.databind.JsonNode;

public class ActFunctionTools implements FunctionTools {

	private static final String TOML_EXTENSION = ".toml";

	private Configurator configurator;

	@Override
	public void applyTools(GenAIProvider provider) {
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
	}

	/**
	 * Lists all available Act TOML files in the specified directory or built-in
	 * directory.
	 * 
	 * @throws IOException
	 */
	private Object getActList(Object... args) throws IOException {
		List<String> result = getBaseActList().stream().map(line -> "- `" + line).collect(Collectors.toList());
		return StringUtils.join(result, GenAIProvider.LINE_SEPARATOR);
	}

	public Set<String> getBaseActList() throws IOException {
		Set<String> result = new HashSet<>();
		CodeSource codeSource = Ghostwriter.class.getProtectionDomain().getCodeSource();

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
							result.add("`" + actName + "`: "
									+ Objects.toString(properties.get("description")));
						} catch (IOException e) {
							throw new IllegalArgumentException(e);
						}
					}
				});
			}
		}

		return result;
	}

	private Object getActDetails(Object... params) throws IOException {
		JsonNode props = (JsonNode) params[0];
		String actName = props.get("actName").asText();
		String custom = props.has("custom") ? props.get("custom").asText() : null;

		Map<String, Object> properties = new HashMap<>();
		String acts = configurator.get(Ghostwriter.GW_ACTS_PROP_NAME, null);
		if (custom == null) {
			ActProcessor.loadAct(actName, properties, acts);
		} else {
			if ("true".equals(custom)) {
				ActProcessor.tryLoadActFromDirectory(properties, actName, acts);
			} else {
				ActProcessor.tryLoadActFromClasspath(properties, actName);
			}
		}

		return properties;
	}

	@Override
	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
}
