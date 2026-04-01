package org.machanism.machai.cli;

import java.io.IOException;

import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command for reading and writing persistent Machai CLI configuration.
 *
 * <p>
 * The command stores values in {@value #MACHAI_PROPERTIES_FILE_NAME} (when available) via
 * {@link PropertiesConfigurator} and exposes helpers for configuring defaults used by other commands
 * (for example, the default GenAI provider/model and the default working directory).
 * </p>
 *
 * <h2>Examples</h2>
 * <pre>
 * config dir .\\my-project
 * config set --key gw.model --value OpenAI:gpt-5.1
 * config set --key gw.scanDir --value src/main/java
 * </pre>
 */
@ShellComponent
public class ConfigCommand {
	/** Default configuration file name used by this CLI. */
	public static final String MACHAI_PROPERTIES_FILE_NAME = "machai.properties";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCommand.class);

	/**
	 * Sets or gets a configuration property.
	 *
	 * <p>
	 * If {@code value} is provided, updates the configuration property with the given {@code key} and
	 * saves the change. If {@code value} is omitted, outputs the current value for {@code key}.
	 * </p>
	 *
	 * @param key   the configuration property key to set or get
	 * @param value the value to assign to the specified key; if {@code null}, the current value is displayed
	 * @throws IOException if an error occurs while saving the properties file
	 */
	@ShellMethod(value = "Set or get a configuration property. If value is omitted, displays the current value.", key = "set")
	public void set(
			@ShellOption(value = "--key", help = "The configuration property key to set or get.") String key,
			@ShellOption(value = "--value", help = "The value to assign to the specified key. If omitted, displays the current value.", defaultValue = ShellOption.NULL) String value)
			throws IOException {
		PropertiesConfigurator config = getConfigurator();
		if (value == null) {
			String currentValue = config.get(key);
			LOGGER.info("Current value for '{}': {}", key, currentValue);
		} else {
			config.set(key, value);
			config.save(MACHAI_PROPERTIES_FILE_NAME);
			LOGGER.info("Set '{}' to '{}' and saved.", key, value);
		}
	}

	/**
	 * Creates a configurator bound to {@value #MACHAI_PROPERTIES_FILE_NAME} when the file exists.
	 *
	 * <p>
	 * If the file is missing, a configurator is still returned and can be used to set values and
	 * later {@linkplain PropertiesConfigurator#save(String) save} them.
	 * </p>
	 *
	 * @return a properties-based configurator
	 */
	public static PropertiesConfigurator getConfigurator() {
		PropertiesConfigurator config = new PropertiesConfigurator();
		try {
			config.setConfiguration(MACHAI_PROPERTIES_FILE_NAME);
		} catch (Exception e) {
			// configuration file not found.
		}
		return config;
	}

}
