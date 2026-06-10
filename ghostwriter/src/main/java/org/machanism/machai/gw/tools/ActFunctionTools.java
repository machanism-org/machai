package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.Function;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
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
	@Function(name = "build_in_list_acts", description = "Retrieves a list of all available Act templates that can be used with Ghostwriter. Acts are reusable "
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
	 * @param configurator 
	 */
	@Function(name = "load_act_details", description = "Loads the details of a specific Act template, including its instructions, input template, and "
			+ "configuration options. Useful for inspecting or editing Act definitions.")
	public Object getActDetails(@Param(name = "actName", description = "The name of the Act to load.") String actName,
			@Param(name = "custom", description = "If true, retrieves the Act definition only from the user-defined (custom) "
					+ "acts directory. If false, retrieves only the built-in act. If not specified, retrieves "
					+ "effective user-defined acts.", defaultValue = "false") boolean custom,
			@Param(name = "projectDir", description = "The project dir.") File projectDir, Configurator configurator) throws IOException {
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
	 * Performs an act operation based on the provided properties and working
	 * directory.
	 * <p>
	 * This method configures act execution using the supplied JSON properties and a
	 * working directory. It logs the operation, parses environment properties, sets
	 * up the act processor, and scans documents in the specified directory. The act
	 * name and other configuration parameters are extracted from the {@code props}
	 * JSON node.
	 * </p>
	 *
	 * <p>
	 * <b>Properties in {@code props}:</b>
	 * <ul>
	 * <li><b>actName</b> (required): The name of the act to perform.</li>
	 * <li><b>properties</b> (optional): A string containing environment-style
	 * key-value pairs (e.g., {@code "KEY1=VALUE1;KEY2=VALUE2"}).
	 * <ul>
	 * <li><b>model</b> ({@code GWConstants.MODEL_PROP_NAME}): The model to use for
	 * act processing. If not specified, defaults to {@code null}.</li>
	 * <li><b>paths</b> ({@code GWConstants.SCAN_DIR_PROP_NAME}): The directory to
	 * scan for documents. If not specified, defaults to the provided
	 * {@code projectDir} path.</li>
	 * <li><b>actsLocation</b> ({@code GWConstants.ACTS_LOCATION_PROP_NAME}): The
	 * location of act definitions. If not specified, defaults to {@code null}.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * JsonNode props = ...; // JSON with "actName" and optional "properties"
	 * File projectDir = new File("/path/to/dir");
	 * Object result = performAct(props, projectDir);
	 * </pre>
	 * </p>
	 * @param config 
	 */
	@Function(name = "perform_act", description = "Performs the specified Act by name. Use this tool to trigger a predefined action or workflow identified by the given Act name.")
	public Object performAct(@Param(name = "actName", description = "The name of the Act to perform.") String actName,
			@Param(name = "properties", description = "Act properties, specified as NAME=VALUE pairs separated by newline (\\n).", defaultValue = "") String envStr,
			@Param(name = "projectDir", description = "The project dir.") File projectDir, Configurator config)
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

		if (configurator.get(GWConstants.SCAN_DIR_PROP_NAME, null) == null) {
			configurator.set(GWConstants.SCAN_DIR_PROP_NAME,
					config.get(GWConstants.SCAN_DIR_PROP_NAME, projectDir.getAbsolutePath()));
		}

		ActProcessor actProcessor = new ActProcessor(projectDir, configurator, model);
		String actsLocation = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
		actProcessor.setActsLocation(actsLocation);

		actProcessor.setAct(actName);

		String paths = configurator.get(GWConstants.SCAN_DIR_PROP_NAME, projectDir.getAbsolutePath());

		logger.info("{}", StringUtils.center("Act: " + actName + " ", 80, "-"));
		actProcessor.scanDocuments(projectDir, paths);

		return "success";
	}

}