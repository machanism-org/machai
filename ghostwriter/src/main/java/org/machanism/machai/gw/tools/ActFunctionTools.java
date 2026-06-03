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
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provides functional tools for managing and executing "Act" templates within the Ghostwriter framework.
 * <p>
 * Act templates are reusable prompt definitions stored as TOML files, which define instructions and input templates
 * for common workflows. This class enables listing available acts, loading act details, and performing act operations
 * by integrating with the {@link Genai} provider.
 * </p>
 * <p>
 * Tools are registered via {@link #applyTools(Genai)} and can be configured at runtime using {@link #setConfigurator(Configurator)}.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class ActFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(ActFunctionTools.class);

	private static final String TOML_EXTENSION = ".toml";

	private Configurator configurator;

	/**
	 * Registers Act-related functional tools with the specified Genai provider.
	 * <p>
	 * The following tools are registered:
	 * <ul>
	 *   <li><b>build_in_list_acts</b>: Lists all available Act templates.</li>
	 *   <li><b>load_act_details</b>: Loads details of a specific Act template.</li>
	 *   <li><b>perform_act</b>: Performs a specified Act workflow.</li>
	 * </ul>
	 * </p>
	 *
	 * @param provider the Genai provider to register tools with
	 */
	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"build_in_list_acts",
				"Retrieves a list of all available Act templates that can be used with Ghostwriter. Acts are reusable "
						+ "prompt templates stored as TOML files, which define instructions and input templates for "
						+ "common workflows.",
				this::getActList);

		provider.addTool(
				"load_act_details",
				"Loads the details of a specific Act template, including its instructions, input template, and "
						+ "configuration options. Useful for inspecting or editing Act definitions.",
				this::getActDetails,
				"actName:string:required:The name of the Act to load.",
				"custom:boolean:optional:If true, retrieves the Act definition only from the user-defined (custom) "
						+ "acts directory. If false, retrieves only the built-in act. If not specified, retrieves "
						+ "effective user-defined acts.");

		provider.addTool(
				"perform_act",
				"Performs the specified Act by name. Use this tool to trigger a predefined action or workflow identified by the given Act name.",
				this::performAct,
				"actName:string:required:The name of the Act to perform.",
				"properties:string:optional:Act properties, specified as NAME=VALUE pairs separated by newline (\\n).");
	}

	/**
	 * Lists all available Act TOML files in the specified directory or built-in directory.
	 *
	 * @param params     JSON node containing parameters (not used in this implementation)
	 * @param workingDir the working directory (not used in this implementation)
	 * @return           a formatted string listing all available Act templates
	 * @throws IOException if an I/O error occurs during act discovery
	 */
	public Object getActList(JsonNode params, File workingDir) throws IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Get act list.");
		}

		List<String> result = getBaseActList().stream().map(line -> "- `" + line).collect(Collectors.toList());
		return StringUtils.join(result, Genai.LINE_SEPARATOR);
	}

	/**
	 * Retrieves the set of available Act template names from the classpath or JAR file.
	 *
	 * @return           a set of Act template names with descriptions
	 * @throws IOException if an I/O error occurs during act discovery
	 */
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
				jarFile.stream().forEach(entry -> addActDescription(result, entry.getName()));
			}
		}

		return result;
	}

	/**
	 * Adds the description of an Act template to the result set if the entry name matches an Act TOML file.
	 *
	 * @param result    the set to add the Act description to
	 * @param entryName the name of the entry in the JAR or directory
	 */
	private void addActDescription(Set<String> result, String entryName) {
		String actName = StringUtils.substringBetween(entryName, "acts/", TOML_EXTENSION);
		if (actName == null) {
			return;
		}

		Map<String, Object> properties = new HashMap<>();
		try {
			ActProcessor.tryLoadActFromClasspath(properties, actName);
			result.add("`" + actName + "`: " + Objects.toString(properties.get("description")));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Loads the details of a specific Act template, including instructions, input template, and configuration options.
	 *
	 * @param props      JSON node containing act configuration, including "actName" and optional "custom" flag
	 * @param workingDir the directory in which to load the Act details
	 * @return           a map of Act properties or an error message if loading fails
	 * @throws IOException if an I/O error occurs during act loading
	 */
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
	 * Performs an act operation based on the provided properties and working directory.
	 * <p>
	 * This method configures act execution using the supplied JSON properties and a working directory.
	 * It logs the operation, parses environment properties, sets up the act processor, and scans documents
	 * in the specified directory. The act name and other configuration parameters are extracted from the
	 * {@code props} JSON node.
	 * </p>
	 *
	 * <p>
	 * <b>Properties in {@code props}:</b>
	 * <ul>
	 *   <li><b>actName</b> (required): The name of the act to perform.</li>
	 *   <li><b>properties</b> (optional): A string containing environment-style key-value pairs (e.g., {@code "KEY1=VALUE1;KEY2=VALUE2"}).
	 *     <ul>
	 *       <li><b>model</b> ({@code GWConstants.MODEL_PROP_NAME}): The model to use for act processing. If not specified, defaults to {@code null}.</li>
	 *       <li><b>scanDir</b> ({@code GWConstants.SCAN_DIR_PROP_NAME}): The directory to scan for documents. If not specified, defaults to the provided {@code workingDir} path.</li>
	 *       <li><b>actsLocation</b> ({@code GWConstants.ACTS_LOCATION_PROP_NAME}): The location of act definitions. If not specified, defaults to {@code null}.</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * <b>Example:</b>
	 * <pre>
	 * JsonNode props = ...; // JSON with "actName" and optional "properties"
	 * File workingDir = new File("/path/to/dir");
	 * Object result = performAct(props, workingDir);
	 * </pre>
	 * </p>
	 *
	 * @param props      JSON node containing act configuration, including "actName" and optional "properties" string
	 * @param workingDir the directory in which to perform the act operation
	 * @return           a result object indicating the outcome of the act operation (typically "success")
	 * @throws IOException if an I/O error occurs during document scanning or act processing
	 */
	public Object performAct(JsonNode props, File workingDir) throws IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Perform act: {}, {}", StringUtils.abbreviate(String.valueOf(props), 80)
					.replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""), workingDir);
		}

		PropertiesConfigurator configurator = new PropertiesConfigurator();

		String model = null;
		Map<String, String> properties;
		if (props.has("properties")) {
			String envStr = props.get("properties").asText();
			properties = CommandFunctionTools.parseEnv(envStr, configurator);
			properties.entrySet().stream().forEach(e -> configurator.set(e.getKey(), e.getValue()));
			model = properties.get(GWConstants.MODEL_PROP_NAME);
		}
		
	    if(model == null) {
	    	model = this.configurator.get(GWConstants.MODEL_PROP_NAME);
	    }

		if (configurator.get(GWConstants.SCAN_DIR_PROP_NAME, null) == null) {
			configurator.set(GWConstants.SCAN_DIR_PROP_NAME,
					this.configurator.get(GWConstants.SCAN_DIR_PROP_NAME, workingDir.getAbsolutePath()));
		}

		ActProcessor actProcessor = new ActProcessor(workingDir, configurator, model);
		String actsLocation = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
		actProcessor.setActsLocation(actsLocation);

		String actName = props.get("actName").asText();
		actProcessor.setAct(actName);

		String scanDir = configurator.get(GWConstants.SCAN_DIR_PROP_NAME, workingDir.getAbsolutePath());

		logger.info("{}", StringUtils.center("Act: " + actName + " ", 80, "-"));
		actProcessor.scanDocuments(workingDir, scanDir);

		return "success";
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