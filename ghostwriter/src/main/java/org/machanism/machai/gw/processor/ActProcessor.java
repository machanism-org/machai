package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

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
				instructions = toml.getString("instructions");
				defaultPrompt = toml.getString("inputs");

			} catch (IOException e) {
				// do nothing, the act not found on the custom directory.
			}
		}

		if (instructions == null || defaultPrompt == null) {
			String path = ACTS_BASENAME_PREFIX + name + ".toml";
			URL resource = ClassLoader.getSystemResource(path);
			if (resource != null) {
				try {
					String tomlStr = IOUtils.toString(resource, "UTF8");
					TomlParseResult toml = Toml.parse(tomlStr);
					if (instructions == null) {
						instructions = toml.getString("instructions");
					}
					if (defaultPrompt == null) {
						defaultPrompt = toml.getString("inputs");
					}
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				throw new IllegalArgumentException("Act: `" + act + "` not found.");
			}
		}

		super.setInstructions(instructions);

		defaultPrompt = MessageFormat.format(defaultPrompt, prompt);
		super.setDefaultPrompt(defaultPrompt);

	}

	public void setActDir(File actDir) {
		this.actDir = actDir;
	}
}
