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
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.Genai;
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
 * Provides functional tools for managing and executing "Act" templates within
 * the Ghostwriter framework.
 * <p>
 * Act templates are reusable prompt definitions stored as TOML files, which
 * define instructions and input templates for common workflows. This class
 * enables listing available acts, loading act details, and performing act
 * operations by integrating with the {@link Genai} provider.
 * </p>
 * <p>
 * Tools are registered via {@link #applyTools(Genai)} and can be configured at
 * runtime using {@link #setConfigurator(Configurator)}.
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
	 * Performs the specified Act by name, triggering a predefined action or
	 * workflow asynchronously.
	 *
	 * <p>
	 * This tool is used to initiate an Act (action or workflow) identified by the
	 * given name. The execution is performed asynchronously in a background thread.
	 * The method returns immediately with a response containing a unique GUID and a
	 * status of "processing". The actual result of the Act is serialized to a
	 * temporary file for later retrieval.
	 * </p>
	 *
	 * <p>
	 * Act properties may include placeholders, which are resolved using a
	 * {@link PropertiesConfigurator}. The method also ensures that required
	 * configuration properties (such as scan directory and model) are set, using
	 * values from the provided {@link Configurator} or the supplied properties map.
	 * </p>
	 *
	 * @param actName    The name of the Act to perform.
	 * @param projectDir The project directory context for the Act.
	 * @param properties Optional map of Act properties. May include configuration
	 *                   overrides or parameters for the Act. If {@code null}, only
	 *                   the main configuration is used.
	 * @param config     The configuration object for property resolution and
	 *                   default values.
	 * @return A map containing a unique "process_id" for the asynchronous operation
	 *         and a "status" field set to "processing".
	 * @throws IOException If an I/O error occurs during Act setup or result
	 *                     serialization.
	 */
	@Tool(name = "perform_act", description = "Performs the specified Act by name. Use this tool to asynchronous trigger a predefined action or workflow identified by the given Act name.")
	public Object performAct(
			@Param(name = "act_name", description = "The name of the Act to perform.") String actName,
			@Param(name = "project_dir", description = "The project dir.") File projectDir,
			@Param(name = "properties", description = "Act properties.", defaultValue = Param.NULL) Map<String, String> properties,
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

		if (configurator.get(GWConstants.SCAN_DIR_PROP_NAME, null) == null) {
			configurator.set(GWConstants.SCAN_DIR_PROP_NAME,
					config.get(GWConstants.SCAN_DIR_PROP_NAME, projectDir.getAbsolutePath()));
		}

		ActProcessor actProcessor = new ActProcessor(projectDir, configurator, model);
		String defaultValue = config.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
		String actsLocation = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, defaultValue);
		actProcessor.setActsLocation(actsLocation);

		actProcessor.setAct(actName);

		String path = configurator.get(GWConstants.SCAN_DIR_PROP_NAME, projectDir.getAbsolutePath());

		logger.info("{}", StringUtils.center("Act: " + actName + " ", 80, "-"));

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