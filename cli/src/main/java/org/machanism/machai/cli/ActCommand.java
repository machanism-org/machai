package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.ActProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ActCommand {

	private static Logger logger = LoggerFactory.getLogger(ActCommand.class);

	private static final PropertiesConfigurator config = new PropertiesConfigurator();
	private File rootDir = SystemUtils.getUserDir();
	private String genai = "CodeMie:gpt-5-2-2025-12-11";

	@PostConstruct
	public void init() {
	}

	@ShellMethod("Scan and process directories or files using GenAI guidance.")
	public void act(@ShellOption(value = "action") String action[]) throws IOException {
		ActProcessor processor = new ActProcessor(rootDir, config, genai);
		String act = StringUtils.join(action, " ");
		processor.setDefaultPrompt(act);
		try {
			processor.scanDocuments(rootDir, rootDir.getAbsolutePath());
		} catch (ProcessTerminationException e) {
			logger.error("ProcessTerminationException: ", e.getMessage());
		}
	}
}