package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

/**
 * Processor that runs Ghostwriter in "Act" mode.
 *
 * <p>
 * An act is a predefined prompt template stored as a {@code .toml} file. Act
 * files can be loaded from bundled classpath resources (under {@code /acts})
 * and/or from a user-specified directory.
 * </p>
 *
 * <h2>Act format</h2>
 * <p>
 * Act files are parsed as TOML and support a small set of keys:
 * </p>
 * <ul>
 * <li>{@code instructions}: provider system instructions</li>
 * <li>{@code inputs}: a prompt template;
 * {@link String#format(String, Object...)} is used to inject the user-provided
 * prompt text</li>
 * <li>{@code gw.threads}: enables module multi-threading</li>
 * <li>{@code gw.excludes}: comma-separated scan exclusions</li>
 * <li>{@code gw.nonRecursive}: disables module recursion</li>
 * <li>any other key is forwarded to the underlying configuration</li>
 * </ul>
 */
public class ActProcessor extends AIFileProcessor {

	/** Logger for documentation input processing events. */
	private static final Logger logger = LoggerFactory.getLogger(ActProcessor.class);

	/** Classpath base directory for built-in act definitions. */
	public static final String ACTS_BASENAME_PREFIX = "/acts/";

	private static final String BASED_ON_PROPERTY_NAME = "basedOn";
	private static final Pattern FIRST_WHITESPACE = Pattern.compile("\\s");

	/** Optional directory containing external {@code *.toml} act files. */
	private File actDir;

	/**
	 * Creates an act processor.
	 *
	 * @param rootDir      root directory used as a base for relative paths
	 * @param configurator configuration source
	 * @param genai        provider key/name (including model)
	 */
	public ActProcessor(File rootDir, Configurator configurator, String genai) {
		super(rootDir, configurator, genai);
		actDir = configurator.getFile("gw.acts", null);
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
	 */
	@Override
	public void setDefaultPrompt(String act) {
		act = StringUtils.defaultIfBlank(act, "help");
		String name = StringUtils.substringBefore(StringUtils.defaultString(act), " ");
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Act name must not be blank. Usage: --act <name> [prompt]");
		}

		String defaultPrompt = getDefaultPrompt();

		String prompt;
		Matcher matcher = FIRST_WHITESPACE.matcher(StringUtils.defaultString(act));
		if (matcher.find()) {
			int start = matcher.start();
			String substringAfter = StringUtils.substring(act, start);
			prompt = StringUtils.defaultIfBlank(substringAfter, StringUtils.defaultString(defaultPrompt));

			name = StringUtils.substring(act, 0, start);
		} else {
			name = act;
			prompt = defaultPrompt;
		}

		Properties actData = new Properties();
		try {
			loadAct(name, actData, actDir);
			String actPrompt = Objects.toString(super.getDefaultPrompt(),
					getConfigurator().get("prompt", actData.getProperty("inputs", "%s")));
			String value = String.format(actPrompt, StringUtils.defaultString(prompt).trim());

			super.setDefaultPrompt(value);
			applyActData(actData);

		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Loads an act definition into the provided {@link Properties} object,
	 * supporting inheritance via the {@code basedOn} property.
	 * <p>
	 * This method attempts to load the specified act from both a user-defined
	 * directory (custom act) and the built-in classpath resources. If both are
	 * present, the custom act wraps (overrides) the built-in act, allowing for
	 * extension or modification of base act behavior.
	 * <p>
	 * If the act specifies a {@code basedOn} property, the parent act is loaded
	 * first (recursively), and its properties are merged. The child act's
	 * properties then override or extend the parent, following inheritance rules.
	 *
	 * @param name       the name of the act to load (without the {@code .toml}
	 *                   extension)
	 * @param properties the destination {@link Properties} object to populate with
	 *                   parsed act properties
	 * @param actDir     optional directory containing user-defined (custom) act
	 *                   files; may be {@code null}
	 * @throws IOException              if reading act content fails
	 * @throws IllegalArgumentException if the specified act cannot be found in
	 *                                  either location
	 *
	 *                                  <p>
	 *                                  <b>Inheritance and Overrides:</b>
	 *                                  </p>
	 *                                  <ul>
	 *                                  <li>If a custom act and a built-in act with
	 *                                  the same name exist, the custom act wraps
	 *                                  the built-in act.</li>
	 *                                  <li>If the act defines {@code basedOn}, the
	 *                                  parent act is loaded first, and its
	 *                                  properties are merged recursively.</li>
	 *                                  <li>Child act properties override or extend
	 *                                  parent properties as appropriate.</li>
	 *                                  </ul>
	 */
	public static void loadAct(String name, Properties properties, File actDir) throws IOException {
		TomlParseResult customToml = tryLoadActFromDirectory(properties, name, actDir);
		TomlParseResult toml = tryLoadActFromClasspath(properties, name);

		if (toml == null && customToml == null) {
			throw new IllegalArgumentException("Act: `" + name + "` not found.");
		}

		String basedOn = null;
		if (customToml != null) {
			basedOn = customToml.getString(BASED_ON_PROPERTY_NAME);
		}
		if (basedOn == null && toml != null) {
			basedOn = toml.getString(BASED_ON_PROPERTY_NAME);
		}

		if (basedOn != null) {
			loadAct(basedOn, properties, actDir);
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
	public static TomlParseResult tryLoadActFromClasspath(Properties properties, String name) throws IOException {
		String path = ACTS_BASENAME_PREFIX + name + ".toml";
		URL resource = Ghostwriter.class.getResource(path);
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
	 * @param properties destination for parsed dotted properties
	 * @param name       act name (without {@code .toml})
	 * @param actDir     directory containing {@code *.toml} act files (may be
	 *                   {@code null})
	 * @return parsed TOML result, or {@code null} when not found
	 * @throws IOException if the file cannot be read
	 */
	public static TomlParseResult tryLoadActFromDirectory(Properties properties, String name, File actDir)
			throws IOException {
		if (actDir == null) {
			return null;
		}

		File file = new File(actDir, name + ".toml");
		TomlParseResult toml = null;
		if (file.exists()) {
			toml = Toml.parse(file.toPath());
			setActData(properties, toml);
		}
		return toml;
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
	private static void setActData(Properties properties, TomlParseResult toml) {
		Set<Entry<String, Object>> props = toml.dottedEntrySet();
		for (Entry<String, Object> entry : props) {
			String key = entry.getKey();
			if (entry.getValue() instanceof String) {
				String value = (String) entry.getValue();
				String inheritValue = properties.getProperty(key);
				if (inheritValue != null) {
					value = String.format(inheritValue, value);
				}
				properties.setProperty(key, value);
			}
		}
	}

	private void applyActData(Properties properties) {
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			if (entry.getValue() instanceof String) {
				String value = (String) entry.getValue();
				String inheritValue = getConfigurator().get(key, null);
				if (inheritValue != null) {
					value = String.format(value, StringUtils.defaultString(inheritValue));
				}
				switch (key) {
				case "instructions":
					super.setInstructions(value);
					break;

				case "inputs":
					value = String.format(value, StringUtils.defaultString(getDefaultPrompt()));
					super.setDefaultPrompt(value);
					break;

				case "gw.threads":
					super.setModuleMultiThread(Boolean.parseBoolean(value));
					break;

				case "gw.excludes":
					super.setExcludes(StringUtils.split(value, ","));
					break;

				case "gw.nonRecursive":
					super.setNonRecursive(Boolean.parseBoolean(value));
					break;

				default:
					getConfigurator().set(key, value);
					break;
				}
			}
		}
	}

	/**
	 * Sets the directory used for loading external act definition files.
	 *
	 * @param actDir directory containing {@code *.toml} act files
	 */
	public void setActDir(File actDir) {
		if (actDir != null) {
			if (!actDir.exists() || !actDir.isDirectory()) {
				logger.error("Act directory does not exist or not a directory: {}", actDir.getAbsolutePath());
				return;
			}
		}
		this.actDir = actDir;
	}

	@Override
	protected void processParentFiles(ProjectLayout projectLayout) throws IOException {
		File projectDir = projectLayout.getProjectDir();
		List<File> children = findFiles(projectDir);

		children.removeIf(child -> isModuleDir(projectLayout, child) || !match(child, projectDir));

		for (File child : children) {
			processFile(projectLayout, child);
		}

		boolean match = match(projectDir, projectDir);

		if (match && getDefaultPrompt() != null) {
			process(projectLayout, projectDir, getInstructions(), getDefaultPrompt());
		}
	}

	/**
	 * Executes the act against a single file.
	 *
	 * @param projectLayout project layout
	 * @param file          file to process
	 * @throws IOException if provider execution fails
	 */
	protected void processFile(ProjectLayout projectLayout, File file) throws IOException {
		process(projectLayout, file, getInstructions(), getDefaultPrompt());
	}

}
