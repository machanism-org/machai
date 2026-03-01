package org.machanism.machai.gw.processor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
				TomlParseResult result = Toml.parse(new File(actDir, name + ".toml").toPath());
				instructions = result.getString("instructions");
				defaultPrompt = result.getString("inputs");

			} catch (IOException e) {
				// do nothing, the act not found on the custom directory.
			}
		}

		if (instructions == null || defaultPrompt == null) {
			try {
				ResourceBundle promptBundle = ResourceBundle.getBundle(ACTS_BASENAME_PREFIX + name);
				if (instructions == null) {
					try {
						instructions = promptBundle.getString("instructions");
					} catch (MissingResourceException e) {
						// do nothing
					}
				}
				if (defaultPrompt == null) {
					try {
						String inputs = promptBundle.getString("inputs");
						defaultPrompt = MessageFormat.format(inputs, prompt);
					} catch (MissingResourceException e) {
						// do nothing
					}
				}
			} catch (MissingResourceException e) {
				throw e;
			}
		}

		super.setInstructions(instructions);
		super.setDefaultPrompt(defaultPrompt);

	}

	public void setActDir(File actDir) {
		this.actDir = actDir;
	}
}
