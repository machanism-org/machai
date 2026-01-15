package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ConfigCommand {

	@ShellMethod("Specifies the GenAI default value for service provider and model (e.g., `OpenAI:gpt-5.1`).")
	public void genai(@ShellOption("genai") String genai) {
		Config.setDefaultChatModel(genai);
	}

	@ShellMethod("The default path to the project directory.")
	public void dir(@ShellOption("dir") File dir) throws IOException {
		Config.setWorkingDir(dir);
	}

	@ShellMethod("Set the default value of minimum similarity threshold for search results.")
	public void score(@ShellOption("score") double score) throws IOException {
		Config.setScore(score);
	}

}
