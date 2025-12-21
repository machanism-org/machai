package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.machai.core.BindexCreator;
import org.machanism.machai.core.BindexRegister;
import org.machanism.machai.core.ai.GenAIProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.openai.models.ChatModel;

@ShellComponent
public class BindexCommand {

	private static final ChatModel CHAT_MODEL = ChatModel.GPT_5_1;

	@Autowired
	@Lazy
	LineReader reader;

	@ShellMethod()
	public void bindex(
			@ShellOption(help = "The path to the project  directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "The update mode: all saved data will be updated.", value = "update", defaultValue = "true") boolean update,
			@ShellOption(help = "Generates only the inputs.txt file; no request is sent to OpenAI to create a bindex.", value = "inputs") boolean debug)
			throws IOException {

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		}

		GenAIProvider provider = new GenAIProvider(CHAT_MODEL);
		provider.setInputsOnly(debug);

		BindexCreator register = new BindexCreator(provider);
		register.update(update);
		register.scanProjects(dir);
	}

	@ShellMethod()
	public void register(
			@ShellOption(help = "The path to the project  directory.", value = "dir", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "The update mode: all saved data will be updated.", value = "update", defaultValue = "true") boolean update)
			throws IOException {

		if (dir == null) {
			dir = SystemUtils.getUserDir();
		}

		GenAIProvider provider = new GenAIProvider(CHAT_MODEL);
		try (BindexRegister register = new BindexRegister(provider)) {
			register.update(update);
			register.scanProjects(dir);
		}
	}
}
