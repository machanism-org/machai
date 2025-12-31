package org.machanism.machai.ai.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

public class CommandFunctionTools {

	private static Logger logger = LoggerFactory.getLogger(CommandFunctionTools.class);
	private File workingDir;

	public CommandFunctionTools(File workingDir) {
		super();
		this.setWorkingDir(workingDir);
	}

	public void applyTools(GenAIProvider provider) {
		provider.addTool("run_command_line_tool",
				"Execute allowed shell commands (Linux/OSX only, some commands are denied for safety).",
				p -> executeCommand(p),
				"command:string:required:The command to run in the shell.");
	}

	private String executeCommand(JsonNode params) {
		logger.info("Run shell command: {}", params);

		String command = params.get("command").asText();

		StringBuilder output = new StringBuilder();
		String os = System.getProperty("os.name").toLowerCase();
		ProcessBuilder processBuilder;

		try {
			if (os.contains("win")) {
				List<String> argList = Lists.asList("wsl.exe", CommandLineUtils.translateCommandline(command));
				processBuilder = new ProcessBuilder(argList);
			} else {
				List<String> argList = Lists.asList("sh", "-c", CommandLineUtils.translateCommandline(command));
				processBuilder = new ProcessBuilder(argList);
			}

			Process process;
			processBuilder.directory(getWorkingDir());
			process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
				output.append("\n");
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				output.append("Command exited with code: " + exitCode);
			}
		} catch (IOException | CommandLineException | InterruptedException e) {
			throw new IllegalArgumentException(e);
		}

		String outputStr = output.toString();
		logger.debug(outputStr);
		return outputStr;
	}

	public File getWorkingDir() {
		if (workingDir == null) {
			throw new IllegalArgumentException("The function tool working dir is not defined.");
		}
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}
}
