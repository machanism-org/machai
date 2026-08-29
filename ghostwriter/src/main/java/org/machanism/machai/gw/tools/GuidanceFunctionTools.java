package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.LayeredConfigurator;
import org.machanism.macha.core.commons.configurator.MutableConfigurator;
import org.machanism.macha.core.commons.configurator.Substitutor;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides function tools for discovering and processing files with guidance
 * tags in project directories.
 * <p>
 * This class registers tools for:
 * <ul>
 * <li>Scanning project directories to find files annotated with guidance
 * tags</li>
 * <li>Processing those files using a configured model, either synchronously or
 * asynchronously</li>
 * <li>Retrieving the results of asynchronous processing by process ID</li>
 * <li>Supplying prompt templates for guidance tag processing</li>
 * </ul>
 * <p>
 * GuidanceFunctionTools integrates with the {@link Genai} provider and supports
 * both custom and built-in project workflows. It manages asynchronous execution
 * and result retrieval using temporary files and process IDs. Methods in this
 * class are typically invoked by an AI provider or workflow engine to enable
 * dynamic, tool-augmented project automation involving guidance tags.
 * </p>
 *
 * @author Viktor Tovstyi
 */
@SupportedFor(ActProcessor.class)
public class GuidanceFunctionTools implements FunctionTools {

	/** Logger used to report asynchronous guidance-processing failures. */
	private static final Logger logger = LoggerFactory.getLogger(GuidanceFunctionTools.class);

	/** Directory below the runtime temporary directory that stores guidance results. */
	private static final String GUIDANCE_FOLDER = "guidance";
	/** Response-map key for an asynchronous guidance execution identifier. */
	private static final String PROCESS_ID_KEY = "process_id";
	/** Response-map key for an asynchronous guidance execution status. */
	private static final String STATUS_KEY = "status";

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle mcpPromptBundle = ResourceBundle.getBundle("mcp-prompts");

	/**
	 * Scans the specified directory and its subdirectories for files annotated with
	 * guidance tags, returning a mapping of project directories to the files that
	 * contain such tags.
	 * <p>
	 * The scan is performed relative to the provided root directory and can be
	 * filtered using a path or pattern (such as glob or regex). Each discovered
	 * file with a guidance tag is grouped under its corresponding project directory
	 * in the returned map.
	 * </p>
	 *
	 * @param rootDir      The absolute path to the root project directory or a
	 *                     folder containing multiple projects. All scanning
	 *                     operations are performed relative to this directory.
	 * @param path         Specifies the scanning path or pattern. Use a relative
	 *                     path with respect to the current project directory. If an
	 *                     absolute path is provided, it must be located within the
	 *                     root project directory. Supported patterns: raw directory
	 *                     names, glob patterns (e.g., "glob:*.java"), or regex
	 *                     patterns (e.g., "regex:^.java$"). Default: "glob:*.*"
	 * @param projectDir   The project directory to use as the working directory for
	 *                     scanning operations.
	 * @param configurator The configuration object.
	 * @return A map where each key is a project directory and each value is a list
	 *         of files with guidance tags found in that directory.
	 * @throws IOException if an I/O error occurs during scanning.
	 */
	@Tool(name = "get-files-with-guidance-tags", description = "Specialized discovery tool for Guidance-Driven Processing (GDP). Scans files and returns "
			+ "only those that contain in-code @guidance marginalia tags (e.g., '// @guidance: ...' style "
			+ "comments), grouped by their owning project directory. This is NOT a general-purpose file "
			+ "search tool — it does not match on file name, content keywords, or arbitrary patterns; it "
			+ "specifically detects the presence of embedded @guidance annotations used to drive localized, "
			+ "file-scoped AI instructions. Use this tool as the first step in a GDP workflow to identify "
			+ "which files require guidance-based processing before invoking any guidance-execution step. "
			+ "The result is a map where each key is a project directory (relevant for multi-module or "
			+ "multi-project roots) and each value is the list of files within that project containing at "
			+ "least one @guidance tag.")
	public Map<File, List<File>> getGuidanceTaggedFiles(
			@Param(name = "root-dir", description = "The absolute path to the root project directory, or to a parent "
					+ "folder containing multiple projects/modules. This defines the outer boundary for the scan; "
					+ "all file lookups and path resolutions are performed relative to this directory.") String rootDir,
			@Param(name = "path", description = "The scanning path or pattern used to select candidate files to "
					+ "inspect for @guidance tags. Provide a path relative to 'project_dir'. If an absolute path is "
					+ "given, it must still fall within 'root_dir'. Supported forms: a plain relative directory "
					+ "name, a glob pattern (e.g., \"glob:**/*.java\"), or a regex pattern "
					+ "(e.g., \"regex:^.*/[^/]+\\.java$\"). Only files matching this pattern are scanned for "
					+ "@guidance tags — files outside the pattern are skipped entirely, regardless of their content.", defaultValue = "glob:**/*.*") String path,
			@Param(name = "project-dir", description = "The specific project (or module) directory to scan for "
					+ "@guidance-tagged files. Must reside within 'root_dir'. When 'root_dir' spans multiple "
					+ "projects, this parameter narrows the scan to a single project so results can be grouped "
					+ "and attributed correctly.") File projectDir,
			Configurator configurator)
			throws IOException {
		Map<File, List<File>> map = new HashMap<>();

		AIFileProcessor processor = new GuidanceProcessor(new File(rootDir), null, configurator) {
			@Override
			public String process(ProjectLayout projectLayout, File file, String guidance) {
				map.computeIfAbsent(projectLayout.getProjectDir(), k -> new ArrayList<>()).add(file);
				return null;
			}
		};

		processor.scanDocuments(projectDir, path);
		return map;
	}

	/**
	 * Runs guidance processing in the background and persists its report.
	 *
	 * @param processor configured guidance processor
	 * @param projectDir project directory to scan
	 * @param path scan path or pattern
	 * @param tempFile file that receives the serialized report
	 */
	private void saveGuidanceResult(GuidanceProcessor processor, File projectDir, String path, File tempFile) {
		try {
			processor.scanDocuments(projectDir, path);
			writeGuidanceResult(tempFile, processor.getReport());
		} catch (Exception ex) {
			logger.error("Error during background guidance tag file processing. Temp file: '{}'",
					tempFile.getAbsolutePath(), ex);
		}
	}

	/**
	 * Serializes a guidance-processing report to its temporary result file.
	 *
	 * @param tempFile destination temporary file
	 * @param result report to serialize
	 * @throws IOException if the report cannot be written
	 */
	private void writeGuidanceResult(File tempFile, List<Map<String, Object>> result) throws IOException {
		// Sonar java:S2095: always close the serialized result stream.
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(tempFile))) {
			output.writeObject(result);
		}
	}

	/**
	 * Asynchronously processes files with guidance tags using the configured model.
	 * <p>
	 * Scans the files in the specified {@code project_dir} (and optionally matching
	 * the given {@code path} pattern) and applies guidance processing to each file
	 * found. The processing is performed in a background thread. The method returns
	 * immediately with a response containing a unique process ID and a status of
	 * "processing". The actual result is serialized to a temporary file for later
	 * retrieval using the process ID.
	 * </p>
	 *
	 * @param projectDir The project directory in which to scan for files.
	 * @param properties Optional map of Act properties, such as configuration
	 *                   overrides or parameters for the guidance processing. If
	 *                   {@code null}, only the main configuration is used.
	 * @param path       Specifies the scanning path or pattern. Use a relative path
	 *                   with respect to the current project directory. If an
	 *                   absolute path is provided, it must be located within the
	 *                   root project directory. Supported patterns: raw directory
	 *                   names, glob patterns (e.g., "glob:**.java"), or regex
	 *                   patterns (e.g., "regex:^.[^/]+\\.java$"). Default:
	 *                   "${project_dir}".
	 * @param config     The configuration object for property resolution and
	 *                   default values.
	 * @return A map containing:
	 *         <ul>
	 *         <li><b>process_id</b>: The unique identifier for the asynchronous
	 *         operation.</li>
	 *         <li><b>status</b>: "processing" to indicate the operation is running
	 *         asynchronously.</li>
	 *         </ul>
	 * @throws IOException If there is an error initializing the processor or
	 *                     creating the temp file.
	 */
	@Tool(name = "process-files-with-guidance-tag", description = "Scans files for embedded guidance-tag directives (marginalia such as `@guidance` comments) "
			+ "and processes each matching file using the configured AI model to apply the requested guidance. "
			+ "Files are discovered by scanning the location described by `path`, resolved relative to `project_dir` "
			+ "(or the project's root directory when `path` is omitted or absolute). "
			+ "The model used for processing is resolved in the following order: (1) a `model` entry inside "
			+ "`properties`, if provided; (2) the default model configured for the current project/session. "
			+ "Any other entries supplied in `properties` are applied to the execution configuration before "
			+ "processing starts, and their values may reference existing configuration placeholders (e.g. `${...}`), "
			+ "which are resolved prior to being applied. "
			+ "Execution can run either synchronously or asynchronously, controlled by the `async` parameter: "
			+ "in synchronous mode, the tool blocks until all matched files have been processed and returns the "
			+ "full processing report immediately; in asynchronous mode (recommended for MCP server usage or "
			+ "long-running scans), the tool starts a background task, immediately returns a `process_id` and a "
			+ "`status` of `processing`, and the final report is written to a temporary file for later retrieval "
			+ "once the background task completes.")
	public Object processGuidanceTagFiles(
			@Param(name = "project-dir", description = "The project dir.") File projectDir,
			@Param(name = "properties", description = "Act properties.", defaultValue = Param.NULL) Map<String, String> properties,
			@Param(name = "path", description = "Specifies the scanning path or pattern used to locate files to process. "
					+ "Use a relative path with respect to the current project directory. "
					+ "If an absolute path is provided, it must be located within the root project directory. "
					+ "Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex "
					+ "patterns (e.g., \"regex:^.*/[^/]+\\.java$\"). Defaults to the project directory itself, meaning "
					+ "the entire project is scanned.", defaultValue = "${project_dir}") String path,
			@Param(name = "async", description = "Controls the execution mode. If true, processing runs in the "
					+ "background and the tool immediately returns a `process_id` and `status` for later polling — "
					+ "useful for MCP server execution or long-running scans that shouldn't block the caller. "
					+ "If false, the tool blocks until processing completes and returns the full report directly.", defaultValue = "false") boolean async,
			Configurator config)
			throws IOException {

		MutableConfigurator configurator = new LayeredConfigurator(config);

		String model = null;
		if (properties != null) {
			for (Map.Entry<String, String> e : properties.entrySet()) {
				String value = Substitutor.replace(e.getValue(), configurator);
				configurator.set(e.getKey(), value);
			}
			model = properties.get(GWConstants.MODEL_PROP_NAME);
		}

		if (model == null) {
			model = configurator.get(GWConstants.MODEL_PROP_NAME);
		}

		final GuidanceProcessor processor = new GuidanceProcessor(projectDir, model, configurator);

		if (async) {
			final String processId = UUID.randomUUID().toString();
			final String tempDir = ProjectLayout.getTempDir();
			final File tempFile = new File(new File(tempDir, GUIDANCE_FOLDER), processId + ".tmp");
			tempFile.getParentFile().mkdirs();

			ExecutorService bgExecutor = Executors.newSingleThreadExecutor();
			try {
				bgExecutor.submit(() -> saveGuidanceResult(processor, projectDir, path, tempFile));
			} finally {
				// Sonar java:S2095: release the executor after the queued task completes.
				bgExecutor.shutdown();
			}

			Map<String, Object> response = new HashMap<>();
			response.put(PROCESS_ID_KEY, processId);
			response.put(STATUS_KEY, "processing");
			return response;

		} else {
			processor.scanDocuments(projectDir, path);
			return processor.getReport();
		}
	}

	/**
	 * Retrieves the result of a previously started guidance tag file processing by
	 * its GUID.
	 * <p>
	 * This method reconstructs the path to the temporary file where the result was
	 * stored, using the provided GUID and the system's temporary directory. If the
	 * result file exists, it reads and returns the result. If the file does not
	 * exist, it returns a status indicating that the result is still processing or
	 * unavailable.
	 * </p>
	 *
	 * @param processId The GUID returned when the processing was started. Used to
	 *                  identify the result file.
	 * @return A map containing:
	 *         <ul>
	 *         <li><b>guid</b>: The provided GUID.</li>
	 *         <li><b>status</b>: "done" if the result is available, "processing"
	 *         otherwise.</li>
	 *         <li><b>result</b>: The list of file and guidance tag entries if
	 *         available.</li>
	 *         <li><b>message</b>: An informational message if the result is not
	 *         ready.</li>
	 *         </ul>
	 * @throws IOException If there is an error reading the result from the temp
	 *                     file.
	 */
	@Tool(name = "get-process-guidance-tag-files-result", description = "Retrieves the result of a previously started guidance tag file processing by GUID.")
	public Object getProcessGuidanceTagFilesResult(
			@Param(name = "process-id", description = "The GUID returned when the processing was started.") String processId)
			throws IOException {

		String tempDir = ProjectLayout.getTempDir();
		File tempFile = new File(new File(tempDir, GUIDANCE_FOLDER), processId + ".tmp");

		if (!tempFile.exists()) {
			Map<String, Object> response = new HashMap<>();
			response.put(PROCESS_ID_KEY, processId);
			response.put(STATUS_KEY, "processing");
			response.put("message", "Result is not ready yet or file does not exist.");
			return response;
		}

		Object result;
		// Sonar java:S2093: close the input stream even when deserialization fails.
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile))) {
			result = ois.readObject();
		} catch (Exception e) {
			throw new IOException("Error reading guidance tag files result from temp file", e);
		}

		Map<String, Object> response = new HashMap<>();
		response.put(PROCESS_ID_KEY, processId);
		response.put(STATUS_KEY, "done");
		response.put("result", result);
		return response;
	}

	/**
	 * Provides the prompt template used to process files containing guidance tags.
	 * <p>
	 * The returned template is resolved from the {@code mcp-prompts} resource
	 * bundle and is intended for use by the guidance-tag processing workflow.
	 * </p>
	 *
	 * @param projectDir The root folder of the project, or the root folder
	 *                   containing projects to scan.
	 * @param path       The scanning path or pattern used to select files.
	 * @return The prompt template for processing files with guidance tags.
	 */
	@Prompt(name = "process-guidance-tags", description = "Processes files with guidance tags using the configured model.")
	public String getGuidancePrompt(
			@Param(name = "project-dir", description = "The root folder of the project or the root folder of projects to scan.") String projectDir,
			@Param(name = "path", description = "Scanning path or pattern.", defaultValue = "${project_dir}") String path) {
		return mcpPromptBundle.getString("process_guidance");
	}
}
