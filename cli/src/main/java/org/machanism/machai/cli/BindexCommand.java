package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.BindexCreator;
import org.machanism.machai.bindex.BindexRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell command for creating and registering Bindex files using GenAI.
 * <p>
 * Provides CLI access to bindex generation and registration logic.
 * <p>
 * Usage Example:
 * 
 * <pre>
 * {@code
 * BindexCommand cmd = new BindexCommand();
 * cmd.bindex(new File("/myapp/"), true, false);
 * }
 * </pre>
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class BindexCommand {
	private static final String DEFAULT_GENAI_VALUE = "OpenAI:gpt-5-mini";

	/** JLine line reader for shell interaction. */
	@Autowired
	@Lazy
	LineReader reader;

	private PropertiesConfigurator config;

	/**
	 * Generates bindex files for the given directory using GenAI provider.
	 * 
	 * @param dir       The directory to scan for bindex creation
	 * @param update    Update mode: all saved data will be updated
	 * @param chatModel GenAI service provider/model (default is
	 *                  Ghostwriter.CHAT_MODEL)
	 * @throws IOException if scan or creation fails
	 */
	@ShellMethod("Generates bindex files.")
	public void bindex(
			@ShellOption(value = { "-d",
					"--dir" }, help = "The path to the project  directory.", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(value = { "-u",
					"--update" }, help = "The update mode: all saved data will be updated.", defaultValue = "false") boolean update,
			@ShellOption(value = { "-g",
					"--genai" }, help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`).", defaultValue = ShellOption.NULL) String chatModel)
			throws IOException {

		chatModel = Optional.ofNullable(chatModel).orElse(ConfigCommand.config.get("genai", DEFAULT_GENAI_VALUE));
		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel, config);
		BindexCreator register = new BindexCreator(provider);
		register.update(update);
		dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
		register.scanFolder(dir);
	}

	/**
	 * Registers bindex file for the given directory using GenAI provider.
	 * 
	 * @param dir    The directory to register bindex from
	 * @param update Update mode: all saved data will be updated
	 * @throws IOException if registration fails
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

		dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
		chatModel = Optional.ofNullable(chatModel).orElse(ConfigCommand.config.get("genai", DEFAULT_GENAI_VALUE));
		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel, config);
		try (BindexRegister register = new BindexRegister(provider, registerUrl)) {
			register.update(update);
			register.scanFolder(dir);
		}
	}
}
