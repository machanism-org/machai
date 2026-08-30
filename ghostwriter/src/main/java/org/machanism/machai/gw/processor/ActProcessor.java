package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.gw.tools.EndTaskException;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*@guidance:
 * IMPORTANT: Create or Update the Javadoc for ActProcessor class.
 * Class javadoc description should describe supported functionality and provide examples to use it.
 * If the method used as Javadoc documentation is not public or protected, the method name should not be specified.
 * Functionality:
 *  - describe supported special markers, see javadoc for following constants:
 *  	- SUPER_VALUE_PLACEHOLDER
 *  	- PUBLIC_USER_PROMPT_PROP_NAME
 *  	- ACT_DEFAULT_PROPS_SECTION_NAME
 *  	- STOP_SYMBOL
 *  	- SEPARATOR_CHARS
 *  	- EPISODE_DELIMETER
 *  	- ACTS_BASENAME_PREFIX
 *  	- TOML_EXTENSION
 *  	- BASED_ON_PROPERTY_NAME
 *  	- HTTP_PREFIX, HTTPS_PREFIX
 *  	- DEFAULT_TASK_MARKER
 */

/**
 * Processes named action definitions (“acts”) and executes their prompts
 * against a project, a project directory, or matching files by delegating the
 * actual AI interaction to {@link AIFileProcessor}.
 * <p>
 * An act is loaded from a TOML definition. Definitions may be bundled on the
 * classpath under {@value #ACTS_BASENAME_PREFIX}, provided from a configured
 * local/remote act location, or referenced directly as an explicit
 * {@value #TOML_EXTENSION} file. Built-in and custom definitions can be merged,
 * and definitions can inherit another definition through the
 * {@value #BASED_ON_PROPERTY_NAME} property. Inherited string and prompt-list
 * values may use {@value #SUPER_VALUE_PLACEHOLDER} to splice the parent value
 * into the overriding value.
 * </p>
 * <p>
 * Supported command and configuration markers include:
 * </p>
 * <ul>
 * <li>{@value #DEFAULT_TASK_MARKER} &mdash; shorthand prefix for an ad-hoc
 * {@code task} act command.</li>
 * <li>{@value #PUBLIC_USER_PROMPT_PROP_NAME} &mdash; property containing the
 * user prompt visible to act templates.</li>
 * <li>{@value #ACT_DEFAULT_PROPS_SECTION_NAME} &mdash; TOML section prefix for
 * default property values that are applied when no explicit value exists.</li>
 * <li>{@value #EPISODE_DELIMETER} &mdash; delimiter appended to an act name to
 * select one or more episodes.</li>
 * <li>{@value #SEPARATOR_CHARS} &mdash; separator for multiple selected episode
 * numbers.</li>
 * <li>{@value #STOP_SYMBOL} &mdash; suffix for an episode selection that
 * prevents subsequent normal-order episode execution.</li>
 * <li>{@value #ACTS_BASENAME_PREFIX} and {@value #TOML_EXTENSION} &mdash;
 * classpath location prefix and file extension used for built-in act
 * definitions.</li>
 * <li>{@value #BASED_ON_PROPERTY_NAME} &mdash; property name used to declare
 * act inheritance.</li>
 * <li>{@value #HTTP_PREFIX} and {@value #HTTPS_PREFIX} &mdash; the supported
 * remote act-location prefixes; non-URL locations are resolved from the project
 * root.</li>
 * </ul>
 * <h2>Examples</h2>
 * 
 * <pre>{@code
 * ActProcessor processor = new ActProcessor(projectDir, "openai:gpt-4o", configurator);
 * processor.setAct("help");
 * processor.process(projectLayout);
 *
 * // Run an ad-hoc task using the shorthand marker.
 * processor.setAct("> summarize the project structure");
 *
 * // Run only episodes 1 and 3 of an act, then stop without continuing normally.
 * processor.setAct("review#1,3! Check concurrency and error handling");
 *
 * // Use external TOML acts from a local directory or HTTPS location.
 * processor.setActsLocation("acts");
 * processor.setAct("custom-review");
 * }</pre>
 */
public class ActProcessor extends AIFileProcessor {

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(ActProcessor.class);

	private static final String TOOL_AUTO_SEARCH_NAME = "auto";

	/** Resource bundle supplying prompt templates for generators. */
	final ResourceBundle actBundle = ResourceBundle.getBundle("act-bundle");

	private static final String ACT_EXECUTION_INFORMATION_PREFIX = "The current act execution information: ";

	/**
	 * Shorthand command prefix indicating that the raw prompt should be interpreted
	 * and executed directly as a standard, ad-hoc agent task command.
	 * <p>
	 * When the input command begins with this marker, the runtime automatically
	 * expands the shorthand into a fully-qualified task command (e.g.,
	 * {@code > run build} is processed as {@code task run build}).
	 * </p>
	 */
	public static final String DEFAULT_TASK_MARKER = ">";

	/**
	 * Placeholder string used in inherited act definitions to reference and include
	 * the parent's value.
	 */
	public static final String SUPER_VALUE_PLACEHOLDER = "${super.value}";

	/**
	 * Property name representing the user prompt configured publicly inside the
	 * properties.
	 */
	public static final String PUBLIC_USER_PROMPT_PROP_NAME = "public.prompt";

	/**
	 * Prefix section designating default fallback values inside the loaded
	 * configurations.
	 */
	public static final String ACT_DEFAULT_PROPS_SECTION_NAME = "default";

	/**
	 * Character symbol that triggers immediate termination and disables normal
	 * order progression.
	 */
	public static final String STOP_SYMBOL = "!";

	/**
	 * Separator character used to delimit collection values like lists of files or
	 * episode indices.
	 */
	public static final String SEPARATOR_CHARS = ",";

	/**
	 * Divider symbol linking the base act name to an optional explicit subset of
	 * episodes.
	 */
	public static final String EPISODE_DELIMETER = "#";

	/** Classpath base directory for built-in act definitions. */
	public static final String ACTS_BASENAME_PREFIX = "/acts/";

	/**
	 * Expected file extension for configurations parsed as TOML files.
	 */
	public static final String TOML_EXTENSION = ".toml";

	/**
	 * Key used to denote inheritance by naming the base configuration to extend.
	 */
	public static final String BASED_ON_PROPERTY_NAME = "basedOn";

	/**
	 * Pre-compiled regex pattern to identify the first whitespace character in
	 * arguments.
	 */
	private static final Pattern FIRST_WHITESPACE = Pattern.compile("\\s");

	/**
	 * Protocol prefix for standard unsecured HTTP endpoints.
	 */
	private static final String HTTP_PREFIX = "http://";

	/**
	 * Protocol prefix for secured HTTPS endpoints.
	 */
	private static final String HTTPS_PREFIX = "https://";

	/** Optional directory containing external {@code *.toml} act files. */
	private String actsLocation;

	/** The episodes container managed by this processor. */
	private final Episodes episodes;

	/**
	 * Whether normal sequential execution should be skipped after explicit episode
	 * processing.
	 */
	private boolean disableNormalOrder;

	/** List of collected outputs generated during processing. */
	private List<String> results = new ArrayList<>();

	/**
	 * Map holding the accumulated act configuration properties loaded for
	 * execution.
	 */
	private Map<String, Object> actProperties = new HashMap<>();

	/** Cached automatically selected tools, keyed by act name and episode ID. */
	private Map<String, String[]> autoToolsMap = new ConcurrentHashMap<>();

	/**
	 * TOML property name containing the instructions supplied to the AI provider
	 * for an act.
	 */
	public static final String INSTRUCTIONS_PROPERTY_NAME = "instructions";

	/** TOML property name containing prompt inputs/episodes. */
	public static final String INPUTS_PROPERTY_NAME = "inputs";

	/**
	 * Creates an act processor.
	 *
	 * @param projectDir   root directory used as a base for relative paths
	 * @param genai        provider key/name (including model)
	 * @param configurator configuration source
	 */
	public ActProcessor(File projectDir, String genai, Configurator configurator) {
		super(projectDir, configurator, genai);
		episodes = new Episodes(this);
		actsLocation = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
	}

	/**
	 * Configures and initializes the current execution Action (Act) context by
	 * parsing the raw command string.
	 * <p>
	 * This method orchestrates the early stage lifecycle of an action. It handles:
	 * </p>
	 * <ol>
	 * <li><b>Task Shorthand Expansion:</b> Converting shortcut inputs (beginning
	 * with {@link #DEFAULT_TASK_MARKER}) into standard task instructions.</li>
	 * <li><b>Fallback Fallback Defaults:</b> Defaulting blank actions to
	 * {@code "help"}.</li>
	 * <li><b>Token/Argument Extraction:</b> Parsing the action name (first
	 * contiguous word) and separating it from any trailing, inline text
	 * prompt.</li>
	 * <li><b>Episode Slicing:</b> Extracting targeted sub-episode qualifiers
	 * appended via the {@code EPISODE_DELIMETER} (e.g., {@code my-act#2}).</li>
	 * <li><b>Property Binding:</b> Loading the action files, applying schema
	 * defaults, binding prompt argument placeholders, and configuring the target
	 * LLM runner model if overridden.</li>
	 * </ol>
	 *
	 * <h3>Example Parse Formats</h3>
	 * <ul>
	 * <li>{@code "> build-docs"} expands to {@code "task build-docs"}</li>
	 * <li>{@code "bindex/java/mvn-project"} runs the full 'bindex/java/mvn-project'
	 * action using the default prompt</li>
	 * <li>{@code "bindex/java/mvn-project#2"} runs only the 2nd episode of the
	 * 'bindex/java/mvn-project' action</li>
	 * <li>{@code "bindex/java/mvn-project -Dkey=val"} runs
	 * 'bindex/java/mvn-project' and extracts the arguments into
	 * {@code actProperties}</li>
	 * </ul>
	 *
	 * @param act the raw command or action string to parse and execute (e.g.,
	 *            {@code "task run"}, {@code ">add javadoc"},
	 *            {@code "bindex/java/mvn-project#2! use -DskipTests=true"})
	 * @throws IOException if an error occurs while loading the action definitions
	 *                     from the target storage location
	 */
	public void setAct(String act) throws IOException {
		act = StringUtils.trim(act);
		if (Strings.CS.startsWith(act, DEFAULT_TASK_MARKER)) {
			act = "task " + StringUtils.substringAfter(act, DEFAULT_TASK_MARKER);
		}
		act = StringUtils.defaultIfBlank(act, "help");

		String defaultPrompt = getDefaultPrompt();
		String prompt;
		Matcher matcher = FIRST_WHITESPACE.matcher(act);
		String name;
		if (matcher.find()) {
			int start = matcher.start();
			String substringAfter = StringUtils.substring(act, start);
			prompt = StringUtils.defaultIfBlank(substringAfter, StringUtils.defaultString(defaultPrompt));
			name = StringUtils.substring(act, 0, start);
		} else {
			name = act;
			prompt = defaultPrompt;
		}

		String episodeSelection = StringUtils.substringAfterLast(name, EPISODE_DELIMETER);
		name = StringUtils.substringBeforeLast(name, EPISODE_DELIMETER);

		episodes.setName(name);

		loadAct(name, actProperties, actsLocation, getRootDir());

		prompt = StringUtils.trim(prompt);
		applyDefaultValues(actProperties);
		applyPromptValues(prompt, actProperties);

		applyActData(actProperties);
		applyEpisodeSelection(episodeSelection);

		String model = (String) actProperties.get(GWConstants.MODEL_PROP_NAME);
		if (model != null) {
			setModel(model);
		}
	}

	/**
	 * Populates default properties from the act data, applying configurations and
	 * falling back to active configurator values when required.
	 *
	 * @param actData the act data map containing raw values
	 */
	private void applyDefaultValues(Map<String, Object> actData) {
		Set<Entry<String, Object>> entrySet = actData.entrySet();
		Map<String, Object> defaultValues = new HashMap<>();
		for (Entry<String, Object> entry : entrySet) {
			String key = entry.getKey();

			if (Strings.CS.startsWith(key, ACT_DEFAULT_PROPS_SECTION_NAME + ".")) {
				Object value = entry.getValue();
				key = StringUtils.substringAfter(key, ACT_DEFAULT_PROPS_SECTION_NAME + ".");
				if (!GWConstants.MODEL_PROP_NAME.equals(key) || getModel() == null) {
					if (!actData.containsKey(key)) {
						Object confValue = getConfigurator().get(key, null);
						if (confValue != null) {
							value = confValue;
						}

						defaultValues.put(key, value);
						getConfigurator().set(key, String.valueOf(value));
					}
				} else {
					getConfigurator().set(key, getModel());
				}
			}
		}

		actData.putAll(defaultValues);
	}

	/**
	 * Configures user prompt metadata, falling back to act-specified defaults if
	 * empty.
	 *
	 * @param prompt  the raw prompt to apply
	 * @param actData target act properties map
	 */
	private void applyPromptValues(String prompt, Map<String, Object> actData) {
		if (!actData.containsKey(PUBLIC_USER_PROMPT_PROP_NAME)) {
			if (prompt == null) {
				prompt = (String) actData.get("default." + PUBLIC_USER_PROMPT_PROP_NAME);
			}

			actData.put(PUBLIC_USER_PROMPT_PROP_NAME, Objects.toString(prompt, ""));
		}
	}

	/**
	 * Parses and registers specified episode boundaries from an argument string.
	 *
	 * @param episodeSelection boundary definitions containing index selectors and
	 *                         flags
	 */
	private void applyEpisodeSelection(String episodeSelection) {
		if (StringUtils.isBlank(episodeSelection)) {
			return;
		}

		String configuredEpisodeSelection = episodeSelection;
		if (Strings.CS.endsWith(configuredEpisodeSelection, STOP_SYMBOL)) {
			setDisableNormalOrder(true);
			configuredEpisodeSelection = StringUtils.substringBefore(configuredEpisodeSelection, STOP_SYMBOL);
		}

		List<Integer> selectedEpisodeIds = Arrays.stream(StringUtils.split(configuredEpisodeSelection, SEPARATOR_CHARS))
				.map(Integer::parseInt)
				.collect(Collectors.toList());
		episodes.setSelectedEpisodes(selectedEpisodeIds);
	}

	/**
	 * Enables or disables continuation with the default episode execution order.
	 *
	 * @param disableNormalOrder {@code true} to stop after requested episodes,
	 *                           {@code false} to continue with normal order
	 */
	public void setDisableNormalOrder(boolean disableNormalOrder) {
		this.disableNormalOrder = disableNormalOrder;
	}

	/**
	 * Loads an act definition into the provided map, supporting inheritance via the
	 * {@code basedOn} property.
	 * <p>
	 * This method attempts to load the specified act from both a user-defined
	 * directory (custom act) and the built-in classpath resources. If both are
	 * present, the custom act wraps (overrides) the built-in act, allowing for
	 * extension or modification of base act behavior.
	 * </p>
	 * <p>
	 * If the act specifies a {@code basedOn} property, the parent act is loaded
	 * first (recursively), and its properties are merged. The child act's
	 * properties then override or extend the parent.
	 * </p>
	 *
	 * @param name         the name of the act to load (without the {@code .toml}
	 *                     extension)
	 * @param properties   destination map to populate with parsed act properties
	 * @param actsLocation optional directory containing user-defined (custom) act
	 *                     files; may be {@code null}
	 * @param rootDir      project root used to resolve relative act locations
	 * @throws IOException              if reading act content fails
	 * @throws IllegalArgumentException if the specified act cannot be found in
	 *                                  either location
	 */
	public static void loadAct(String name, Map<String, Object> properties, String actsLocation, File rootDir)
			throws IOException {
		TomlParseResult customToml = null;
		try {
			customToml = tryLoadActFromDirectory(properties, name, actsLocation, rootDir);
		} catch (IOException e) {
			// User-defined act not found.
		}
		TomlParseResult toml = tryLoadActFromClasspath(properties, name);

		if (toml == null && customToml == null) {
			throw new IOException(
					"Act: `" + name + "`, " + GWConstants.ACTS_LOCATION_PROP_NAME + ": `" + actsLocation + "`.");
		}

		String basedOn = null;
		if (customToml != null) {
			basedOn = customToml.getString(BASED_ON_PROPERTY_NAME);
		}
		if (basedOn == null && toml != null) {
			basedOn = toml.getString(BASED_ON_PROPERTY_NAME);
		}

		if (basedOn != null) {
			loadAct(basedOn, properties, actsLocation, rootDir);
			properties.remove(BASED_ON_PROPERTY_NAME);
		}
	}

	/**
	 * Attempts to load an act definition from classpath resources.
	 *
	 * @param properties destination for parsed dotted properties
	 * @param name       act name (without {@code .toml})
	 * @return parsed TOML results, or {@code null} when the act is not found
	 * @throws IOException if the resource cannot be read
	 */
	public static TomlParseResult tryLoadActFromClasspath(Map<String, Object> properties, String name)
			throws IOException {
		String path = ACTS_BASENAME_PREFIX + name + TOML_EXTENSION;
		URL resource = GWConstants.class.getResource(path);
		if (resource == null) {
			return null;
		}

		String tomlStr = IOUtils.toString(resource, StandardCharsets.UTF_8);
		TomlParseResult toml = Toml.parse(tomlStr);
		setActData(properties, toml);

		if (logger.isDebugEnabled()) {
			logger.debug("Load act: `{}` from classpath.", name);
		}

		return toml;
	}

	/**
	 * Attempts to load an act definition from a user-defined directory.
	 *
	 * @param properties   destination for parsed dotted properties
	 * @param name         act name (without {@code .toml})
	 * @param actsLocation directory containing {@code *.toml} act files (may be
	 *                     {@code null})
	 * @param rootDir      project root used to resolve relative act locations
	 * @return parsed TOML results, or {@code null} when not found
	 * @throws IOException if the file cannot be read
	 */
	public static TomlParseResult tryLoadActFromDirectory(Map<String, Object> properties, String name,
			String actsLocation, File rootDir) throws IOException {

		TomlParseResult toml = null;
		if (isAbsolute(name)) {
			toml = loadActToml(name);
		} else if (actsLocation != null) {
			String absolutePath = getAbsolutePath(name, actsLocation, rootDir);
			toml = loadActToml(absolutePath);
		}

		if (toml != null) {
			setActData(properties, toml);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded act: `{}` from directory.", name);
			}
		}

		return toml;
	}

	/**
	 * Resolves an act file path or URL from an act name and configured act source.
	 *
	 * @param name         act name or file path
	 * @param actsLocation base directory or URL for act definitions
	 * @param rootDir      project root used to resolve relative act locations
	 * @return absolute file path or URL string
	 * @throws IOException if an explicitly referenced local act file does not exist
	 */
	private static String getAbsolutePath(String name, String actsLocation, File rootDir) throws IOException {
		String path = null;
		if (!Strings.CS.startsWithAny(actsLocation, HTTP_PREFIX, HTTPS_PREFIX)) {
			File file = new File(name);
			if (!file.isAbsolute()) {
				Path actsPath = rootDir.toPath().resolve(actsLocation);
				file = actsPath.resolve(name + TOML_EXTENSION).toFile();
			} else {
				if (!file.exists()) {
					throw new IOException("The act not found: " + name);
				}
			}
			path = file.getAbsolutePath();

		} else {
			String base = actsLocation.endsWith("/") ? actsLocation : actsLocation + "/";
			String uriString = Strings.CS.endsWith(name, TOML_EXTENSION) ? name : base + name + TOML_EXTENSION;
			path = URI.create(uriString).toURL().toString();
		}

		return path;
	}

	/**
	 * Loads and parses an act TOML document from a local file or remote URL.
	 *
	 * @param name absolute file path or URL to the TOML resource
	 * @return parsed TOML results, or {@code null} if a local file path does not
	 *         exist
	 * @throws IOException if reading the TOML resource fails
	 */
	private static TomlParseResult loadActToml(String name) throws IOException {
		TomlParseResult toml = null;
		if (!Strings.CS.startsWithAny(name, HTTP_PREFIX, HTTPS_PREFIX)) {
			File file = new File(name);
			if (file.exists()) {
				toml = Toml.parse(file.toPath());
			}
		} else {
			URI uri = URI.create(name);
			toml = Toml.parse(uri.toURL().openStream());
		}
		return toml;
	}

	/**
	 * Determines whether the supplied act reference should be treated as an
	 * explicit TOML path.
	 *
	 * @param name act reference to inspect
	 * @return {@code true} if the reference already ends with {@code .toml}
	 */
	private static boolean isAbsolute(String name) {
		return Strings.CS.endsWith(name, TOML_EXTENSION);
	}

	/**
	 * Copies dotted-string keys from the TOML parse results into
	 * {@code properties}.
	 * <p>
	 * If a key already exists in {@code properties}, the new value is formatted
	 * into the old value using {@link String#format(String, Object...)}.
	 * </p>
	 *
	 * @param properties properties destination
	 * @param toml       TOML parse results
	 */
	static void setActData(Map<String, Object> properties, TomlParseResult toml) {
		Set<Entry<String, Object>> props = toml.dottedEntrySet();
		for (Entry<String, Object> entry : props) {
			setActDataEntry(properties, entry);
		}
	}

	/**
	 * Applies a single TOML entry to the merged act property map.
	 *
	 * @param properties destination property map
	 * @param entry      TOML entry to process
	 */
	private static void setActDataEntry(Map<String, Object> properties, Entry<String, Object> entry) {
		String key = entry.getKey();
		Object value = entry.getValue();
		if (value instanceof String) {
			putStringActData(properties, key, (String) value);
		} else if (value instanceof Boolean) {
			properties.put(key, Boolean.toString((Boolean) value));
		} else if (value instanceof Integer) {
			properties.put(key, Integer.toString((Integer) value));
		} else if (value instanceof Double) {
			properties.put(key, Double.toString((Double) value));
		} else if (value instanceof TomlArray) {
			List<String> result = mergeTomlArrayValues(properties.get(key), ((TomlArray) value).toList(), key);
			properties.put(key, result);
		}
	}

	/**
	 * Stores a string property, merging it with any inherited value already
	 * present.
	 *
	 * @param properties destination property map
	 * @param key        property name
	 * @param value      property value from the current act
	 */
	@SuppressWarnings("unchecked")
	private static void putStringActData(Map<String, Object> properties, String key, String value) {
		Object mainValue = properties.get(key);

		if (INPUTS_PROPERTY_NAME.equals(key) && mainValue != null) {
			value = removeFrontMatterData(value);
		}

		if (mainValue instanceof String) {
			String replace = Strings.CS.replace((String) mainValue, SUPER_VALUE_PLACEHOLDER,
					Objects.toString(value, SUPER_VALUE_PLACEHOLDER));
			properties.put(key, replace);
		} else if (mainValue instanceof List) {
			properties.put(key, mergeStringWithListValue((List<String>) mainValue, value, key));
		} else {
			properties.put(key, value);
		}
	}

	/**
	 * Merges a single string value into each string item of an inherited list.
	 *
	 * @param mainValueList inherited list value
	 * @param value         string value to merge through
	 *                      {@link #SUPER_VALUE_PLACEHOLDER}
	 * @param key           property name whose values are being merged
	 * @return merged list results
	 */
	private static List<String> mergeStringWithListValue(List<String> mainValueList, String value, String key) {

		List<String> result = new ArrayList<>();
		for (String mainValueItem : mainValueList) {
			if (mainValueItem.isEmpty()) {
				result.add(mainValueItem);
			} else {
				if (INPUTS_PROPERTY_NAME.equals(key) && mainValueItem != null) {
					value = removeFrontMatterData(value);
				}

				String replace = Strings.CS.replace(mainValueItem, SUPER_VALUE_PLACEHOLDER,
						Objects.toString(value, SUPER_VALUE_PLACEHOLDER));
				result.add(replace);
			}
		}
		return result;
	}

	/**
	 * Merges TOML array values with any existing inherited string or list value.
	 *
	 * @param existingValue existing property value, if any
	 * @param values        TOML array values from the current act
	 * @param key           property name whose values are being merged
	 * @return merged string list
	 */
	private static List<String> mergeTomlArrayValues(Object existingValue, List<Object> values, String key) {
		List<String> result = new ArrayList<>();

		List<String> mainValues = toStringList(existingValue);
		int maxSize = Math.max(values.size(), mainValues.size());
		for (int i = 0; i < maxSize; i++) {
			String mainValue = i < mainValues.size() ? (String) mainValues.get(i) : null;
			String value = i < values.size() ? (String) values.get(i) : null;

			if (INPUTS_PROPERTY_NAME.equals(key) && mainValue != null) {
				value = removeFrontMatterData(value);
			}

			result.add(resolveMergedValue(mainValues, i, value));
		}
		return result;
	}

	/**
	 * Converts an inherited property value to a list of strings.
	 *
	 * @param existingValue existing property value
	 * @return list representation of the value, or an empty list if unsupported
	 */
	@SuppressWarnings("unchecked")
	private static List<String> toStringList(Object existingValue) {
		if (existingValue instanceof String) {
			return Arrays.asList((String) existingValue);
		}
		if (existingValue instanceof List) {
			return (List<String>) existingValue;
		}
		return new ArrayList<>();
	}

	/**
	 * Resolves a merged value for an inherited prompt slot.
	 *
	 * @param mainValues inherited values
	 * @param index      current position
	 * @param value      overriding value for the position
	 * @return merged value for the position
	 */
	private static String resolveMergedValue(List<String> mainValues, int index, String value) {
		if (index >= mainValues.size()) {
			return value;
		}
		String mainValue = mainValues.get(index);
		return Strings.CS.replace(mainValue, SUPER_VALUE_PLACEHOLDER, Objects.toString(value, SUPER_VALUE_PLACEHOLDER));
	}

	/**
	 * Applies loaded act data to this processor's configuration and runtime
	 * settings.
	 *
	 * @param properties properties loaded from TOML acts
	 */
	@SuppressWarnings("unchecked")
	void applyActData(Map<String, Object> properties) {
		for (Entry<String, Object> entry : properties.entrySet()) {
			String key = entry.getKey();
			Object valueObj = entry.getValue();
			if (valueObj instanceof String) {
				applyStringActData(key, (String) valueObj);
			} else if (valueObj instanceof List && INPUTS_PROPERTY_NAME.equals(key)) {
				episodes.setEpisodes(resolvePromptValues((List<String>) valueObj));
			}
		}
		Object prompts = properties.get(INPUTS_PROPERTY_NAME);
		if (prompts instanceof String) {
			setDefaultPrompt((String) prompts);
		} else if (prompts instanceof List && !((List<String>) prompts).isEmpty()) {
			setDefaultPrompt(((List<String>) prompts).get(0));
		}
	}

	/**
	 * Applies a single string property to processor state or configuration.
	 *
	 * @param key      property name
	 * @param valueObj property value as an Object
	 */
	private void applyStringActData(String key, String valueObj) {
		String value = resolveInheritedValue(key, valueObj);
		applyStringProperty(key, value);
	}

	/**
	 * Resolves a property value against the current configurator for inheritance.
	 *
	 * @param key   property name
	 * @param value act-defined value that may contain
	 *              {@link #SUPER_VALUE_PLACEHOLDER}
	 * @return resolved property value
	 */
	private String resolveInheritedValue(String key, String value) {
		String inheritValue = getConfigurator().get(key, null);
		if (inheritValue != null) {
			return Strings.CS.replace(value, SUPER_VALUE_PLACEHOLDER, StringUtils.defaultString(inheritValue));
		}
		return value;
	}

	/**
	 * Applies a resolved string property by dispatching to the matching processor
	 * setting.
	 *
	 * @param key   property name
	 * @param value resolved property value
	 */
	private void applyStringProperty(String key, String value) {
		switch (key) {
		case INSTRUCTIONS_PROPERTY_NAME:
			if (super.getInstructions() == null) {
				super.setInstructions(value);
			}
			break;
		case INPUTS_PROPERTY_NAME:
			episodes.setEpisodes(Collections.singletonList(value));
			break;
		case GWConstants.THREADS_PROP_NAME:
			super.setThreads(Integer.parseInt(value));
			break;
		case GWConstants.EXCLUDES_PROP_NAME:
			super.setExcludes(StringUtils.split(value, SEPARATOR_CHARS));
			break;
		case GWConstants.NONRECURSIVE_PROP_NAME:
			super.setNonRecursive(Boolean.parseBoolean(value));
			break;
		case GWConstants.INTERACTIVE_MODE_PROP_NAME:
			boolean interactive = Boolean.parseBoolean(value);
			logger.info("Interactive mode: {}", interactive);
			super.setInteractive(interactive);
			break;
		case GWConstants.MODEL_PROP_NAME:
			String model = super.getModel();
			getConfigurator().set(key, model);
			if (model == null) {
				super.setModel(value);
			}
			break;
		default:
			getConfigurator().set(key, value);
			break;
		}
	}

	/**
	 * Resolves inherited placeholders for each prompt episode.
	 *
	 * @param promptValues prompt values to resolve
	 * @return resolved prompt list
	 */
	private List<String> resolvePromptValues(List<String> promptValues) {
		List<String> updateValue = new ArrayList<>();
		for (String value : promptValues) {
			updateValue.add(resolveInheritedValue(INPUTS_PROPERTY_NAME, value));
		}
		return updateValue;
	}

	/**
	 * Sets the location used for loading external act definition files
	 * ({@code *.toml}).
	 * <p>
	 * The location may be specified as:
	 * <ul>
	 * <li><b>An absolute path</b> — used as-is (e.g., {@code /opt/gw/acts}).</li>
	 * <li><b>A relative path</b> — resolved against the {@linkplain #getRootDir()
	 * root directory}.</li>
	 * <li><b>A URL</b> — any value starting with {@code http://} or
	 * {@code https://}, in which case acts are loaded remotely and no local
	 * directory validation is performed.</li>
	 * </ul>
	 * For path-based (non-URL) locations, the resolved directory must already
	 * exist; otherwise an exception is thrown.
	 * <p>
	 * A {@code null} value is ignored and leaves the current setting unchanged.
	 *
	 * @param actsLocation absolute path, relative path, or URL pointing to the
	 *                     directory (or remote source) containing act files;
	 *                     {@code null} to leave the current value unchanged
	 * @throws IllegalArgumentException if {@code actsLocation} is a non-URL path
	 *                                  that does not resolve to an existing
	 *                                  directory
	 */
	public void setActsLocation(String actsLocation) {
		if (actsLocation != null) {
			if (!Strings.CS.startsWithAny(actsLocation, HTTP_PREFIX, HTTPS_PREFIX)) {
				File actDir = new File(actsLocation);
				if (!actDir.isAbsolute()) {
					actDir = new File(getRootDir(), actsLocation);
				}

				if (!actDir.exists() || !actDir.isDirectory()) {
					throw new IllegalArgumentException(
							"Act directory does not exist or is not a directory: " + actDir.getAbsolutePath());
				}
				this.actsLocation = actDir.getAbsolutePath();
			} else {
				this.actsLocation = actsLocation;
			}
			getConfigurator().set(GWConstants.ACTS_LOCATION_PROP_NAME, this.actsLocation);
		}
	}

	/**
	 * Processes files and folders under the parent project directory (excluding
	 * modules).
	 *
	 * @param projectLayout active project layout metadata context to process
	 * @throws IOException if scanning or executing templates fails
	 */
	@Override
	protected void processParentFiles(ProjectLayout projectLayout) throws IOException {
		File projectDir = projectLayout.getProjectDir();
		List<File> children = listFiles(projectDir);

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child, projectDir));

		if (children.isEmpty()) {
			children.add(projectDir);
		}

		for (File file : children) {
			try {
				try {
					boolean match = match(file, projectDir);
					int requestedEpisodeId = 1;
					if (match && getDefaultPrompt() != null) {
						if (!episodes.isRegularOrder()) {
							try {
								requestedEpisodeId = episodes.requestedOrder((i, episode) -> {
									return process(projectLayout, file, episode, i);
								});
								if (disableNormalOrder) {
									return;
								} else {
									requestedEpisodeId++;
								}
							} catch (MoveToEpisodeException e) {
								requestedEpisodeId = episodes.getEpisodeId(requestedEpisodeId, e);
							}
						}

						episodes.regularOrder(requestedEpisodeId, (i, episode) -> {
							return process(projectLayout, file, episode, i);
						});
					}
				} catch (EndTaskException e) {
					String perform = e.getMessage();
					addResults(perform);
					return;
				}

			} catch (EndTaskException e) {
				String perform = e.getMessage();
				addResults(perform);
				return;
			}
		}
	}

	/**
	 * Executes a single episode prompt after prepending act metadata.
	 *
	 * @param projectLayout active project layout
	 * @param projectDir    file or directory being processed
	 * @param prompt        episode prompt text
	 * @param episodeId     zero-based episode index
	 * @return provider results string, if any
	 */
	private String process(ProjectLayout projectLayout, File projectDir, String prompt, int episodeId) {
		Map<String, Object> actInformation = episodes.getActInformation(episodeId);
		String actInformationJson;
		try {
			actInformationJson = ACT_EXECUTION_INFORMATION_PREFIX
					+ new ObjectMapper().writeValueAsString(actInformation);
		} catch (MoveToEpisodeException e) {
			String process = e.getMessage();
			addResults(process);
			throw e;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}

		String actSysInstructions = actBundle.getString("act_sys_instructions");
		String process = super.process(projectLayout, projectDir, getInstructions(),
				getProcessInfo(projectLayout, projectDir), actInformationJson, actSysInstructions, prompt);
		return process;
	}

	/*
	 * @guidance: # Examples Act episode markdown front matter usage: ```markdown
	 * --- enabledTools: auto --- ``` or ```markdown --- enabledTools: auto: Don't
	 * use web access and system command tools. --- ```
	 */
	/**
	 * Applies the tools configured for an act episode, including automatic tool
	 * selection requested through the episode's YAML front matter.
	 * <p>
	 * Set {@code enabledTools: auto} to have a separate provider request select the
	 * applicable tools from the episode instructions and prompt. The selected tool
	 * names are cached for the act episode, then registered on the provider for the
	 * actual request. For example:
	 * </p>
	 *
	 * <pre>{@code
	 * ---
	 * enabledTools: auto
	 * ---
	 * Review this module and use only the tools needed for the task.
	 * }</pre>
	 *
	 * <p>
	 * A YAML mapping can give the automatic selector additional constraints. Its
	 * {@code auto} value is passed to the selector as a query; it guides selection
	 * rather than directly disabling tools. For example:
	 * </p>
	 *
	 * <pre>{@code
	 * ---
	 * enabledTools:
	 *   auto: Don't use web access and system command tools.
	 * ---
	 * Analyze the local implementation.
	 * }</pre>
	 *
	 * <p>
	 * Any other {@code enabledTools} value is delegated unchanged to the standard
	 * tool-registration behavior.
	 * </p>
	 *
	 * @param instructions resolved system instructions for the episode
	 * @param prompts      resolved prompt parts, including episode metadata
	 * @param provider     provider that receives the selected tools
	 * @param tools        configured tool names or automatic-selection marker
	 */
	@Override
	protected void applyTools(String instructions, String[] prompts, Genai provider, String[] tools) {
		if (tools != null && tools.length != 0 && isAutoToolSelection(tools[0])
				&& prompts.length > 1
				&& Strings.CS.startsWith(prompts[1], ACT_EXECUTION_INFORMATION_PREFIX)) {
			tools = getAutoTools(getAutoToolSelectionQuery(tools[0]), instructions, prompts);
		}
		super.applyTools(instructions, prompts, provider, tools);
	}

	/**
	 * Determines whether a front-matter tool value requests automatic selection.
	 *
	 * @param toolValue serialized scalar or YAML mapping value
	 * @return {@code true} when the value is {@code auto} or an {@code auto}
	 *         mapping
	 */
	private boolean isAutoToolSelection(String toolValue) {
		return TOOL_AUTO_SEARCH_NAME.equals(toolValue)
				|| Strings.CS.startsWith(toolValue, "{" + TOOL_AUTO_SEARCH_NAME + "=");
	}

	/**
	 * Extracts the optional query from SnakeYAML's serialized {@code auto} mapping
	 * value.
	 *
	 * @param toolValue serialized scalar or YAML mapping value
	 * @return query text, or an empty string for a plain {@code auto} marker
	 */
	private String getAutoToolSelectionQuery(String toolValue) {
		if (TOOL_AUTO_SEARCH_NAME.equals(toolValue)) {
			return "";
		}
		return StringUtils.substringBetween(toolValue, "{auto=", "}");
	}

	/**
	 * Selects and caches the tools required for the current act episode by asking
	 * the configured provider for a JSON tool list.
	 *
	 * @param instructions provider instructions; retained for the selection context
	 * @param query
	 * @param prompts      prompt parts containing act execution metadata and the
	 *                     episode prompt
	 * @return selected tool names, or {@code null} when selection fails
	 * @throws IllegalArgumentException if the provider returns malformed JSON
	 */
	@SuppressWarnings("unchecked")
	private String[] getAutoTools(String query, String instructions, String[] prompts) {
		String[] tools = null;
		try {
			String inputId = getInputId(prompts);

			tools = autoToolsMap.computeIfAbsent(inputId, key -> {
				try {
					Genai provider = GenaiProviderManager.getProvider(getModel(), getConfigurator());
					super.applyTools(null, null, provider, null);

					String toolSearch = actBundle.getString("tool_search");
					HashMap<String, String> varMap = new HashMap<>();
					varMap.put("instructions", instructions);
					varMap.put("prompt", prompts[2]);
					varMap.put("query", query);

					String prompt = StringSubstitutor.replace(toolSearch, varMap);
					provider.prompt(prompt);
					String perform = provider.perform();
					Map<String, Object> value = new ObjectMapper().readValue(perform, Map.class);
					Object enabledToolsValue = value.get("enabledTools");
					String[] enabledTools = null;
					if (enabledToolsValue != null) {
						enabledTools = (String[]) ((List<String>) enabledToolsValue).toArray(new String[0]);
						logger.info("Auto-tool selection successful for inputId [{}]: {} tool(s) selected -> [{}]",
								inputId, enabledTools.length, StringUtils.join(enabledTools, ", "));
					} else {
						logger.info(
								"Auto-tool selection for inputId [{}] returned no specific restrictions: falling back to all tools enabled.",
								inputId);
					}

					return enabledTools;

				} catch (Exception e) {
					logger.error("Automatic tool selection failed for inputId [{}]: [{}] {}",
							inputId, e.getClass().getSimpleName(), e.getMessage(), e);
					return null;
				}
			});

		} catch (JsonProcessingException e) {
			logger.error("Automatic tool selection failed: {}", e.getMessage());
		}
		return tools;
	}

	/**
	 * Builds the cache key for an act episode from the execution metadata in the
	 * prompt.
	 *
	 * @param prompts prompt parts containing serialized act execution metadata
	 * @return cache key composed of the act name and current episode ID
	 * @throws JsonProcessingException if the execution metadata is not valid JSON
	 */
	private String getInputId(String[] prompts) throws JsonProcessingException {
		String name = episodes.getName();
		String actInfoJson = StringUtils.substringAfter(prompts[1], ACT_EXECUTION_INFORMATION_PREFIX);
		@SuppressWarnings("unchecked")
		Map<String, Object> value = new ObjectMapper().readValue(actInfoJson, Map.class);
		int episodeId = (int) value.get("CURRENT_EPISODE_ID");
		name = name + "#" + episodeId;

		return name;
	}

	/**
	 * Executes the act against a single file.
	 *
	 * @param projectLayout project layout
	 * @param file          file to process
	 * @throws IOException if provider execution fails
	 */
	@Override
	protected void processFile(ProjectLayout projectLayout, File file) throws IOException {
		String process = process(projectLayout, file, getDefaultPrompt());
		if (logger.isInfoEnabled()) {
			logger.info("File processing result: {}", process);
		}
		addResults(process);
	}

	/**
	 * Appends a string result item to the execution list.
	 *
	 * @param result result message or payload to record
	 */
	public void addResults(String result) {
		this.results.add(result);
	}

	/**
	 * Returns the list of all collected outputs.
	 *
	 * @return the collected list of run outputs
	 */
	public List<String> getResults() {
		return results;
	}

	/**
	 * Returns the merged act properties currently loaded on this processor.
	 *
	 * @return the act properties map
	 */
	public Map<String, Object> getActProperties() {
		return actProperties;
	}

}
