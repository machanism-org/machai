package org.machanism.machai.cli;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.slf4j.Logger;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class GWCommand {

	private static Logger logger;
	private static final PropertiesConfigurator config = new PropertiesConfigurator();
	private static File gwHomeDir;

	@PostConstruct
	public void init() {
	}

	@ShellMethod("Scan and process directories or files using GenAI guidance.")
	public void gw(
			@ShellOption(value = "threads", help = "Enable multi-threaded processing", defaultValue = "false") boolean threads,
			@ShellOption(value = "genai", help = "Set the GenAI provider and model", defaultValue = ShellOption.NULL) String genai,
			@ShellOption(value = "instructions", help = "System instructions as text, URL, or file path", defaultValue = ShellOption.NULL) String instructions,
			@ShellOption(value = "guidance", help = "Default guidance as text, URL, or file path", defaultValue = ShellOption.NULL) String guidance,
			@ShellOption(value = "excludes", help = "Comma-separated list of directories to exclude", defaultValue = ShellOption.NULL) String excludes,
			@ShellOption(value = "logInputs", help = "Log LLM request inputs to dedicated log files", defaultValue = "false") boolean logInputs,
			@ShellOption(value = "scanDirs", help = "Directories to scan", defaultValue = ShellOption.NULL) String[] scanDirs) {
		logger.info("GW home dir: {}", gwHomeDir);

		try {
			File rootDir = config.getFile(Ghostwriter.GW_ROOTDIR_PROP_NAME, SystemUtils.getUserDir());

			String genaiValue = config.get(Ghostwriter.GW_GENAI_PROP_NAME, null);
			if (genai != null) {
				genaiValue = genai;
			}

			String instructionsValue = config.get(Ghostwriter.GW_INSTRUCTIONS_PROP_NAME, null);
			if (instructions != null) {
				instructionsValue = instructions;
				if (instructionsValue.isEmpty()) {
					instructionsValue = readText("No instructions were provided as an option value.\n"
							+ "Please enter the instructions text below. When you are done, press Ctrl+D (or Ctrl+Z on Windows) to finish:");
				}
			}

			String[] dirs = scanDirs;
			if (dirs == null || dirs.length == 0) {
				dirs = new String[] { rootDir.getAbsolutePath() };
			}

			String[] excludesArr = null;
			if (excludes != null) {
				excludesArr = excludes.split(",");
			}

			boolean multiThread = threads;

			String defaultGuidance = config.get(Ghostwriter.GW_GUIDANCE_PROP_NAME, null);
			if (guidance != null) {
				defaultGuidance = guidance;
				if (defaultGuidance.isEmpty()) {
					defaultGuidance = readText(
							"Please enter the guidance text below. When finished, press Ctrl+D (or Ctrl+Z on Windows) to signal end of input:");
				}
			}

			for (String scanDir : dirs) {
				GuidanceProcessor processor = new GuidanceProcessor(rootDir, genaiValue, config);
				if (excludesArr != null) {
					logger.info("Excludes: {}", Arrays.toString(excludesArr));
					processor.setExcludes(excludesArr);
				}

				if (instructionsValue != null) {
					logger.info("Instructions: {}",
							org.apache.commons.lang.StringUtils.abbreviate(instructionsValue, 60));
					processor.setInstructions(instructionsValue);
				}

				processor.setModuleMultiThread(multiThread);

				if (defaultGuidance != null) {
					logger.info("Default Guidance: {}",
							org.apache.commons.lang.StringUtils.abbreviate(defaultGuidance, 60));
					processor.setDefaultGuidance(defaultGuidance);
				}

				processor.setLogInputs(logInputs);

				processor.scanDocuments(rootDir, scanDir);
				logger.info("Finished scanning directory: {}", scanDir);
			}

		} catch (ProcessTerminationException e) {
			logger.error("Process terminated: {}", e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error: " + e.getMessage(), e);
		} finally {
			GenAIProviderManager.logUsage();
			logger.info("File processing completed.");
		}
	}

	private String readText(String prompt) {
		System.out.println(prompt);
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine()).append("\n");
			}
		}
		System.out.println("Input complete. Processing your text...");
		return sb.length() > 0 ? sb.deleteCharAt(sb.length() - 1).toString() : null;
	}
}