package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provides function tools for discovering and processing files with guidance tags in project directories.
 * <p>
 * This class registers tools for scanning project directories to find files annotated with guidance tags,
 * and for processing those files using a configured model. It integrates with the {@link Genai} provider.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class GuidanceFunctionTools implements FunctionTools {

	private static final Logger logger = LoggerFactory.getLogger(GuidanceFunctionTools.class);

	private Configurator configurator;

	/**
	 * Registers guidance-related function tools with the given GenAI provider.
	 * <ul>
	 *   <li><b>get_files_with_guidance_tags</b>: Returns a mapping of project directories to files that contain guidance tags.</li>
	 *   <li><b>process_files_with_guidance_tag</b>: Processes files with guidance tags using the configured model.</li>
	 * </ul>
	 *
	 * @param provider the GenAI provider to register tools with
	 */
	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"get_files_with_guidance_tags",
				"Returns a mapping of project directories to files that contain guidance tags. " +
						"Scans the specified working directory and collects files annotated with guidance information.",
				this::getGuidanceTaggedFiles,
				"rootDir:string:required:The absolute path to the root project directory or a folder containing multiple projects. " +
						"All scanning operations are performed relative to this directory.",
				"scanDir:string:optional:Specifies the scanning path or pattern. Use a relative path with respect to the current project directory. " +
						"If an absolute path is provided, it must be located within the root project directory. " +
						"Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex " +
						"patterns (e.g., \"regex:^.*/[^/]+\\.java$\").\n\n");

		provider.addTool(
		        "process_files_with_guidance_tag",
		        "Processes files with guidance tags using the configured model. " +
		            "Scans the specified directory and applies guidance processing to each file found.",
		        this::processGuidanceTagFiles,
		        "rootDir:string:required:The absolute path to the root project directory or a folder containing multiple projects. " +
		            "All scanning operations are performed relative to this directory.",
		        "scanDir:string:optional:Specifies the scanning path or pattern. Use a relative path with respect to the current project directory. " +
		            "If an absolute path is provided, it must be located within the root project directory. " +
		            "Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex " +
		            "patterns (e.g., \"regex:^.*/[^/]+\\.java$\").\n\n"
		    );
	}

	/**
	 * Scans the specified directory for files annotated with guidance tags and returns a mapping of project directories to such files.
	 *
	 * @param params     JSON node containing "rootDir" (required) and "scanDir" (optional)
	 * @param projectDir the working directory for scanning operations
	 * @return           a map where each key is a project directory and each value is a list of files with guidance tags
	 * @throws IOException if an I/O error occurs during scanning
	 */
	public Object getGuidanceTaggedFiles(JsonNode params, File projectDir) throws IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Get files with guidance tags");
		}

		Map<File, List<File>> map = new HashMap<>();
		String rootDir = params.get("rootDir").asText();
		String scanDir = params.has("scanDir") ? params.get("scanDir").asText() : projectDir.getAbsolutePath();

		AIFileProcessor processor = new GuidanceProcessor(new File(rootDir), "None", configurator) {
			@Override
			public String process(ProjectLayout projectLayout, File file, String guidance) {
				map.computeIfAbsent(projectLayout.getProjectDir(), k -> new ArrayList<>()).add(file);
				return null;
			}
		};

		processor.scanDocuments(projectDir, scanDir);
		return map;
	}

	/**
	 * Processes files with guidance tags using the configured model.
	 * <p>
	 * Scans the specified directory and applies guidance processing to each file found.
	 * </p>
	 *
	 * @param params     JSON node containing "rootDir" (required) and "scanDir" (optional)
	 * @param projectDir the working directory for scanning operations
	 * @return           a map where each key is a project directory and each value is a list of processed files
	 * @throws IOException if an I/O error occurs during scanning or processing
	 */
	public Object processGuidanceTagFiles(JsonNode params, File projectDir) throws IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Process Guidance Tag Files.");
		}

		Map<File, List<File>> map = new HashMap<>();
		String rootDir = params.get("rootDir").asText();
		String scanDir = params.has("scanDir") ? params.get("scanDir").asText() : projectDir.getAbsolutePath();

		AIFileProcessor processor = new GuidanceProcessor(new File(rootDir),
				configurator.get(GWConstants.MODEL_PROP_NAME), configurator);

		processor.scanDocuments(projectDir, scanDir);
		return map;
	}

	/**
	 * Sets the configurator instance for runtime value resolution.
	 *
	 * @param configurator the configurator to use for resolving runtime values
	 */
	@Override
	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
}