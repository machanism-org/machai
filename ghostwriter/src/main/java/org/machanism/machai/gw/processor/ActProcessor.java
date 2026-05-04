package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArraySorter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.gw.tools.RepeatEpisodeException;
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
 * An <em>act</em> is a predefined prompt template stored as a {@code .toml}
 * file. Act files can be loaded from bundled classpath resources (under
 * {@code /acts}) and/or from a user-specified directory.
 * </p>
 *
 * <h2>Act format</h2>
 * <p>
 * Act files are parsed as TOML and support a small set of keys:
 * </p>
 * <ul>
 * <li>{@code instructions}: provider system instructions</li>
 * <li>{@code inputs}: a prompt template;
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
 * composed prompt against each file. Acts may also declare a {@code prologue}
 * and/or {@code epilogue} list of related acts to run before/after the main
 * scan.
 * </p>
 */
public class ActProcessor extends AIFileProcessor {

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(ActProcessor.class);

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

	private String[] prompts;

	private int activeEpisodeId = 1;

	private List<Integer> episodeIds;

	private Integer requestedEpisodeId;

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

		String name;
		String prompt;
		Matcher matcher = FIRST_WHITESPACE.matcher(act);
		if (matcher.find()) {
			int start = matcher.start();
			String substringAfter = StringUtils.substring(act, start);
			prompt = StringUtils.defaultIfBlank(substringAfter, StringUtils.defaultString(defaultPrompt));

			name = StringUtils.substring(act, 0, start);
		} else {
			name = act;
			prompt = defaultPrompt;
		}

		String episodeIds = StringUtils.substringAfterLast(name, EPISODE_DELIMETER);
		name = StringUtils.substringBeforeLast(name, EPISODE_DELIMETER);

		Map<String, Object> actData = new HashMap<>();

		loadAct(name, actData, actsLocation);

		prompt = StringUtils.trim(prompt);

		Object mainValue = actData.get(GWConstants.INPUTS_PROPERTY_NAME);
		if (mainValue instanceof String) {
			String value = applayPrompt(prompt, actData, (String) mainValue);
			actData.put(GWConstants.INPUTS_PROPERTY_NAME, value);

		} else if (mainValue instanceof List) {
			List<String> inputs = new ArrayList<>();
			for (String value : (List<String>) mainValue) {
				String episode = applayPrompt(prompt, actData, value);
				inputs.add(episode);
			}
			actData.put(GWConstants.INPUTS_PROPERTY_NAME, inputs);
		}

		applyActData(actData);

		if (StringUtils.isNoneBlank(episodeIds)) {
			if (Strings.CS.endsWith(episodeIds, STOP_SYMBOL)) {
				setDisableNormalOrder(true);
				episodeIds = StringUtils.substringBefore(episodeIds, STOP_SYMBOL);
			}

			List<Integer> collect = Arrays.asList(StringUtils.split(episodeIds, SEPARATOR_CHARS)).stream()
					.map(id -> Integer.parseInt(id))
					.collect(Collectors.toList());
			setEpisodeIds(collect);
		}
	}

	public void setDisableNormalOrder(boolean disableNormalOrder) {
		this.disableNormalOrder = disableNormalOrder;
	}

	private String applayPrompt(String prompt, Map<String, Object> actData, String mainValue) {
		String actPrompt = Objects.toString(actData.get("prompt"), "");
		String value = Strings.CS.replace((String) mainValue, "%s", Objects.toString(prompt, actPrompt));
		return value;
	}

	public void setEpisodeIds(List<Integer> episodeIds) {
		int numberOfEpisodes = prompts.length;

		boolean hasInvalidId = episodeIds.stream()
				.anyMatch(id -> id <= 0 || id > numberOfEpisodes);

		if (hasInvalidId) {
			throw new IllegalArgumentException(
					"All episode IDs must be between 1 and " + numberOfEpisodes + "  (inclusive).");
		}

		this.episodeIds = episodeIds;
	}

	/**
	 * Loads an act definition into the provided map, supporting inheritance via the
	 * {@code basedOn} property.
	 *
	 * <p>
	 * This method attempts to load the specified act from both a user-defined
	 * directory (custom act) and the built-in classpath resources. If both are
	 * present, the custom act wraps (overrides) the built-in act, allowing for
	 * extension or modification of base act behavior.
	 * </p>
	 *
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
		return toml;
	}

	/**
	 * Attempts to load an act definition from a user-defined directory.
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
		} else {
			if (actsLocation != null) {
				String absolutePath = getAssolutePath(name, actsLocation);
				toml = loadActToml(absolutePath);
			}
		}

		if (toml != null) {
			setActData(properties, toml);
		}

		return toml;
	}

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

	private static boolean isAbsolute(String name) {
		return Strings.CS.endsWith(name, TOML_EXTENSION);
	}

	/**
	 * Copies dotted-string keys from the TOML parse result into {@code properties}.
	 *
	 * <p>
	 * If a key already exists in {@code properties}, the new value is formatted
	 * into the old value using {@link String#format(String, Object...)}.
	 * </p>
	 *
	 * @param properties properties destination
	 * @param toml       TOML parse result
	 */
	static void setActData(Map<String, Object> properties, TomlParseResult toml) {
		Set<Entry<String, Object>> props = toml.dottedEntrySet();
		for (Entry<String, Object> entry : props) {
			String key = entry.getKey();
			if (entry.getValue() instanceof String) {
				String value = (String) entry.getValue();
				Object mainValue = properties.get(key);
				if (mainValue instanceof String) {
					value = Strings.CS.replace((String) mainValue, "%s", Objects.toString(value, "%s"));
				}
				properties.put(key, value);
			}
			if (entry.getValue() instanceof Boolean) {
				Boolean value = (Boolean) entry.getValue();
				properties.put(key, Boolean.toString(value));
			}
			if (entry.getValue() instanceof Integer) {
				Integer value = (Integer) entry.getValue();
				properties.put(key, Integer.toString(value));
			}
			if (entry.getValue() instanceof Double) {
				Double value = (Double) entry.getValue();
				properties.put(key, Double.toString(value));
			}
			if (entry.getValue() instanceof TomlArray) {
				List<Object> values = ((TomlArray) entry.getValue()).toList();

				List<String> mainValues = (List<String>) properties.get(key);
				if (mainValues == null) {
					mainValues = new ArrayList<String>();
				}

				List<String> result = new ArrayList<String>();

				for (int i = 0; i < Math.max(values.size(), mainValues.size()); i++) {
					String value;
					try {
						value = (String) values.get(i);
					} catch (IndexOutOfBoundsException e) {
						value = null;
					}

					String mainValue;
					try {
						mainValue = mainValues.get(i);
						mainValue = Strings.CS.replace(mainValue, "%s", Objects.toString(value, "%s"));
					} catch (IndexOutOfBoundsException e) {
						mainValue = value;
					}

					result.add(mainValue);
				}

				properties.put(key, result);
			}
		}
	}

	/**
	 * Applies loaded act data to this processor's configuration and runtime
	 * settings.
	 *
	 * @param properties properties loaded from TOML acts
	 */
	void applyActData(Map<String, Object> properties) {
		for (Entry<String, Object> entry : properties.entrySet()) {
			String key = entry.getKey();
			Object valueObj = entry.getValue();
			if (valueObj instanceof String) {
				String value = (String) valueObj;
				String inheritValue = getConfigurator().get(key, null);
				if (inheritValue != null) {
					value = Strings.CS.replace(value, "%s", StringUtils.defaultString(inheritValue));
				}
				switch (key) {
				case GWConstants.INSTRUCTIONS_PROP_NAME:
					super.setInstructions(value);
					break;

				case GWConstants.INPUTS_PROPERTY_NAME:
					setPrompts(value);
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
					super.setModel(value);
					getConfigurator().set(key, value);
					break;

				default:
					getConfigurator().set(key, value);
					break;
				}
			} else if (valueObj instanceof List && GWConstants.INPUTS_PROPERTY_NAME.equals(key)) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				List<String> prompts = (List) valueObj;
				List<String> updateValue = new ArrayList<String>();
				for (String value : prompts) {
					String inheritValue = getConfigurator().get(key, null);
					if (inheritValue != null) {
						value = Strings.CS.replace(value, "%s", StringUtils.defaultString(inheritValue));
					}
					updateValue.add(value);
				}

				setPrompts(updateValue.toArray(new String[0]));
			}
		}
	}

	public void setPrompts(String... value) {
		prompts = value;
	}

	public String[] getPrompts() {
		return prompts;
	}

	public void setDefaultPrompt(String defaultPrompt) {
		prompts = new String[] { defaultPrompt };
	}

	public String getDefaultPrompt() {
		int id = getActivePromptId();
		String prompt = ArrayUtils.isNotEmpty(prompts) ? (prompts != null ? prompts[id - 1] : null)
				: super.getDefaultPrompt();
		return prompt;
	}

	private int getActivePromptId() {
		return (episodeIds == null ? activeEpisodeId : episodeIds.get(activeEpisodeId - 1));
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
						"Act directory does not exist or not a directory: " + actDir.getAbsolutePath());
			}
		}
		this.actsLocation = actsLocation;
		getConfigurator().set(GWConstants.ACTS_LOCATION_PROP_NAME, actsLocation);
	}

	public void scanDocuments(File projectDir, String scanDir) throws IOException {
		do {
			try {
				boolean repeate;
				int iteration = 1;
				do {
					repeate = false;
					if (prompts.length > 1) {
						logger.info(StringUtils.center(" Episode #" + (getActivePromptId())
								+ (iteration > 1 ? " [Iteration: " + iteration + "]) " : " "), 80, "-"));
					}
					try {
						super.scanDocuments(projectDir, scanDir);

					} catch (RepeatEpisodeException e) {
						repeate = true;
					}
					iteration++;
				} while (repeate);

			} catch (MoveToEpisodeException e) {
				String episodeId = e.getEpisodeId();
				if (episodeId != null) {
					this.requestedEpisodeId = Integer.parseInt(episodeId);
				}
			}
		} while (nextAct());
	}

	private boolean nextAct() {
		if (requestedEpisodeId == null) {
			int currentActiveEpisodeId = getActivePromptId();
			this.activeEpisodeId++;

			boolean result;
			if (episodeIds != null) {
				result = activeEpisodeId <= episodeIds.size();
				if (!result) {
					episodeIds = null;
					this.activeEpisodeId = currentActiveEpisodeId + 1;
					result = disableNormalOrder ? false : activeEpisodeId <= prompts.length;
				}

			} else {
				result = activeEpisodeId <= prompts.length;
			}

			return result;

		} else {
			if (requestedEpisodeId > 0) {
				activeEpisodeId = requestedEpisodeId;
				requestedEpisodeId = 0;
				episodeIds = null;
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Processes non-module files and directories directly under the project
	 * directory.
	 */
	@Override
	protected void processParentFiles(ProjectLayout projectLayout) throws IOException {
		File scanProjectDir = projectLayout.getProjectDir();
		List<File> children = findFiles(scanProjectDir);

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child, scanProjectDir));

		for (File child : children) {
			processFile(projectLayout, child);
		}

		if (match(scanProjectDir, scanProjectDir) && getPrompts() != null
				&& !shouldExcludePath(scanProjectDir.toPath())) {
			String perform = process(projectLayout, scanProjectDir, getInstructions(), getDefaultPrompt());
			if (perform != null) {
				logger.info(">>> {}", perform);
			}
		}
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
		process(projectLayout, file, getInstructions(), getDefaultPrompt());
	}

}
