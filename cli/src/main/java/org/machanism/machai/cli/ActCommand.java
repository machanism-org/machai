package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ActCommand {

	private static Logger logger = LoggerFactory.getLogger(ActCommand.class);

	@PostConstruct
	public void init() {
	}

	@ShellMethod("Interactively execute a predefined action or prompt using Act mode.")
	public void act(@ShellOption(value = "action") String action[],
			@ShellOption(value = "model", help = "Set the GenAI provider and model", defaultValue = "CodeMie:gpt-5-2-2025-12-11") String model)
			throws IOException {

		File rootDir = ConfigCommand.config.getFile("gw.rootDir", SystemUtils.getUserDir());

		PropertiesConfigurator configurator = new PropertiesConfigurator();
		ActProcessor processor = new ActProcessor(rootDir, configurator, model);
		String act = StringUtils.join(action, " ");
		processor.setDefaultPrompt(act);

		String scanDir = configurator.get("gw.scanDir",
				ConfigCommand.config.getFile("gw.scanDir", rootDir).getAbsolutePath());
		if (scanDir == null) {
			scanDir = SystemUtils.getUserDir().getAbsolutePath();
		}
		logger.info("Starting scan of directory: {}", scanDir);
		File projectDir = processor.getRootDir();
		processor.scanDocuments(projectDir, scanDir);
		logger.info("Finished scanning directory: {}", scanDir);
	}
}