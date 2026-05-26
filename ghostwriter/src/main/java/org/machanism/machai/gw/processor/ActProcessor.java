package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

/**
 * Processor that runs Ghostwriter in "Act" mode.
 *
 * <p>
 * An <em>act</em> is episodes predefined prompt template stored as episodes
 * {@code .toml} file. Act files can be loaded from bundled classpath resources
 * (under {@code /acts}) and/or from episodes user-specified directory.
 * </p>
 *
 * <h2>Act format</h2>
 * <p>
 * Act files are parsed as TOML and support episodes small set of keys:
 * </p>
 * <ul>
 * <li>{@code instructions}: provider system instructions</li>
 * <li>{@code inputs}: episodes prompt template;
 * {@link String#format(String, Object...)} is used to inject user-provided
 * prompt text</li>
 * <li>{@code gw.threads}: enables module multi-threading</li>
 * <li>{@code gw.excludes}: comma-separated scan exclusions</li>
 * <li>{@code gw.nonRecursive}: disables module recursion</li>
 * <li>any other key is forwarded to the underlying configuration</li>
 * </ul>
 *
 * <h2>Execution</h2>
 * <p>
 * When executing an act, Ghostwriter will scan matching files and run the act's
 * composed prompt against each file. Acts may also declare episodes
 * {@code prologue} and/or {@code epilogue} list of related acts to run
 * before/after the main scan.
 * </p>
 */
public class ActProcessor extends AIFileProcessor {

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(ActProcessor.class);

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	private static final String STOP_SYMBOL = "!";

	private static final String SEPARATOR_CHARS = ",";

	private static final String EPISODE_DELIMETER = "#";

	/** Classpath base directory for built-in act definitions. */
	public static final String ACTS_BASENAME_PREFIX = "/acts/";

	private static final String TOML_EXTENSION = ".toml";

	private static final String BASED_ON_PROPERTY_NAME = "basedOn";
	private static final Pattern FIRST_WHITESPACE = Pattern.compile("\\s");

	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";

	/** Optional directory containing external {@code *.toml} act files. */
	private String actsLocation;

	private Episodes episodes = new Episodes();

	/**
	 * Whether normal sequential execution should be skipped after explicit episode
	 * processing.
	 */
	private boolean disableNormalOrder;

	/**
	 * Creates an act processor.
	 *
	 * @param projectDir   root directory used as a base for relative paths
	 * @param configurator configuration source
	 * @param genai        provider key/name (including model)
	 */
	public ActProcessor(File projectDir, Configurator configurator, String genai) {
		super(projectDir, configurator, genai);
		actsLocation = configurator.get(GWConstants.ACTS_LOCATION_PROP_NAME, null);
	}

	/**
	 * Loads an act definition and applies it as the current execution defaults.
	 *
	 * <p>
	 * The {@code act} argument supports the form {@code <name> [prompt]}, where the
	 * optional prompt portion is inserted into the act's {@code inputs} template.
	 * If no prompt is provided, {@link #getDefaultPrompt()} is used.
	 * </p>
	 *
	 * @param act act name plus optional prompt text
	 * @throws IOException
	 */
	public void setAct(String act) throws IOException {
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

		Map<String, Object> actData = new HashMap<>();
		episodes.setName(name);
		loadAct(name, actData, actsLocation);

		prompt = StringUtils.trim(prompt);
		applyPromptValues(prompt, actData);
		applyActData(actData);
		applyEpisodeSelection(episodeSelection);
	}

	@SuppressWarnings("unchecked")
	private void applyPromptValues(String prompt, Map<String, Object> actData) {
		Object mainValue = actData.get(GWConstants.INPUTS_PROPERTY_NAME);
		if (mainValue instanceof String) {
			actData.put(GWConstants.INPUTS_PROPERTY_NAME, applyPrompt(prompt, actData, (String) mainValue));
			return;
		}

		if (mainValue instanceof List) {
			List<String> inputs = new ArrayList<>();
			for (String value : (List<String>) mainValue) {
				inputs.add(applyPrompt(prompt, actData, value));
			}
			actData.put(GWConstants.INPUTS_PROPERTY_NAME, inputs);
		}
	}

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
	 * Replaces the act prompt placeholder in episodes template value.
	 *
	 * @param prompt    resolved prompt text
	 * @param actData   loaded act properties
	 * @param mainValue template string that may contain {@code %s}
	 * @return prompt-expanded template string
	 */
	private String applyPrompt(String prompt, Map<String, Object> actData, String mainValue) {
		String actPrompt = Objects.toString(actData.get("prompt"), "");
		return Strings.CS.replace(mainValue, "%s", Objects.toString(prompt, actPrompt));
	}

	/**
	 * Loads an act definition into the provided map, supporting inheritance via the
	 * {@code basedOn} property.
	 *
	 * <p>
	 * This method attempts to load the specified act from both episodes
	 * user-defined directory (custom act) and the built-in classpath resources. If
	 * both are present, the custom act wraps (overrides) the built-in act, allowing
	 * for extension or modification of base act behavior.
	 * </p>
	 *
	 * <p>
	 * If the act specifies episodes {@code basedOn} property, the parent act is
	 * loaded first (recursively), and its properties are merged. The child act's
	 * properties then override or extend the parent.
	 * </p>
	 *
	 * @param name         the name of the act to load (without the {@code .toml}
	 *                     extension)
	 * @param properties   destination map to populate with parsed act properties
	 * @param actsLocation optional directory containing user-defined (custom) act
	 *                     files; may be {@code null}
	 * @throws IOException              if reading act content fails
	 * @throws IllegalArgumentException if the specified act cannot be found in
	 *                                  either location
	 */
	public static void loadAct(String name, Map<String, Object> properties, String actsLocation) throws IOException {
		TomlParseResult customToml = null;
		try {
			customToml = tryLoadActFromDirectory(properties, name, actsLocation);
		} catch (IOException e) {
			// User-defined act not found.
		}
		TomlParseResult toml = tryLoadActFromClasspath(properties, name);

		if (toml == null && customToml == null) {
			throw new ActNotFound(name);
		}

		String basedOn = null;
		if (customToml != null) {
			basedOn = customToml.getString(BASED_ON_PROPERTY_NAME);
		}
		if (basedOn == null && toml != null) {
			basedOn = toml.getString(BASED_ON_PROPERTY_NAME);
		}

		if (basedOn != null) {
			loadAct(basedOn, properties, actsLocation);
			properties.remove(BASED_ON_PROPERTY_NAME);
		}
	}

	/**
	 * Attempts to load an act definition from classpath resources.
	 *
	 * @param properties destination for parsed dotted properties
	 * @param name       act name (without {@code .toml})
	 * @return parsed TOML result, or {@code null} when the act is not found
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
	 * Attempts to load an act definition from episodes user-defined directory.
	 *
	 * @param properties   destination for parsed dotted properties
	 * @param name         act name (without {@code .toml})
	 * @param actsLocation directory containing {@code *.toml} act files (may be
	 *                     {@code null})
	 * @return parsed TOML result, or {@code null} when not found
	 * @throws IOException if the file cannot be read
	 */
	public static TomlParseResult tryLoadActFromDirectory(Map<String, Object> properties, String name,
			String actsLocation) throws IOException {

		TomlParseResult toml = null;
		if (isAbsolute(name)) {
			toml = loadActToml(name);
		} else if (actsLocation != null) {
			String absolutePath = getAssolutePath(name, actsLocation);
			toml = loadActToml(absolutePath);
		}

		if (toml != null) {
			setActData(properties, toml);
			if (logger.isDebugEnabled()) {
				logger.debug("Load act: `{}` from directory.", name);
			}
		}

		return toml;
	}

	/**
	 * Resolves an act file path or URL from an act name and configured act source.
	 *
	 * @param name         act name or file path
	 * @param actsLocation base directory or URL for act definitions
	 * @return absolute file path or URL string
	 * @throws IOException if an explicitly referenced local act file does not exist
	 */
	private static String getAssolutePath(String name, String actsLocation) throws IOException {
		String path = null;
		if (!Strings.CS.startsWithAny(actsLocation, HTTP_PREFIX, HTTPS_PREFIX)) {
			File file = new File(name);
			if (!file.isAbsolute()) {
				file = new File(actsLocation, name + TOML_EXTENSION);
				path = file.getAbsolutePath();
			} else {
				if (!file.exists()) {
					throw new IOException("The act not found: " + name);
				}
				path = file.getAbsolutePath();
			}

		} else {
			String base = actsLocation.endsWith("/") ? actsLocation : actsLocation + "/";
			String uriString = Strings.CS.endsWith(name, TOML_EXTENSION) ? name : base + name + TOML_EXTENSION;
			path = URI.create(uriString).toURL().toString();
		}

		return path;
	}

	/**
	 * Loads and parses an act TOML document from episodes local file or remote URL.
	 *
	 * @param name absolute file path or URL to the TOML resource
	 * @return parsed TOML result, or {@code null} if episodes local file path does
	 *         not exist
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
	 * Copies dotted-string keys from the TOML parse result into {@code properties}.
	 *
	 * <p>
	 * If episodes key already exists in {@code properties}, the new value is
	 * formatted into the old value using {@link String#format(String, Object...)}.
	 * </p>
	 *
	 * @param properties properties destination
	 * @param toml       TOML parse result
	 */
	static void setActData(Map<String, Object> properties, TomlParseResult toml) {
		Set<Entry<String, Object>> props = toml.dottedEntrySet();
		for (Entry<String, Object> entry : props) {
			setActDataEntry(properties, entry);
		}
	}

	/**
	 * Applies episodes single TOML entry to the merged act property map.
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
			List<String> result = mergeTomlArrayValues(properties.get(key), ((TomlArray) value).toList());
			properties.put(key, result);
		}
	}

	/**
	 * Stores episodes string property, merging it with any inherited value already
	 * present.
	 *
	 * @param properties destination property map
	 * @param key        property name
	 * @param value      property value from the current act
	 */
	@SuppressWarnings("unchecked")
	private static void putStringActData(Map<String, Object> properties, String key, String value) {
		Object mainValue = properties.get(key);
		if (mainValue instanceof String) {
			properties.put(key, Strings.CS.replace((String) mainValue, "%s", Objects.toString(value, "%s")));
		} else if (mainValue instanceof List) {
			properties.put(key, mergeStringWithListValue((List<String>) mainValue, value));
		} else {
			properties.put(key, value);
		}
	}

	/**
	 * Merges episodes single string value into each string item of an inherited
	 * list.
	 *
	 * @param mainValueList inherited list value
	 * @param value         string value to merge through {@code %s}
	 * @return merged list result
	 */
	private static List<String> mergeStringWithListValue(List<String> mainValueList, String value) {
		List<String> result = new ArrayList<>();
		for (String mainValueItem : mainValueList) {
			if (mainValueItem.isEmpty()) {
				result.add(mainValueItem);
			} else {
				result.add(Strings.CS.replace(mainValueItem, "%s", Objects.toString(value, "%s")));
			}
		}
		return result;
	}

	/**
	 * Merges TOML array values with any existing inherited string or list value.
	 *
	 * @param existingValue existing property value, if any
	 * @param values        TOML array values from the current act
	 * @return merged string list
	 */
	private static List<String> mergeTomlArrayValues(Object existingValue, List<Object> values) {
		List<String> mainValues = toStringList(existingValue);
		List<String> result = new ArrayList<>();
		int maxSize = Math.max(values.size(), mainValues.size());
		for (int i = 0; i < maxSize; i++) {
			String value = getValueAt(values, i);
			result.add(resolveMergedValue(mainValues, i, value));
		}
		return result;
	}

	/**
	 * Converts an inherited property value to episodes list of strings.
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
	 * Returns the string value at episodes list index.
	 *
	 * @param values source values
	 * @param index  element index
	 * @return the string value at the index, or {@code null} if out of bounds
	 */
	private static String getValueAt(List<Object> values, int index) {
		return index < values.size() ? (String) values.get(index) : null;
	}

	/**
	 * Resolves episodes merged value for an inherited prompt slot.
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
		return Strings.CS.replace(mainValue, "%s", Objects.toString(value, "%s"));
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
			} else if (valueObj instanceof List && GWConstants.INPUTS_PROPERTY_NAME.equals(key)) {
				episodes.setEpisodes(resolvePromptValues((List<String>) valueObj));
			}
		}
		Object prompts = properties.get(GWConstants.INPUTS_PROPERTY_NAME);
		if (prompts instanceof String) {
			setDefaultPrompt((String) prompts);
		} else if (prompts instanceof List && !((List<String>) prompts).isEmpty()) {
			setDefaultPrompt(((List<String>) prompts).get(0));
		}
	}

	/**
	 * Applies episodes single string property to processor state or configuration.
	 *
	 * @param key   property name
	 * @param value property value
	 */
	private void applyStringActData(String key, String valueObj) {
		String value = resolveInheritedValue(key, valueObj);
		applyStringProperty(key, value);
	}

	/**
	 * Resolves episodes property value against the current configurator for
	 * inheritance.
	 *
	 * @param key   property name
	 * @param value act-defined value that may contain {@code %s}
	 * @return resolved property value
	 */
	private String resolveInheritedValue(String key, String value) {
		String inheritValue = getConfigurator().get(key, null);
		if (inheritValue != null) {
			return Strings.CS.replace(value, "%s", StringUtils.defaultString(inheritValue));
		}
		return value;
	}

	/**
	 * Applies episodes resolved string property by dispatching to the matching
	 * processor setting.
	 *
	 * @param key   property name
	 * @param value resolved property value
	 */
	private void applyStringProperty(String key, String value) {
		switch (key) {
		case GWConstants.INSTRUCTIONS_PROP_NAME:
			if (super.getInstructions() == null) {
				super.setInstructions(value);
			}
			break;
		case GWConstants.INPUTS_PROPERTY_NAME:
			episodes.setEpisodes(Collections.singletonList(value));
			break;
		case GWConstants.THREADS_PROP_NAME:
			super.setDegreeOfConcurrency(Integer.parseInt(value));
			break;
		case GWConstants.EXCLUDES_PROP_NAME:
			super.setExcludes(StringUtils.split(value, SEPARATOR_CHARS));
			break;
		case GWConstants.NONRECURSIVE_PROP_NAME:
			super.setNonRecursive(Boolean.parseBoolean(value));
			break;
		case GWConstants.INTERACTIVE_MODE_PROP_NAME:
			super.setInteractive(Boolean.parseBoolean(value));
			break;
		case GWConstants.MODEL_PROP_NAME:
			if (super.getModel() == null) {
				super.setModel(value);
				getConfigurator().set(key, value);
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
			updateValue.add(resolveInheritedValue(GWConstants.INPUTS_PROPERTY_NAME, value));
		}
		return updateValue;
	}

	/**
	 * Sets the directory used for loading external act definition files.
	 *
	 * @param actsLocation directory containing {@code *.toml} act files
	 */
	public void setActsLocation(String actsLocation) {
		if (!Strings.CS.startsWithAny(actsLocation, HTTP_PREFIX, HTTPS_PREFIX)) {
			File actDir = new File(actsLocation);
			if (!actDir.exists() || !actDir.isDirectory()) {
				throw new IllegalArgumentException(
						"Act directory does not exist or not episodes directory: " + actDir.getAbsolutePath());
			}
		}
		this.actsLocation = actsLocation;
		getConfigurator().set(GWConstants.ACTS_LOCATION_PROP_NAME, actsLocation);
	}

	/**
	 * Processes files and folders under the parent project directory (excluding
	 * modules).
	 */
	@Override
	protected void processParentFiles(ProjectLayout projectLayout) throws IOException {
		File projectDir = projectLayout.getProjectDir();
		List<File> children = findFiles(projectDir);

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child, projectDir));

		for (File child : children) {
			processFile(projectLayout, child);
		}

		boolean match = match(projectDir, projectDir);
		int requestedEpisodeId = 1;
		if (match && getDefaultPrompt() != null) {
			if (!episodes.isRegularOrder()) {
				try {
					requestedEpisodeId = episodes.requestedOrder((i, episode) -> {
						return process(projectLayout, projectDir, episode, i);
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
				return process(projectLayout, projectDir, episode, i);
			});
		}
	}

	/**
	 * Executes episodes single episode prompt after prepending act metadata.
	 *
	 * @param projectLayout active project layout
	 * @param projectDir    file or directory being processed
	 * @param prompt        episode prompt text
	 * @param episodeId     zero-based episode index
	 * @return provider result string, if any
	 */
	private String process(ProjectLayout projectLayout, File projectDir, String prompt, int episodeId) {
		String actInformation = episodes.getEpisodeInformation(episodeId);
		String projectInformation = promptBundle.getString("act_information");
		actInformation = String.format(projectInformation, actInformation);

		return super.process(projectLayout, projectDir, getInstructions(), actInformation, prompt);
	}

	/**
	 * Executes the act against episodes single file.
	 *
	 * @param projectLayout project layout
	 * @param file          file to process
	 * @throws IOException if provider execution fails
	 */
	@Override
	protected void processFile(ProjectLayout projectLayout, File file) throws IOException {
		process(projectLayout, file, getDefaultPrompt());
	}

}
