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

@ShellComponent
public class BindexCommand {

	private static final String CHAT_MODEL = "OpenAI:gpt-5.1";

	@Autowired
	@Lazy
	LineReader reader;

	@ShellMethod("Generates bindex files.")
	public void bindex(
			@ShellOption(help = "The path to the project  directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "The update mode: all saved data will be updated.", value = "update", defaultValue = "true") boolean update,
			@ShellOption(help = "Generates only the inputs.txt file; no request is sent to OpenAI to create a bindex.", value = "inputs") boolean inputs)
			throws IOException {

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		}

		GenAIProvider provider = GenAIProviderManager.getProvider(inputs ? "Non" : CHAT_MODEL);

		BindexCreator register = new BindexCreator(provider, !inputs);
		register.update(update);
		register.scanFolder(dir);
	}

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
