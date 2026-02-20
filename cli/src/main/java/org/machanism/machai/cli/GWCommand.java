package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.FileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class GWCommand {

	private static Logger logger;
	private static final String DEFAULT_GENAI_VALUE = "OpenAI:gpt-5-mini";
	private static final PropertiesConfigurator config = new PropertiesConfigurator();
	private static File gwHomeDir;

	@PostConstruct
	public void init() {
		try {
			gwHomeDir = config.getFile("GW_HOME", null);

			if (gwHomeDir == null) {
				System.out.println(
						"GW_HOME environment variable not found. Using the directory where the Ghostwriter JAR file is located.");
				gwHomeDir = new File(GWCommand.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				if (gwHomeDir.isFile()) {
					gwHomeDir = gwHomeDir.getParentFile();
				}
			}

			org.machanism.machai.log.FileAppender.setExecutionDir(gwHomeDir);
			logger = LoggerFactory.getLogger(GWCommand.class);

			try {
				File configFile = new File(gwHomeDir, System.getProperty("gw.config", "gw.properties"));
				config.setConfiguration(configFile.getAbsolutePath());
			} catch (IOException e) {
				// The property file is not defined, ignore.
			}
		} catch (Exception e) {
			// Configuration file not found. An alternative configuration method will be
			// used.
		}
	}

	@ShellMethod("Scan and process directories or files using GenAI guidance.")
	public void gw(
			@ShellOption(value = "root", help = "Root directory for file processing", defaultValue = ShellOption.NULL) String root,
			@ShellOption(value = "threads", help = "Enable multi-threaded processing", defaultValue = "false") boolean threads,
			@ShellOption(value = "genai", help = "Set the GenAI provider and model", defaultValue = ShellOption.NULL) String genai,
			@ShellOption(value = "instructions", help = "System instructions as text, URL, or file path", defaultValue = ShellOption.NULL) String instructions,
			@ShellOption(value = "guidance", help = "Default guidance as text, URL, or file path", defaultValue = ShellOption.NULL) String guidance,
			@ShellOption(value = "excludes", help = "Comma-separated list of directories to exclude", defaultValue = ShellOption.NULL) String excludes,
			@ShellOption(value = "logInputs", help = "Log LLM request inputs to dedicated log files", defaultValue = "false") boolean logInputs,
			@ShellOption(value = "scanDirs", help = "Directories to scan", defaultValue = ShellOption.NULL) String[] scanDirs) {
		logger.info("GW home dir: {}", gwHomeDir);

		try {
			File rootDir = config.getFile("root", null);
			if (root != null) {
				rootDir = new File(root);
			}

			String genaiValue = config.get("genai", DEFAULT_GENAI_VALUE);
			if (genai != null) {
				genaiValue = genai;
			}

			String instructionsValue = config.get("instructions", null);
			if (instructions != null) {
				instructionsValue = instructions;
				if (instructionsValue.isEmpty()) {
					instructionsValue = readText("No instructions were provided as an option value.\n"
							+ "Please enter the instructions text below. When you are done, press Ctrl+D (or Ctrl+Z on Windows) to finish:");
				}
			}

			if (rootDir == null) {
				rootDir = new File(System.getProperty("user.dir"));
			}

			String[] dirs = scanDirs;
			if (dirs == null || dirs.length == 0) {
				dirs = new String[] { rootDir.getAbsolutePath() };
			}

			String[] excludesArr = null;
			if (excludes != null) {
				excludesArr = excludes.split(",");
			}

			logger.info("Root directory: {}", rootDir);

			boolean multiThread = threads;

			String defaultGuidance = config.get("guidance", null);
			if (guidance != null) {
				defaultGuidance = guidance;
				if (defaultGuidance.isEmpty()) {
					defaultGuidance = readText(
							"Please enter the guidance text below. When finished, press Ctrl+D (or Ctrl+Z on Windows) to signal end of input:");
				}
			}

			for (String scanDir : dirs) {
				logger.info("Starting scan of directory: {}", scanDir);

				FileProcessor processor = new FileProcessor(rootDir, genaiValue, config);
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