package org.machanism.machai.cli;

import java.io.File;
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
 * Spring Shell command that runs Ghostwriter "Act mode" for a project.
 *
 * <p>
 * The command resolves the project directory and other defaults from
 * {@link ConfigCommand} and then delegates execution to {@link ActProcessor}.
 * The user is prompted for any additional input required by the selected act.
 * </p>
 *
 * <h2>Examples</h2>
 * <pre>
 * act commit
 * act commit "and push"
 * act sonar-fix
 * </pre>
 */
@ShellComponent
public class ActCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActCommand.class);

	private final LineReader lineReader;

	/**
	 * Creates a new Act command instance.
	 *
	 * @param lineReader JLine reader used to prompt the user in interactive mode
	 */
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
	 * The default project directory and model are resolved from the persisted
	 * configuration managed by {@link ConfigCommand}.
	 * </p>
	 *
	 * @param act words composing the action name and optional extra prompt text
	 *            passed to Act mode
	 * @throws IOException if scanning documents fails
	 */
	@ShellMethod("Interactively execute a predefined action or prompt using Act mode.")
	public void act(@ShellOption(value = "", defaultValue = ShellOption.NULL) String[] act) throws IOException {

		PropertiesConfigurator config = ConfigCommand.getConfigurator();
		File projectDir = config.getFile(ProjectLayout.PROJECT_DIR_PROP_NAME, SystemUtils.getUserDir());

		String resolvedModel = config.get(Ghostwriter.MODEL_PROP_NAME, null);
		ActProcessor processor = new ActProcessor(projectDir, config, resolvedModel) {
			@Override
			protected String input() {
				return lineReader.readLine("prompt:> ");
			}
		};
		String prompt = StringUtils.join(act, " ");
		processor.setDefaultPrompt(prompt);

		Boolean logInputs = config.getBoolean(Genai.LOG_INPUTS_PROP_NAME, false);
		processor.setLogInputs(logInputs);

		String scanDir = processor.getConfigurator().get(Ghostwriter.SCAN_DIR_PROP_NAME, null);
		if (scanDir == null) {
			scanDir = (projectDir != null ? projectDir : SystemUtils.getUserDir()).getAbsolutePath();
		}

		LOGGER.info("Starting scan of directory: {}", scanDir);
		projectDir = processor.getProjectDir();

		processor.scanDocuments(projectDir, scanDir);
		LOGGER.info("Finished scanning directory: {}", scanDir);
	}
}
