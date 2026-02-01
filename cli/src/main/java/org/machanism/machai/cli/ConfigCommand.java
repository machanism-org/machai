package org.machanism.machai.cli;

import java.io.File;

import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ConfigCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCommand.class);

	public static PropertiesConfigurator config = new PropertiesConfigurator();

	static {
		try {
			config.setConfiguration("machai.properties");
		} catch (Exception e) {
			// configuration file not found.
		}
	}

	@ShellMethod("Specifies the GenAI default value for service provider and model (e.g., `OpenAI:gpt-5.1`).")
	public void genai(@ShellOption(value = "", defaultValue = ShellOption.NULL) String genai) {
		config.set("genai", genai);
	}

	@ShellMethod("The default path to the project directory.")
	public void dir(@ShellOption(value = "", defaultValue = ShellOption.NULL) File dir) {
		config.set("dir", dir.getAbsolutePath());
	}

	@ShellMethod("Set the default value of minimum similarity threshold for search results.")
	public void score(@ShellOption(value = "", defaultValue = ShellOption.NULL) Double score) {
		config.set("score", Double.toString(score));
	}

	@ShellMethod("Show configuration properties.")
	public void conf() {
		LOGGER.info("Working directory path: dir = {}", config.get("dir"));
		LOGGER.info("Default GenAI Service: genai = {}", config.get("genai"));
		LOGGER.info("Default minimum score for semantic search: score = {}", config.get("score"));
	}
}
