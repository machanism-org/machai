package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.project.layout.ProjectLayout;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public class ActProcessor extends AIFileProcessor {

	private static final String ACTS_BASENAME_PREFIX = "/acts/";
	private File actDir;

	public ActProcessor(File rootDir, Configurator configurator, String genai) {
		super(rootDir, configurator, genai);
		actDir = configurator.getFile("gw.acts", null);
	}

	@Override
	public void setDefaultPrompt(String act) {
		String name = StringUtils.substringBefore(StringUtils.defaultString(act), " ");
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Act name must not be blank. Usage: --act <name> [prompt]");
		}

		String defaultPrompt = getDefaultPrompt();

		String prompt = "";
		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\s").matcher(act);
		if (matcher.find()) {
			int start = matcher.start();
			String substringAfter = StringUtils.substring(act, start);
			prompt = StringUtils.defaultIfBlank(substringAfter, StringUtils.defaultString(defaultPrompt));

			name = StringUtils.substring(act, 0, start);
		} else {
			name = act;
		}

		String path = ACTS_BASENAME_PREFIX + name + ".toml";
		URL resource = Ghostwriter.class.getResource(path);
		TomlParseResult toml = null;
		if (resource != null) {
			try {
				String tomlStr = IOUtils.toString(resource, "UTF8");
				toml = Toml.parse(tomlStr);
				setActData(prompt, toml);

			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			// do nothing, the act not found on the custom directory.
		}

		if (actDir != null) {
			try {
				File file = new File(actDir, name + ".toml");
				toml = Toml.parse(file.toPath());
				setActData(prompt, toml);

			} catch (IOException e) {
				// do nothing, the act not found on the custom directory.
			}
		}

		if (toml == null) {
			throw new IllegalArgumentException("Act: `" + name + "` not found.");
		}
	}

	private void setActData(String prompt, TomlParseResult toml) {
		Set<Entry<String, Object>> props = toml.dottedEntrySet();
		for (Entry<String, Object> entry : props) {
			String key = entry.getKey();
			if (entry.getValue() instanceof String) {
				String value = (String) entry.getValue();
				switch (key) {
				case "instructions":
					super.setInstructions(value);
					break;

				case "inputs":
					value = String.format(value, prompt);
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

	public void setActDir(File actDir) {
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

	protected void processFile(ProjectLayout projectLayout, File file) throws IOException {
		process(projectLayout, file, getInstructions(), getDefaultPrompt());
	}

}
