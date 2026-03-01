package org.machanism.machai.gw.processor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;

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

		ResourceBundle promptBundle;
		if (actDir != null) {
			try {
				URL[] urls = { actDir.toURI().toURL() };
				ClassLoader loader = new URLClassLoader(urls);

				Locale currentLocale = Locale.getDefault();
				promptBundle = ResourceBundle.getBundle(name, currentLocale, loader);

			} catch (SecurityException | MalformedURLException e) {
				throw new IllegalArgumentException(e);
			} catch (MissingResourceException e) {
				promptBundle = ResourceBundle.getBundle(ACTS_BASENAME_PREFIX + name);
			}
		} else {
			promptBundle = ResourceBundle.getBundle(ACTS_BASENAME_PREFIX + name);
		}

		try {
			String instructions = promptBundle.getString("instructions");
			super.setInstructions(instructions);
		} catch (MissingResourceException e) {
			// do nothing
		}

		try {
			String inputs = promptBundle.getString("inputs");
			defaultPrompt = MessageFormat.format(inputs, prompt);
			super.setDefaultPrompt(defaultPrompt);
		} catch (MissingResourceException e) {
			// do nothing
		}
	}

	public void setActDir(File actDir) {
		this.actDir = actDir;
	}
}
