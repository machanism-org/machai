package org.machanism.machai.gw.processor;

import java.io.File;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;

public class ActProcessor extends AIFileProcessor {

	public ActProcessor(File rootDir, Configurator configurator, String genai) {
		super(rootDir, configurator, genai);
	}

	@Override
	public void setDefaultGuidance(String action) {
		String name = StringUtils.substringBefore(StringUtils.defaultString(action), " ");
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Act name must not be blank. Usage: --act <name> [prompt]");
		}

		String defaultGuidance = getDefaultGuidance();
		String prompt = StringUtils.defaultIfBlank(StringUtils.substringAfter(StringUtils.defaultString(action), " "),
				StringUtils.defaultString(defaultGuidance));

		ResourceBundle promptBundle = ResourceBundle.getBundle("act/" + name);
		try {
			String inputs = promptBundle.getString("inputs");
			defaultGuidance = MessageFormat.format(inputs, prompt);
			super.setDefaultGuidance(defaultGuidance);
		} catch (MissingResourceException e) {
			throw new IllegalArgumentException(
					"Act '" + name + "' is missing required key 'inputs' in its properties file.", e);
		}

		try {
			try {
				String instructions = promptBundle.getString("instructions");
				super.setInstructions(instructions);
			} catch (MissingResourceException e) {
				// do nothing
			}
		} catch (MissingResourceException e) {
			throw new IllegalArgumentException(
					"Unknown act '" + name + "'. Make sure resources/act/" + name + ".properties exists.", e);
		}
	}
}
