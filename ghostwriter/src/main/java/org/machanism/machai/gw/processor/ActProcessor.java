package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

	private static final String BASED_ON_PROPERTY_NAME = "basedOn";
	private static final String ACTS_BASENAME_PREFIX = "/acts/";
	private static final Pattern FIRST_WHITESPACE = Pattern.compile("\\s");

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
		loadAct(name, actData);
		String actPrompt = Objects.toString(super.getDefaultPrompt(),
				getConfigurator().get("prompt", actData.getProperty("inputs")));
		String value = String.format(actPrompt, StringUtils.defaultString(prompt));
		super.setDefaultPrompt(value);
		applyActData(actData);
	}

	private void loadAct(String name, Properties properties) {
		TomlParseResult customToml = tryLoadActFromDirectory(properties, name);
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
			loadAct(basedOn, properties);
			properties.remove(BASED_ON_PROPERTY_NAME);
		}
	}

	private TomlParseResult tryLoadActFromClasspath(Properties properties, String name) {
		String path = ACTS_BASENAME_PREFIX + name + ".toml";
		URL resource = Ghostwriter.class.getResource(path);
		if (resource == null) {
			return null;
		}

		try {
			String tomlStr = IOUtils.toString(resource, "UTF8");
			TomlParseResult toml = Toml.parse(tomlStr);
			setActData(properties, toml);
			return toml;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private TomlParseResult tryLoadActFromDirectory(Properties properties, String name) {
		if (actDir == null) {
			return null;
		}

		try {
			File file = new File(actDir, name + ".toml");
			TomlParseResult toml = Toml.parse(file.toPath());
			setActData(properties, toml);
			return toml;
		} catch (IOException e) {
			return null;
		}
	}

	private void setActData(Properties properties, TomlParseResult toml) {
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
					value = String.format(value, StringUtils.defaultString(properties.getProperty("prompt")));
					super.setDefaultPrompt(value);
					break;

				case "gw.threads":
					super.setModuleMultiThread(Boolean.parseBoolean(value));
					break;

				case "gw.excludes":
					super.setExcludes(StringUtils.split(value, ","));
					break;

				case "gw.nonRecursive":
					super.setNonRecursive(Boolean.getBoolean(value));
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
