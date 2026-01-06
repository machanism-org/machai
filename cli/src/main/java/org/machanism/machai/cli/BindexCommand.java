package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
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
 * <pre>
 * {@code
 * BindexCommand cmd = new BindexCommand();
 * cmd.bindex(new File("/myapp/"), true, false);
 * }
 * </pre>
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class BindexCommand {

	private static final String CHAT_MODEL = "OpenAI:gpt-5.1";

	/** JLine line reader for shell interaction. */
	@Autowired
	@Lazy
	LineReader reader;

	/**
	 * Generates bindex files for the given directory using GenAI provider.
	 * @param dir The directory to scan for bindex creation
	 * @param update Update mode: all saved data will be updated
	 * @param inputs Debug mode: only generates inputs.txt, no AI requests
	 * @throws IOException if scan or creation fails
	 */
	@ShellMethod("Generates bindex files.")
	public void bindex(
			@ShellOption(help = "The path to the project  directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "The update mode: all saved data will be updated.", value = "update", defaultValue = "true") boolean update,
			@ShellOption(help = "Generates only the inputs.txt file; no request is sent to OpenAI to create a bindex.", value = "inputs") boolean inputs)
			throws IOException {

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		}

		GenAIProvider provider = GenAIProviderManager.getProvider(inputs ? null : CHAT_MODEL);

		BindexCreator register = new BindexCreator(provider, !inputs);
		register.update(update);
		register.scanFolder(dir);
	}

	/**
	 * Registers bindex file for the given directory using GenAI provider.
	 * @param dir The directory to register bindex from
	 * @param update Update mode: all saved data will be updated
	 * @throws IOException if registration fails
	 */
	@ShellMethod("Registers bindex file.")
	public void register(
			@ShellOption(help = "The path to the project  directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "The update mode: all saved data will be updated.", value = "update", defaultValue = "true") boolean update)
			throws IOException {

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		}

		GenAIProvider provider = GenAIProviderManager.getProvider(CHAT_MODEL);
		try (BindexRegister register = new BindexRegister(provider)) {
			register.update(update);
			register.scanFolder(dir);
		}
	}
}
