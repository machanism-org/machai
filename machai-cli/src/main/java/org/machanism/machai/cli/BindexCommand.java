package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.BindexCreator;
import org.machanism.machai.bindex.BindexRegister;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command for generating and registering <em>bindex</em> metadata.
 *
 * <p>
 * This command is a CLI facade around {@link BindexCreator} (generation) and
 * {@link BindexRegister} (registration). It reads defaults from the persistent
 * CLI configuration managed by {@link ConfigCommand}.
 *
 * <h2>Typical usage</h2>
 * 
 * <pre>
 * bindex --dir .\\my-project
 * register --dir .\\my-project --registerUrl https://registry.example/api
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class BindexCommand {
	private static final Logger logger = LoggerFactory.getLogger(BindexCommand.class);

	/** Configuration used by bindex creator/register operations. */
	private final PropertiesConfigurator config = new PropertiesConfigurator();

	/**
	 * Generates bindex files for a project directory.
	 *
	 * <p>
	 * If {@code dir} is not provided, the configured default directory is used. If
	 * {@code model} is not provided, the configured default GenAI provider/model is
	 * used.
	 *
	 * @param dir    the project directory to scan; if {@code null}, uses the
	 *               configured default or the current working directory
	 * @param update whether to update previously saved metadata
	 * @param model  GenAI service provider/model identifier (for example,
	 *               {@code OpenAI:gpt-5.1})
	 * @throws IOException if scanning or file generation fails
	 */
	@ShellMethod("Generates bindex files.")
	public void bindex(
			@ShellOption(value = { "-d",
					"--dir" }, help = "The path to the project  directory.", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(value = { "-u",
					"--update" }, help = "The update mode: all saved data will be updated.", defaultValue = "false") boolean update,
			@ShellOption(value = { "-m",
					"--model" }, help = "Specifies the GenAI service provider and model (e.g., `"
							+ BindexCreator.DEFAULT_MODEL + "`).", defaultValue = ShellOption.NULL) String model)
			throws IOException {

		try {
			model = Optional.ofNullable(model)
					.orElse(ConfigCommand.config.get(BindexCreator.MODEL_PROP_NAME, BindexCreator.DEFAULT_MODEL));
			BindexCreator register = new BindexCreator(model, config);
			register.update(update);
			dir = Optional.ofNullable(dir).orElse(
					ConfigCommand.config.getFile(BindexCreator.PROJECT_DIR_PROP_NAME, SystemUtils.getUserDir()));

			logger.info("The project directory: {}", dir);
			logger.info("GenAI model: {}", model);

			register.scanFolder(dir);
		} finally {
			GenAIProviderManager.logUsage();
		}
	}

	/**
	 * Registers a previously generated bindex file for a project directory.
	 *
	 * <p>
	 * This command uploads/registers bindex metadata into an external registry
	 * addressed by {@code registerUrl}.
	 *
	 * @param dir         the project directory containing bindex metadata; if
	 *                    {@code null}, uses the configured default or the current
	 *                    working directory
	 * @param registerUrl URL of the registry service for storing project metadata
	 * @param update      whether to update previously registered metadata
	 * @param model       GenAI service provider/model identifier used for
	 *                    processing (for example, {@code OpenAI:gpt-5.1}); if
	 *                    {@code null}, uses the configured default
	 * @throws IOException if scanning or registration fails
	 */
	@ShellMethod("Registers bindex file.")
	public void register(
			@ShellOption(value = { "-d",
					"--dir" }, help = "The path to the project  directory.", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(value = { "-r",
					"--registerUrl" }, defaultValue = ShellOption.NULL, help = "URL of the register database for storing project metadata.") String registerUrl,
			@ShellOption(value = { "-u",
					"--update" }, help = "The update mode: all saved data will be updated.", defaultValue = "true") boolean update,
			@ShellOption(value = { "-m",
					"--model" }, help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`).", defaultValue = ShellOption.NULL) String model)
			throws IOException {

		try {
			dir = Optional.ofNullable(dir).orElse(
					ConfigCommand.config.getFile(BindexCreator.PROJECT_DIR_PROP_NAME, SystemUtils.getUserDir()));
			model = Optional.ofNullable(model)
					.orElse(ConfigCommand.config.get(Ghostwriter.GW_GENAI_PROP_NAME, BindexCreator.DEFAULT_MODEL));

			logger.info("The project directory: {}", dir);
			logger.info("GenAI model: {}", model);

			BindexRegister register = new BindexRegister(model, registerUrl, config);
			register.update(update);
			register.scanFolder(dir);
		} finally {
			GenAIProviderManager.logUsage();
		}

	}
}
