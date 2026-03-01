package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public class ActProcessor extends AIFileProcessor {

	private static final String ACTS_BASENAME_PREFIX = "acts/";
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
		String prompt = StringUtils.defaultIfBlank(StringUtils.substringAfter(StringUtils.defaultString(act), " "),
				StringUtils.defaultString(defaultPrompt));

		String instructions = null;
		if (actDir != null) {
			try {
				TomlParseResult toml = Toml.parse(new File(actDir, name + ".toml").toPath());

				Set<Entry<String, Object>> props = toml.dottedEntrySet(); 
				for (Entry<String, Object> entry : props) {
					String key = entry.getKey();
					if (entry.getValue() instanceof String) {
						String value = (String) entry.getValue();
						switch (key) {
						case "instructions":
							instructions = value;
							break;

						case "inputs":
							defaultPrompt = value;
							break;

						default:
							if (getConfigurator().get(key, null) == null) {
								getConfigurator().set(key, value);
							}
							break;
						}
					}
				}

			} catch (IOException e) {
				// do nothing, the act not found on the custom directory.
			}
		}

		String path = ACTS_BASENAME_PREFIX + name + ".toml";
		URL resource = ClassLoader.getSystemResource(path);
		if (resource != null) {
			try {
				String tomlStr = IOUtils.toString(resource, "UTF8");
				TomlParseResult toml = Toml.parse(tomlStr);
				Set<Entry<String, Object>> props = toml.dottedEntrySet(); 
				for (Entry<String, Object> entry : props) {
					String key = entry.getKey();
					if (entry.getValue() instanceof String) {
						String value = (String) entry.getValue();
						switch (key) {
						case "instructions":
							if (instructions == null) {
								instructions = value;
							}
							break;

						case "inputs":
							if (defaultPrompt == null) {
								defaultPrompt = value;
							}
							break;

						default:
							if (getConfigurator().get(key, null) == null) {
								getConfigurator().set(key, value);
							}
							break;
						}
					}
				}
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Act: `" + act + "` not found.");
		}

		super.setInstructions(instructions);

		defaultPrompt = MessageFormat.format(defaultPrompt, prompt);
		super.setDefaultPrompt(defaultPrompt);

	}

	public void setActDir(File actDir) {
		this.actDir = actDir;
	}
}
