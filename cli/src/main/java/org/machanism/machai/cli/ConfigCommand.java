package org.machanism.machai.cli;

import java.io.File;

import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell command for reading and writing persistent Machai CLI configuration.
 *
 * <p>The command stores values in {@code machai.properties} (when available) via
 * {@link PropertiesConfigurator} and exposes helpers for configuring defaults
 * used by other commands (e.g., the default GenAI provider/model, the default
 * working directory, and the default semantic-search score threshold).
 *
 * <h2>Examples</h2>
 *
 * <pre>
 * config genai OpenAI:gpt-5.1
 * config dir ./my-project
 * config score 0.9
 * config conf
 * </pre>
 */
@ShellComponent
public class ConfigCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCommand.class);

	/**
	 * Shared configuration instance used by commands in this package.
	 */
	public static PropertiesConfigurator config = new PropertiesConfigurator();

	static {
		try {
			config.setConfiguration("machai.properties");
		} catch (Exception e) {
			// configuration file not found.
		}
	}

	/**
	 * Sets the default GenAI provider and model identifier.
	 *
	 * @param model provider/model identifier (e.g., {@code OpenAI:gpt-5.1})
	 */
	@ShellMethod("Specifies the GenAI default value for service provider and model (e.g., `OpenAI:gpt-5.1`).")
	public void model(@ShellOption(value = "", defaultValue = ShellOption.NULL) String model) {
		config.set("gw.model", model);
	}

	/**
	 * Sets the default working directory used when a command does not receive a
	 * {@code --dir} option.
	 *
	 * @param dir default project directory
	 */
	@ShellMethod("The default path to the root project directory.")
	public void root(@ShellOption(value = "", defaultValue = ShellOption.NULL) File dir) {
		config.set("gw.rootDir", dir.getAbsolutePath());
	}

	/**
	 * Sets the default minimum similarity threshold used when selecting libraries
	 * from semantic search results.
	 *
	 * @param score minimum similarity score
	 */
	@ShellMethod("Set the default value of minimum similarity threshold for search results.")
	public void score(@ShellOption(value = "", defaultValue = ShellOption.NULL) Double score) {
		config.set("bindex.score", Double.toString(score));
	}

	/**
	 * Prints the current configuration values.
	 */
	@ShellMethod("Show configuration properties.")
	public void conf() {
		LOGGER.info("Working directory path: dir = {}", config.get("gw.rootDir", null));
		LOGGER.info("Default GenAI Service: genai = {}", config.get("gw.model", null));
		LOGGER.info("Default minimum score for semantic search: score = {}", config.get("bindex.score", null));
	}
}
