package org.machanism.machai.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command that runs Ghostwriter "Act mode".
 *
 * <p>The command scans the configured project folder and then executes a
 * predefined action/prompt interactively via {@link ActProcessor}.
 *
 * <h2>Examples</h2>
 * <pre>
 * act commit
 * act commit "and push"
 * act sonar-fix --model OpenAI:gpt-5.1
 * </pre>
 */
@ShellComponent
public class ActCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActCommand.class);

	/**
	 * Spring lifecycle hook.
	 */
	@PostConstruct
	public void init() {
		// Kept for future initialization.
	}

	/**
	 * Interactively executes a predefined action/prompt using Act mode.
	 *
	 * <p>The default root directory and model are resolved from the persisted
	 * configuration managed by {@link ConfigCommand}.
	 *
	 * @param action the name of the predefined action to execute
	 * @param prompt optional extra prompt text appended to {@code action}
	 * @param model  optional GenAI provider/model identifier (for example,
	 *               {@code OpenAI:gpt-5.1}); if {@code null}, uses the configured
	 *               default
	 * @throws IOException if scanning documents fails
	 */
	@ShellMethod("Interactively execute a predefined action or prompt using Act mode.")
	public void act(@ShellOption(value = "action") String action,
			@ShellOption(value = "p", defaultValue = "") String prompt,
			@ShellOption(value = "r", help = "Specify the path to the root directory for file processing.", defaultValue = ShellOption.NULL) File rootDir,
			@ShellOption(value = "m", help = "Set the GenAI provider and model", defaultValue = ShellOption.NULL) String model)
			throws IOException {

		if (rootDir == null) {
			rootDir = SystemUtils.getUserDir();
		}
		rootDir = ConfigCommand.config.getFile(Ghostwriter.GW_ROOTDIR_PROP_NAME, rootDir);
		
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		try {
			configurator.setConfiguration(ConfigCommand.MACHAI_PROPERTIES_FILE_NAME);
		} catch (FileNotFoundException e) {
			// configuration file not found.
		}
		String resolvedModel = model == null ? ConfigCommand.config.get("gw.model") : model;
		ActProcessor processor = new ActProcessor(rootDir, configurator, resolvedModel);
		processor.setDefaultPrompt(action + " " + prompt);

		String scanDir = ConfigCommand.config.get("gw.scanDir", null);
		if (scanDir == null) {
			scanDir = (rootDir != null ? rootDir : SystemUtils.getUserDir()).getAbsolutePath();
		}

		LOGGER.info("Starting scan of directory: {}", scanDir);
		File projectDir = processor.getRootDir();
		processor.scanDocuments(projectDir, scanDir);
		LOGGER.info("Finished scanning directory: {}", scanDir);
	}
}
