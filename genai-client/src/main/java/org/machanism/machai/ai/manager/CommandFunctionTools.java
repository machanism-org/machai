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

/**
 * Provides support for command line tool functions to be used within
 * GenAIProvider.
 * <p>
 * Allows execution of (safe) shell commands in a controlled working directory
 * for supported OS.
 * <p>
 * Usage example:
 * 
 * <pre>
 * CommandFunctionTools tools = new CommandFunctionTools(new File("/tmp"));
 * tools.applyTools(provider);
 * </pre>
 *
 * @author Viktor Tovstyi
 */
public class CommandFunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static Logger logger = LoggerFactory.getLogger(CommandFunctionTools.class);

	/**
	 * Installs supported shell command tool(s) to the specified GenAIProvider.
	 * 
	 * @param provider GenAIProvider instance
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool("run_command_line_tool",
				"Execute allowed shell commands (Linux/OSX only, some commands are denied for safety).",
				p -> executeCommand(p), "command:string:required:The command to run in the shell.");
	}

	/**
	 * Executes the supplied shell command (if supported) and returns output.
	 * 
	 * @param params JSON payload specifying the command
	 * @return Command output or error
	 * @throws IllegalArgumentException For unsupported operations or failures
	 */
	private String executeCommand(Object[] params) {
		logger.info("Run shell command: {}", params);

		String command = ((JsonNode) params[0]).get("command").asText();

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
			File workingDir = (File) params[1];
			processBuilder.directory(workingDir);
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

}
