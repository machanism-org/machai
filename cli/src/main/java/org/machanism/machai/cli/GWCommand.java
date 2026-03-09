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
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command that scans files/directories and runs the Ghostwriter
 * guidance pipeline.
 *
 * <p>The command resolves defaults (root directory, GenAI model, guidance and
 * instructions) from the persisted configuration managed by {@link ConfigCommand},
 * and then delegates processing to {@link GuidanceProcessor}.
 *
 * <h2>Examples</h2>
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
	 * @param model        optional GenAI provider/model identifier; if {@code null}, uses the
	 *                     configured default
	 * @param instructions optional system instructions (text/URL/path). If provided as an empty
	 *                     string, the user is prompted to input the full text via stdin.
	 * @param guidance     optional default guidance (text/URL/path). If provided as an empty
	 *                     string, the user is prompted to input the full text via stdin.
	 * @param excludes     optional comma-separated list of directory names to exclude
	 * @param logInputs    whether to log LLM request inputs to dedicated log files
	 * @param scanDirs     directories to scan; if omitted, scans the configured root directory
	 */
	@ShellMethod("Scan and process directories or files using GenAI guidance.")
	public void gw(
			@ShellOption(value = "threads", help = "Enable multi-threaded processing", defaultValue = "false") boolean threads,
			@ShellOption(value = "model", help = "Set the GenAI provider and model", defaultValue = ShellOption.NULL) String model,
			@ShellOption(value = "instructions", help = "System instructions as text, URL, or file path", defaultValue = ShellOption.NULL) String instructions,
			@ShellOption(value = "guidance", help = "Default guidance as text, URL, or file path", defaultValue = ShellOption.NULL) String guidance,
			@ShellOption(value = "excludes", help = "Comma-separated list of directories to exclude", defaultValue = ShellOption.NULL) String excludes,
			@ShellOption(value = "logInputs", help = "Log LLM request inputs to dedicated log files", defaultValue = "false") boolean logInputs,
			@ShellOption(value = "scanDirs", help = "Directories to scan", defaultValue = ShellOption.NULL) String[] scanDirs) {

		try {
			File rootDir = ConfigCommand.config.getFile(Ghostwriter.GW_ROOTDIR_PROP_NAME, SystemUtils.getUserDir());

			String genaiValue = ConfigCommand.config.get(Ghostwriter.GW_GENAI_PROP_NAME, null);
			if (model != null) {
				genaiValue = model;
			}

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

			boolean multiThread = threads;

			String defaultGuidance = ConfigCommand.config.get(Ghostwriter.GW_GUIDANCE_PROP_NAME, null);
			if (guidance != null) {
				defaultGuidance = guidance;
				if (defaultGuidance.isEmpty()) {
					defaultGuidance = readText(
							"Please enter the guidance text below. When finished, press Ctrl+D (or Ctrl+Z on Windows) to signal end of input:");
				}
			}

			for (String scanDir : dirs) {
				PropertiesConfigurator config = new PropertiesConfigurator(ConfigCommand.MACHAI_PROPERTIES_FILE_NAME);
				GuidanceProcessor processor = new GuidanceProcessor(rootDir, genaiValue, config);

				if (excludesArr != null) {
					LOGGER.info("Excludes: {}", Arrays.toString(excludesArr));
					processor.setExcludes(excludesArr);
				}

				if (instructionsValue != null) {
					LOGGER.info("Instructions: {}", org.apache.commons.lang.StringUtils.abbreviate(instructionsValue, 60));
					processor.setInstructions(instructionsValue);
				}

				processor.setModuleMultiThread(multiThread);

				if (defaultGuidance != null) {
					LOGGER.info("Default Guidance: {}", org.apache.commons.lang.StringUtils.abbreviate(defaultGuidance, 60));
					processor.setDefaultPrompt(defaultGuidance);
				}

				processor.setLogInputs(logInputs);
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
