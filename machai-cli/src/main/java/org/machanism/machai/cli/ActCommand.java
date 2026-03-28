package org.machanism.machai.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.LineReader;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command that runs Ghostwriter "Act mode".
 *
 * <p>
 * The command scans the configured project folder and then executes a
 * predefined action/prompt interactively via {@link ActProcessor}.
 *
 * <h2>Examples</h2>
 * 
 * <pre>
 * act commit
 * act commit "and push"
 * act sonar-fix --model OpenAI:gpt-5.1
 * </pre>
 */
@ShellComponent
public class ActCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActCommand.class);

	private final LineReader lineReader;

	public ActCommand(@Lazy LineReader lineReader) {
		super();
		this.lineReader = lineReader;
	}

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
	 * <p>
	 * The default root directory and model are resolved from the persisted
	 * configuration managed by {@link ConfigCommand}.
	 *
	 * @param act words composing the action name and optional extra prompt text
	 *            passed to Act mode
	 * @throws IOException if scanning documents fails
	 */
	@ShellMethod("Interactively execute a predefined action or prompt using Act mode.")
	public void act(@ShellOption(value = "", defaultValue = ShellOption.NULL) String[] act) throws IOException {

		File projectDir = ConfigCommand.config.getFile(ProjectLayout.PROJECT_DIR_PROP_NAME, SystemUtils.getUserDir());

		PropertiesConfigurator configurator = new PropertiesConfigurator();
		try {
			configurator.setConfiguration(ConfigCommand.MACHAI_PROPERTIES_FILE_NAME);
		} catch (FileNotFoundException e) {
			// configuration file not found.
		}
		String resolvedModel = ConfigCommand.config.get(Ghostwriter.GW_MODEL_PROP_NAME);
		ActProcessor processor = new ActProcessor(projectDir, configurator, resolvedModel) {
			@Override
			protected String input() {
				return lineReader.readLine("prompt:> ");
			}
		};
		String prompt = StringUtils.join(act, " ");
		processor.setDefaultPrompt(prompt);

		Boolean logInputs = ConfigCommand.config.getBoolean(Genai.LOG_INPUTS_PROP_NAME, false);
		processor.setLogInputs(logInputs);

		String scanDir = processor.getConfigurator().get(Ghostwriter.GW_SCAN_DIR_PROP_NAME, null);
		if (scanDir == null) {
			scanDir = (projectDir != null ? projectDir : SystemUtils.getUserDir()).getAbsolutePath();
		}

		LOGGER.info("Starting scan of directory: {}", scanDir);
		projectDir = processor.getProjectDir();

		processor.scanDocuments(projectDir, scanDir);
		LOGGER.info("Finished scanning directory: {}", scanDir);
	}
}
