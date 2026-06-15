package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Role;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Provides function tools for discovering and processing files with guidance
 * tags in project directories.
 * <p>
 * This class registers tools for scanning project directories to find files
 * annotated with guidance tags, and for processing those files using a
 * configured model. It integrates with the {@link Genai} provider.`
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class GuidanceFunctionTools implements FunctionTools {

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle mcpPromptBundle = ResourceBundle.getBundle("mcp-prompts");

	/**
	 * Scans the specified directory for files annotated with guidance tags and
	 * returns a mapping of project directories to such files.
	 *
	 * @param params       JSON node containing "rootDir" (required) and "paths"
	 *                     (optional)
	 * @param projectDir   the working directory for scanning operations
	 * @param configurator
	 * @return a map where each key is a project directory and each value is a list
	 *         of files with guidance tags
	 * @throws IOException if an I/O error occurs during scanning
	 */
	@Tool(name = "get_files_with_guidance_tags", description = "Returns a mapping of project directories to files that contain guidance tags. "
			+ "Scans the specified working directory and collects files annotated with guidance information.")
	public Map<File, List<File>> getGuidanceTaggedFiles(
			@Param(name = "root_dir", description = "The absolute path to the root project directory or a folder containing multiple projects. "
					+ "All scanning operations are performed relative to this directory.") String rootDir,
			@Param(name = "paths", description = "Specifies the scanning path or pattern. Use a relative path with respect to the current project directory. "
					+ "If an absolute path is provided, it must be located within the root project directory. "
					+ "Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex "
					+ "patterns (e.g., \"regex:^.*/[^/]+\\.java$\").", defaultValue = "glob:**/*.*") String paths,
			@Param(name = "project_dir", description = "The project dir.") File projectDir, Configurator configurator)
			throws IOException {
		Map<File, List<File>> map = new HashMap<>();

		AIFileProcessor processor = new GuidanceProcessor(new File(rootDir), null, configurator) {
			@Override
			public String process(ProjectLayout projectLayout, File file, String guidance) {
				map.computeIfAbsent(projectLayout.getProjectDir(), k -> new ArrayList<>()).add(file);
				return null;
			}
		};

		processor.scanDocuments(projectDir, paths);
		return map;
	}

	/**
	 * Processes files with guidance tags using the configured model.
	 * <p>
	 * Scans the specified directory and applies guidance processing to each file
	 * found.
	 * </p>
	 * 
	 * @param rootDir
	 * @param configurator
	 */
	@Tool(name = "process_files_with_guidance_tag", description = "Processes files with guidance tags using the configured model. "
			+ "Scans the `paths` matched files in the `project_dir` or `root_dir` directory and applies guidance processing to each file found.")
	public List<Entry<File, String>> processGuidanceTagFiles(
			@Param(name = "project_dir", description = "The project dir.") File projectDir,
			@Param(name = "root_dir", description = "The absolute path to the root project directory or a folder containing multiple projects. "
					+ "All scanning operations are performed relative to this directory.", defaultValue = "${project_dir}") String rootDir,
			@Param(name = "properties", description = "Act properties, specified as NAME=VALUE pairs separated by newline (\\n).", defaultValue = "") String envStr,
			@Param(name = "paths", description = "Specifies the scanning path or pattern. Use a relative path with respect to the current project directory. "
					+ "If an absolute path is provided, it must be located within the root project directory. "
					+ "Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex "
					+ "patterns (e.g., \"regex:^.*/[^/]+\\.java$\").", defaultValue = "${project_dir}") String paths,
			Configurator config)
			throws IOException {
		PropertiesConfigurator configurator = new PropertiesConfigurator();

		String model = null;
		Map<String, String> properties;
		if (!envStr.isEmpty()) {
			properties = CommandFunctionTools.parseEnv(envStr, configurator);
			properties.entrySet().stream().forEach(e -> configurator.set(e.getKey(), e.getValue()));
			model = properties.get(GWConstants.MODEL_PROP_NAME);
		}

		if (model == null) {
			model = config.get(GWConstants.MODEL_PROP_NAME);
		}

		GuidanceProcessor processor = new GuidanceProcessor(new File(rootDir),
				configurator.get(GWConstants.MODEL_PROP_NAME), configurator);

		processor.scanDocuments(projectDir, paths);
		return processor.getReport();
	}

	@Prompt(name = "Process Guidance Tags", description = "Processes files with guidance tags using the configured model.", role = Role.ASSISTANT)
	public String getGuidancePrompt(
			@Param(name = "project_dir", description = "The root folder of the project or the root folder of projects to scan.") String projectDir,
			@Param(name = "paths", description = "Scanning path or pattern.", defaultValue = "${project_dir}") String paths) {
		return mcpPromptBundle.getString("process_guidance");
	}
}