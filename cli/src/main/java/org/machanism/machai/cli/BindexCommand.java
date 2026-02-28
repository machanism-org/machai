package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.BindexCreator;
import org.machanism.machai.bindex.BindexRegister;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command for generating and registering <em>bindex</em> metadata.
 *
 * <p>This command is a CLI facade around {@link BindexCreator} (generation) and
 * {@link BindexRegister} (registration). It reads defaults from the persistent
 * CLI configuration managed by {@link ConfigCommand}.
 *
 * <h2>Typical usage</h2>
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
	private static final String DEFAULT_GENAI_VALUE = "CodeMie:gpt-5-2-2025-12-11";

	/** JLine line reader for shell interaction. */
	@Autowired
	@Lazy
	LineReader reader;

	private final PropertiesConfigurator config = new PropertiesConfigurator();

	/**
	 * Generates bindex files for a project directory.
	 *
	 * <p>If {@code dir} is not provided, the configured default directory is used
	 * (see {@code config dir ...}). If {@code genai} is not provided, the configured
	 * default GenAI provider/model is used.
	 *
	 * @param dir
	 *            the project directory to scan; if {@code null}, uses the configured
	 *            default or the current working directory
	 * @param update
	 *            whether to update previously saved metadata
	 * @param genai
	 *            GenAI service provider/model identifier (for example,
	 *            {@code OpenAI:gpt-5.1})
	 * @throws IOException
	 *             if scanning or file generation fails
	 */
	@ShellMethod("Generates bindex files.")
	public void bindex(
			@ShellOption(value = { "-d",
					"--dir" }, help = "The path to the project  directory.", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(value = { "-u",
					"--update" }, help = "The update mode: all saved data will be updated.", defaultValue = "false") boolean update,
			@ShellOption(value = { "-g",
					"--genai" }, help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`).", defaultValue = ShellOption.NULL) String genai)
			throws IOException {

		try {
			genai = Optional.ofNullable(genai)
					.orElse(ConfigCommand.config.get(Ghostwriter.GW_GENAI_PROP_NAME, DEFAULT_GENAI_VALUE));
			BindexCreator register = new BindexCreator(genai, config);
			register.update(update);
			dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
			register.scanFolder(dir);

		} finally {
			GenAIProviderManager.logUsage();
		}
	}

	/**
	 * Registers a previously generated bindex file for a project directory.
	 *
	 * <p>This command uploads/registers bindex metadata into an external registry
	 * addressed by {@code registerUrl}.
	 *
	 * @param dir
	 *            the project directory containing bindex metadata; if {@code null},
	 *            uses the configured default or the current working directory
	 * @param registerUrl
	 *            URL of the registry service for storing project metadata
	 * @param update
	 *            whether to update previously registered metadata
	 * @param chatModel
	 *            GenAI service provider/model identifier used for processing
	 *            (for example, {@code OpenAI:gpt-5.1}); if {@code null}, uses the
	 *            configured default
	 * @throws IOException
	 *             if scanning or registration fails
	 */
	@ShellMethod("Registers bindex file.")
	public void register(
			@ShellOption(value = { "-d",
					"--dir" }, help = "The path to the project  directory.", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(value = { "-r",
					"--registerUrl" }, defaultValue = ShellOption.NULL, help = "URL of the register database for storing project metadata.") String registerUrl,
			@ShellOption(value = { "-u",
					"--update" }, help = "The update mode: all saved data will be updated.", defaultValue = "true") boolean update,
			@ShellOption(value = { "-g",
					"--genai" }, help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`).", defaultValue = ShellOption.NULL) String chatModel)
			throws IOException {

		try {
			dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
			chatModel = Optional.ofNullable(chatModel)
					.orElse(ConfigCommand.config.get(Ghostwriter.GW_GENAI_PROP_NAME, DEFAULT_GENAI_VALUE));
			BindexRegister register = new BindexRegister(chatModel, registerUrl, config);
			register.update(update);
			register.scanFolder(dir);
		} finally {
			GenAIProviderManager.logUsage();
		}

	}
}
