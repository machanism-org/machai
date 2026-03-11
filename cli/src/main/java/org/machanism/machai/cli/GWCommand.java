package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command that scans files/directories and runs the Ghostwriter
 * guidance pipeline.
 *
 * <p>
 * The command resolves defaults (root directory, GenAI model, guidance and
 * instructions) from the persisted configuration managed by
 * {@link ConfigCommand}, and then delegates processing to
 * {@link GuidanceProcessor}.
 *
 * <h2>Examples</h2>
 * 
 * <pre>
 * gw --scanDirs .\\my-project --excludes target,.git
 * gw --model OpenAI:gpt-5.1 --guidance "Refactor for clarity"
 * gw --instructions "You are a strict code reviewer" --logInputs true
 * </pre>
 */
@ShellComponent
public class GWCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(GWCommand.class);

	/**
	 * Spring lifecycle hook.
	 */
	@PostConstruct
	public void init() {
		// Kept for future initialization.
	}

	/**
	 * Scans and processes directories or files using the configured GenAI model and
	 * guidance.
	 *
	 * @param threads      whether to enable multi-threaded processing
	 * @param model        optional GenAI provider/model identifier; if
	 *                     {@code null}, uses the configured default
	 * @param instructions optional system instructions (text/URL/path). If provided
	 *                     as an empty string, the user is prompted to input the
	 *                     full text via stdin.
	 * @param guidance     optional default guidance (text/URL/path). If provided as
	 *                     an empty string, the user is prompted to input the full
	 *                     text via stdin.
	 * @param excludes     optional comma-separated list of directory names to
	 *                     exclude
	 * @param logInputs    whether to log LLM request inputs to dedicated log files
	 * @param scanDirs     directories to scan; if omitted, scans the configured
	 *                     root directory
	 */
	@ShellMethod("Scan and process directories or files using GenAI guidance.")
	public void gw(
			@ShellOption(value = { "-t",
					"--threads" }, help = "Sets the number of threads for concurrent processing.", defaultValue = "1") int threads,
			@ShellOption(value = { "-m",
					"--model" }, help = "Set the GenAI provider and model", defaultValue = ShellOption.NULL) String model,
			@ShellOption(value = { "-i",
					"--instructions" }, help = "System instructions as text, URL, or file path", defaultValue = ShellOption.NULL) String instructions,
			@ShellOption(value = { "-g",
					"--guidance" }, help = "Default guidance as text, URL, or file path", defaultValue = ShellOption.NULL) String guidance,
			@ShellOption(value = { "-e",
					"--excludes" }, help = "Comma-separated list of directories to exclude", defaultValue = ShellOption.NULL) String excludes,
			@ShellOption(value = { "-l",
					"--logInputs" }, help = "Log LLM request inputs to dedicated log files", defaultValue = ShellOption.NULL) Boolean logInputs,
			@ShellOption(value = { "-r",
					"--rootDir" }, help = "Specify the path to the root directory for file processing.", defaultValue = ShellOption.NULL) File rootDir,
			@ShellOption(value = { "-s",
					"--scanDir" }, help = "Directories to scan.", defaultValue = ShellOption.NULL) String[] scanDirs) {

		try {
			if (rootDir == null) {
				rootDir = SystemUtils.getUserDir();
			}
			rootDir = ConfigCommand.config.getFile(Ghostwriter.GW_ROOTDIR_PROP_NAME, rootDir);

			String genaiValue = ConfigCommand.config.get(Ghostwriter.GW_GENAI_PROP_NAME, null);
			if (model != null) {
				genaiValue = model;
			}

			logInputs = ConfigCommand.config.getBoolean(Ghostwriter.GW_LOG_INPUTS_PROP_NAME, logInputs);

			String instructionsValue = ConfigCommand.config.get(Ghostwriter.GW_INSTRUCTIONS_PROP_NAME, null);
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

			String defaultGuidance = ConfigCommand.config.get(Ghostwriter.GW_GUIDANCE_PROP_NAME, null);
			if (guidance != null) {
				defaultGuidance = guidance;
				if (defaultGuidance.isEmpty()) {
					defaultGuidance = readText(
							"Please enter the guidance text below. When finished, press Ctrl+D (or Ctrl+Z on Windows) to signal end of input:");
				}
			}

			PropertiesConfigurator config = new PropertiesConfigurator();
			try {
				config.setConfiguration(ConfigCommand.MACHAI_PROPERTIES_FILE_NAME);
			} catch (IOException e) {
				// The property file is not defined, ignore.
			}

			for (String scanDir : dirs) {
				GuidanceProcessor processor = new GuidanceProcessor(rootDir, genaiValue, config);

				if (excludesArr != null) {
					LOGGER.info("Excludes: {}", Arrays.toString(excludesArr));
					processor.setExcludes(excludesArr);
				}

				if (instructionsValue != null) {
					LOGGER.info("Instructions: {}",
							org.apache.commons.lang.StringUtils.abbreviate(instructionsValue, 60));
					processor.setInstructions(instructionsValue);
				}

				processor.setDegreeOfConcurrency(threads);

				if (defaultGuidance != null) {
					LOGGER.info("Default Guidance: {}",
							org.apache.commons.lang.StringUtils.abbreviate(defaultGuidance, 60));
					processor.setDefaultPrompt(defaultGuidance);
				}

				processor.setLogInputs(ObjectUtils.getIfNull(logInputs, false));
				processor.scanDocuments(rootDir, scanDir);
				LOGGER.info("Finished scanning directory: {}", scanDir);
			}

		} catch (ProcessTerminationException e) {
			LOGGER.error("Process terminated: {}, Exit code: {}", e.getMessage(), e.getExitCode());
		} catch (Exception e) {
			LOGGER.error("Unexpected error: " + e.getMessage(), e);
		} finally {
			GenAIProviderManager.logUsage();
			LOGGER.info("File processing completed.");
		}
	}

	/**
	 * Reads multi-line text from stdin until EOF is reached.
	 *
	 * @param prompt message to show before reading input
	 * @return the entered text, or {@code null} if no content was provided
	 */
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
