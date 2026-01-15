package org.machanism.machai.cli;

import java.io.File;

import org.machanism.machai.Config;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ConfigCommand {

	@ShellMethod("Specifies the GenAI default value for service provider and model (e.g., `OpenAI:gpt-5.1`).")
	public void genai(@ShellOption(value = "genai", defaultValue = ShellOption.NULL) String genai) {
		Config.setDefaultChatModel(genai);
	}

	@ShellMethod("The default path to the project directory.")
	public void dir(@ShellOption(value = "dir", defaultValue = ShellOption.NULL) File dir) {
		Config.setWorkingDir(dir);
	}

	@ShellMethod("Set the default value of minimum similarity threshold for search results.")
	public void score(@ShellOption(value = "score", defaultValue = ShellOption.NULL) Double score) {
		Config.setScore(score);
	}

	@ShellMethod("Show configuration properties.")
	public void conf() {
		Config.setWorkingDir(null);
		Config.setDefaultChatModel(null);
		Config.setScore(null);
	}

}
