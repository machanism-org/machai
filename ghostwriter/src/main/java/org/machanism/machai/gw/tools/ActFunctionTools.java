package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
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

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(ActFunctionTools.class);

	private static final String TOML_EXTENSION = ".toml";

	/**
	 * Lists all available Act TOML files in the specified directory or built-in
	 * directory.
	 */
	@Tool(name = "build_in_list_acts", description = "Retrieves a list of all available Act templates that can be used with Ghostwriter. Acts are reusable "
			+ "prompt templates stored as TOML files, which define instructions and input templates for "
			+ "common workflows.")
	public Set<Map<String, String>> getActList() throws IOException {
		return getBaseActList();
	}

	/**
	 * Retrieves the set of available Act template names from the classpath or JAR
	 * file.
	 *
	 * @return a set of Act template names with descriptions
	 * @throws IOException if an I/O error occurs during act discovery
	 */

	public Set<Map<String, String>> getBaseActList() throws IOException {
		Set<Map<String, String>> result = new HashSet<>();
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
				jarFile.stream().forEach(entry -> addActDescription(result, entry.getName()));
			}
		}

		return result;
	}

	/**
	 * Adds the description of an Act template to the result set if the entry name
	 * matches an Act TOML file.
	 *
	 * @param result    the set to add the Act description to
	 * @param entryName the name of the entry in the JAR or directory
	 */
	private void addActDescription(Set<Map<String, String>> result, String entryName) {
		String actName = StringUtils.substringBetween(entryName, "acts/", TOML_EXTENSION);
		if (actName == null) {
			return;
		}

		Map<String, Object> properties = new HashMap<>();
		try {
			ActProcessor.tryLoadActFromClasspath(properties, actName);
			String description = properties.get("description") != null
					? properties.get("description").toString()
					: "";
			Map<String, String> actInfo = new HashMap<>();
			actInfo.put("name", actName);
			actInfo.put("description", description);
			result.add(actInfo);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

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
			@Param(name = "custom", description = "If true, retrieves the Act definition only from the user-defined (custom) "
					+ "acts directory. If false, retrieves only the built-in act. If not specified, retrieves "
					+ "effective user-defined acts.", defaultValue = "false") boolean custom,
			@Param(name = "project_dir", description = "The project dir.") File projectDir,
			Configurator configurator)
			throws IOException {
		Map<String, Object> properties = new HashMap<>();
		try {
			String acts = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
			if (custom) {
				ActProcessor.tryLoadActFromDirectory(properties, actName, acts);
			} else {
				ActProcessor.tryLoadActFromClasspath(properties, actName);
			}
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}

		return properties;
	}

	/**
	 * Performs the specified Act by name, either synchronously or asynchronously
	 * based on the provided timeout.
	 * <p>
	 * This method triggers a predefined action or workflow identified by the given
	 * Act name. The Act is executed in a background thread. If the Act completes
	 * within the specified timeout (in seconds), the result is returned immediately
	 * and also stored in a temporary file named
	 * <code>act_result_&lt;guid&gt;.tmp</code> in the system's temporary directory.
	 * If the Act does not complete within the timeout, it continues processing
	 * asynchronously, and the method returns a GUID and status, allowing the caller
	 * to retrieve the result later using the GUID.
	 * </p>
	 *
	 * @param actName        The name of the Act to perform.
	 * @param envStr         Act properties, specified as NAME=VALUE pairs separated
	 *                       by newline (\n).
	 * @param timeoutSeconds The timeout in seconds for synchronous execution. If
	 *                       the Act does not complete within this time, it will
	 *                       continue asynchronously.
	 * @param projectDir     The project directory.
	 * @param config         The configuration object.
	 * @return If completed within timeout: the Act result object. If not: a map
	 *         containing:
	 *         <ul>
	 *         <li><b>guid</b>: The unique identifier for the Act execution.</li>
	 *         <li><b>status</b>: "processing" to indicate the Act is running
	 *         asynchronously.</li>
	 *         </ul>
	 * @throws IOException If there is an error initializing the Act or creating the
	 *                     temp file.
	 */
	@Tool(name = "perform_act", description = "Performs the specified Act by name. Use this tool to trigger a predefined action or workflow identified by the given Act name.")
	public Object performAct(
			@Param(name = "act_name", description = "The name of the Act to perform.") String actName,
			@Param(name = "project_dir", description = "The project dir.") File projectDir,
			@Param(name = "properties", description = "Act properties, specified as NAME=VALUE pairs separated by newline (\\n).", defaultValue = "") String envStr,
			@Param(name = "timeout_seconds", description = "", defaultValue = "120") int timeoutSeconds,
			Configurator config)
			throws IOException {
		PropertiesConfigurator configurator = new PropertiesConfigurator();

		String model = null;
		Map<String, String> properties = null;
		if (!envStr.isEmpty()) {
			properties = CommandFunctionTools.parseEnv(envStr, configurator);
			for (Map.Entry<String, String> e : properties.entrySet()) {
				configurator.set(e.getKey(), e.getValue());
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

		// Prepare GUID and temp file for async result
		final String guid = UUID.randomUUID().toString();
		final String tempDir = System.getProperty("java.io.tmpdir");
		final File tempFile = new File(tempDir, "act_result_" + guid + ".tmp");

		ExecutorService executor = Executors.newSingleThreadExecutor();

		Callable<Object> task = new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				actProcessor.scanDocuments(projectDir, path);
				return actProcessor.getResults();
			}
		};

		Future<Object> future = executor.submit(task);

		try {
			Object result = future.get(timeoutSeconds, TimeUnit.SECONDS);
			executor.shutdown();
			// Store result in temp file for consistency
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile));
			oos.writeObject(result);
			oos.close();
			return result;
		} catch (TimeoutException e) {
			executor.shutdown();
			// Continue processing asynchronously
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
			response.put("guid", guid);
			response.put("status", "processing");
			return response;
		} catch (Exception e) {
			executor.shutdown();
			throw new IOException("Error performing act", e);
		}
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
	 * @param guid The GUID returned when the Act was started. Used to identify the
	 *             result file.
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
			@Param(name = "guid", description = "The GUID returned when the Act was started.") String guid)
			throws IOException {

		// Reconstruct the temp file path using the system temp directory and the known
		// naming pattern
		String tempDir = System.getProperty("java.io.tmpdir");
		File tempFile = new File(tempDir, "act_result_" + guid + ".tmp");

		if (!tempFile.exists()) {
			Map<String, Object> response = new HashMap<>();
			response.put("guid", guid);
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

		// Optionally, delete the temp file after reading
		// tempFile.delete();

		Map<String, Object> response = new HashMap<>();
		response.put("guid", guid);
		response.put("status", "done");
		response.put("result", result);
		return response;
	}

	@Prompt(name = "Perform Act", description = "Executes the specified act based on the provided name parameter.")
	public String actPrompts(@Param(name = "name", description = "The name of the Act to perform.") String actName,
			@Param(name = "project_dir", description = "The project dir.") File projectDir,
			@Param(name = "gw_model", description = "The LLM model.", defaultValue = "") String model,
			@Param(name = "gw_acts", description = "The acts location folder. Default: `acts`", defaultValue = "acts") String acts) {
		return "Perform the act `${name}` for project_dir: `${project_dir}` by `perform_act` funtion tool. Define the act perform properties: \n"
				+ "- gw.model: `${gw_model}`\n"
				+ "- gw.acts: `${gw_acts}`\n";
	}
	
	@Tool(description = "Test method.")
	public String test_method(@Param(description = "The list param.") List<String> list) {
		return null;
	}
}