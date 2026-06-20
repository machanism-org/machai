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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(ActFunctionTools.class);

	private static final String GUIDANCE_FOLDER = "guidance";

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle mcpPromptBundle = ResourceBundle.getBundle("mcp-prompts");

	/**
	 * Scans the specified directory for files annotated with guidance tags and
	 * returns a mapping of project directories to such files.
	 *
	 * @param params       JSON node containing "rootDir" (required) and "path"
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
			@Param(name = "path", description = "Specifies the scanning path or pattern. Use a relative path with respect to the current project directory. "
					+ "If an absolute path is provided, it must be located within the root project directory. "
					+ "Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex "
					+ "patterns (e.g., \"regex:^.*/[^/]+\\.java$\").", defaultValue = "glob:**/*.*") String path,
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

		processor.scanDocuments(projectDir, path);
		return map;
	}

	/**
	 * Processes files with guidance tags using the configured model, either
	 * synchronously or asynchronously based on the provided timeout.
	 * <p>
	 * Scans the specified path in the project or root directory and applies
	 * guidance processing to each file found. If processing completes within the
	 * timeout, the result is returned immediately and also stored in a temporary
	 * file named <code>guidance_result_&lt;guid&gt;.tmp</code> in the system's
	 * temporary directory. If not, the method returns a GUID and status, allowing
	 * the caller to retrieve the result later using the GUID.
	 * </p>
	 *
	 * @param projectDir     The project directory.
	 * @param rootDir        The absolute path to the root project directory or a
	 *                       folder containing multiple projects.
	 * @param properties     Act properties.
	 * @param path           Specifies the scanning path or pattern.
	 * @param config         The configuration object.
	 * @param timeoutSeconds The timeout in seconds for synchronous execution.
	 * @return If completed within timeout: the list of file and guidance tag
	 *         entries. If not: a map containing:
	 *         <ul>
	 *         <li><b>guid</b>: The unique identifier for the processing
	 *         execution.</li>
	 *         <li><b>status</b>: "processing" to indicate the operation is running
	 *         asynchronously.</li>
	 *         </ul>
	 * @throws IOException If there is an error initializing the processor or
	 *                     creating the temp file.
	 */
	@Tool(name = "process_files_with_guidance_tag", description = "Asynchronous processes files with guidance tags using the configured model. "
			+ "Scans the `path` matched files in the `project_dir` or `root_dir` directory and applies guidance processing to each file found.")
	public Object processGuidanceTagFiles(
			@Param(name = "project_dir", description = "The project dir.") File projectDir,
			@Param(name = "properties", description = "Act properties.", defaultValue = Param.NULL) Map<String, String> properties,
			@Param(name = "path", description = "Specifies the scanning path or pattern. Use a relative path with respect to the current project directory. "
					+ "If an absolute path is provided, it must be located within the root project directory. "
					+ "Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex "
					+ "patterns (e.g., \"regex:^.*/[^/]+\\.java$\").", defaultValue = "${project_dir}") String path,
			Configurator config)
			throws IOException {

		PropertiesConfigurator configurator = new PropertiesConfigurator();

		String model = null;
		if (properties != null) {
			for (Map.Entry<String, String> e : properties.entrySet()) {
				String value = CommandFunctionTools.replace(e.getValue(), configurator);
				configurator.set(e.getKey(), value);
			}
			model = properties.get(GWConstants.MODEL_PROP_NAME);
		}

		if (model == null) {
			model = config.get(GWConstants.MODEL_PROP_NAME);
		}

		final GuidanceProcessor processor = new GuidanceProcessor(projectDir,
				configurator.get(GWConstants.MODEL_PROP_NAME), configurator);

		final String processId = UUID.randomUUID().toString();
		final String tempDir = ProjectLayout.getTempDir();
		final File tempFile = new File(tempDir, GUIDANCE_FOLDER + "/" + processId + ".tmp");
		tempFile.getParentFile().mkdirs();

		ExecutorService bgExecutor = Executors.newSingleThreadExecutor();
		bgExecutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					processor.scanDocuments(projectDir, path);
					List<Map<String, Object>> result = processor.getReport();
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile));
					oos.writeObject(result);
					oos.close();
				} catch (Exception ex) {
					logger.error("Error during background guidance tag file processing. Temp file: '{}'",
							tempFile.getAbsolutePath(), ex);
				}
			}
		});
		bgExecutor.shutdown();

		Map<String, Object> response = new HashMap<>();
		response.put("process_id", processId);
		response.put("status", "processing");
		return response;
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
	@Tool(name = "get_process_guidance_tag_files_result", description = "Retrieves the result of a previously started guidance tag file processing by GUID.")
	public Object getProcessGuidanceTagFilesResult(
			@Param(name = "process_id", description = "The GUID returned when the processing was started.") String processId)
			throws IOException {

		String tempDir = ProjectLayout.getTempDir();
		File tempFile = new File(tempDir, GUIDANCE_FOLDER + "/" + processId + ".tmp");

		if (!tempFile.exists()) {
			Map<String, Object> response = new HashMap<>();
			response.put("process_id", processId);
			response.put("status", "processing");
			response.put("message", "Result is not ready yet or file does not exist.");
			return response;
		}

		Object result;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(tempFile));
			result = ois.readObject();
		} catch (Exception e) {
			throw new IOException("Error reading guidance tag files result from temp file", e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException ignore) {
				}
			}
		}

		Map<String, Object> response = new HashMap<>();
		response.put("process_id", processId);
		response.put("status", "done");
		response.put("result", result);
		return response;
	}

	@Prompt(name = "Process Guidance Tags", description = "Processes files with guidance tags using the configured model.", role = Role.ASSISTANT)
	public String getGuidancePrompt(
			@Param(name = "project_dir", description = "The root folder of the project or the root folder of projects to scan.") String projectDir,
			@Param(name = "path", description = "Scanning path or pattern.", defaultValue = "${project_dir}") String path) {
		return mcpPromptBundle.getString("process_guidance");
	}
}