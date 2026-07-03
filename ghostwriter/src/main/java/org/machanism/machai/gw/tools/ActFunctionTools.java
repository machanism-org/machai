package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.LayeredConfigurator;
import org.machanism.macha.core.commons.configurator.MutableConfigurator;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides function tools for managing and executing Ghostwriter Acts within a
 * project.
 * <p>
 * This class exposes methods for:
 * <ul>
 * <li>Loading Act template details (including instructions, input templates,
 * and configuration options)</li>
 * <li>Asynchronously performing an Act and storing the result for later
 * retrieval</li>
 * <li>Retrieving the result of a previously started Act by process ID</li>
 * <li>Supplying prompt templates for Act execution</li>
 * </ul>
 * <p>
 * Acts are reusable, named workflows or actions defined in the project or
 * classpath. This class supports both custom and built-in Act definitions, and
 * handles asynchronous execution and result management using temporary files
 * and process IDs.
 * </p>
 * <p>
 * Methods in this class are typically invoked by an AI provider or workflow
 * engine to enable dynamic, tool-augmented project automation.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class ActFunctionTools implements FunctionTools {

	private static final String ACT_FOLDER_NAME = "act";

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(ActFunctionTools.class);

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle mcpPromptBundle = ResourceBundle.getBundle("mcp-prompts");

	/**
	 * Loads the details of a specific Act template, including instructions, input
	 * template, and configuration options.
	 * 
	 * @param configurator
	 */
	@Tool(name = "load_act_details", description = "Loads the details of a specific Act template, including its instructions, input template, and "
			+ "configuration options. Useful for inspecting or editing Act definitions.")
	public Object getActDetails(
			@Param(name = "act_name", description = "The name of the Act to load.") String actName,
			@Param(name = "project_dir", description = "The project dir.") File projectDir,
			Configurator configurator)
			throws IOException {
		actName = StringUtils.substringBefore(actName, "#");
		actName = StringUtils.substringBefore(actName, " ");

		Map<String, Object> result = new HashMap<>();

		Map<String, Object> prop1 = new HashMap<>();
		String acts = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
		if (ActProcessor.tryLoadActFromDirectory(prop1, actName, acts) != null) {
			result.put("custom", prop1);
		}

		Map<String, Object> prop2 = new HashMap<>();
		if (ActProcessor.tryLoadActFromClasspath(prop2, actName) != null) {
			result.put("build-in", prop2);
		}

		if (result.isEmpty()) {
			result.put("message", "act not found");
			result.put("act_name", actName);
			result.put("project_dir", projectDir);
			result.put("acts", acts);
		}

		return result;
	}

	/**
	 * Performs the specified Act by name.
	 * <p>
	 * Use this tool to trigger a predefined action or workflow identified by the
	 * given Act name. This method supports both synchronous and asynchronous
	 * execution modes based on the `async` parameter.
	 * </p>
	 *
	 * @param actName    The name of the Act to perform.
	 * @param projectDir The project directory where the Act will be executed.
	 * @param properties Act properties to override default configuration values;
	 *                   may be {@code null}.
	 * @param async      If {@code true}, the Act will be executed asynchronously,
	 *                   and the method will return immediately with a process ID.
	 *                   If {@code false}, the Act will be executed synchronously,
	 *                   and the method will return the Act's result.
	 * @param config     The configuration object.
	 * @return A response object containing the Act's result (for synchronous
	 *         execution) or a process ID and status (for asynchronous execution).
	 * @throws IOException If an error occurs during Act processing.
	 */
	@Tool(name = "perform_act", description = "Performs the specified Act by name. Use this tool to trigger a predefined action or workflow identified by the given Act name.")
	public Object performAct(
			@Param(name = "act_name", description = "The name of the Act to perform.") String actName,
			@Param(name = "project_dir", description = "The project directory.") File projectDir,
			@Param(name = "properties", description = "Act properties to override default configuration values.", defaultValue = Param.NULL) Map<String, String> properties,
			@Param(name = "async", description = "If true, the function tool will be executed asynchronously (useful for MCP server execution). If false, it will be executed synchronously.", defaultValue = "false") boolean async,
			Configurator config)
			throws IOException {

		MutableConfigurator configurator = new LayeredConfigurator(config);

		String model = null;
		if (properties != null) {
			for (Map.Entry<String, String> e : properties.entrySet()) {
				String value = CommandFunctionTools.replace(e.getValue(), configurator);
				configurator.set(e.getKey(), value);
			}
		}

		if (model == null) {
			model = configurator.get(GWConstants.MODEL_PROP_NAME);
		}

		if (configurator.get(GWConstants.SCAN_DIR_PROP_NAME, null) == null) {
			configurator.set(GWConstants.SCAN_DIR_PROP_NAME,
					configurator.get(GWConstants.SCAN_DIR_PROP_NAME, projectDir.getAbsolutePath()));
		}

		ActProcessor actProcessor = new ActProcessor(projectDir, model, configurator);
		String defaultValue = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
		String actsLocation = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, defaultValue);
		actProcessor.setActsLocation(actsLocation);

		actProcessor.setAct(actName);

		String path = configurator.get(GWConstants.SCAN_DIR_PROP_NAME, projectDir.getAbsolutePath());

		logger.info("{}", StringUtils.center("Act: " + actName + " ", 80, "-"));

		if (async) {
			final String processId = UUID.randomUUID().toString();
			final String tempDir = ProjectLayout.getTempDir();
			final File tempFile = new File(tempDir, getFileName(processId));
			tempFile.getParentFile().mkdirs();

			ExecutorService bgExecutor = Executors.newSingleThreadExecutor();
			bgExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						actProcessor.scanDocuments(projectDir, path);
						Object result = actProcessor.getResults();
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile));
						oos.writeObject(result);
						oos.close();
					} catch (Exception ex) {
						logger.error("Error processing act asynchronously", ex);
					}
				}
			});
			bgExecutor.shutdown();

			Map<String, Object> response = new HashMap<>();
			response.put("process_id", processId);
			response.put("status", "processing");
			return response;

		} else {
			actProcessor.scanDocuments(projectDir, path);
			return actProcessor.getResults();
		}
	}

	private String getFileName(final String processId) {
		return ACT_FOLDER_NAME + "/" + processId + ".tmp";
	}

	/**
	 * Retrieves the result of a previously started Act by its GUID.
	 * <p>
	 * This method reconstructs the path to the temporary file where the Act result
	 * was stored, using the provided GUID and the system's temporary directory. If
	 * the result file exists, it reads and returns the result. If the file does not
	 * exist, it returns a status indicating that the result is still processing or
	 * unavailable.
	 * </p>
	 *
	 * @param processId The GUID returned when the Act was started. Used to identify
	 *                  the result file.
	 * @return A map containing:
	 *         <ul>
	 *         <li><b>guid</b>: The provided GUID.</li>
	 *         <li><b>status</b>: "done" if the result is available, "processing"
	 *         otherwise.</li>
	 *         <li><b>result</b>: The Act result object if available.</li>
	 *         <li><b>message</b>: An informational message if the result is not
	 *         ready.</li>
	 *         </ul>
	 * @throws IOException If there is an error reading the result from the temp
	 *                     file.
	 */
	@Tool(name = "get_act_result", description = "Retrieves the result of a previously started Act by GUID.")
	public Object getActResult(
			@Param(name = "process_id", description = "The process_id returned when the Act was started.") String processId)
			throws IOException {

		String tempDir = ProjectLayout.getTempDir();
		File tempFile = new File(tempDir, getFileName(processId));

		if (!tempFile.exists()) {
			Map<String, Object> response = new HashMap<>();
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
			throw new IOException("Error reading act result from temp file", e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException ignore) {
				}
			}
		}

		Map<String, Object> response = new HashMap<>();
		response.put("status", "done");
		response.put("result", result);
		return response;
	}

	@Prompt(name = "Perform Act", description = "Executes the specified act based on the provided name parameter.")
	public String actPrompts(@Param(name = "name", description = "The name of the Act to perform.") String actName) {
		return mcpPromptBundle.getString("process_act");
	}

}