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
 * Installs a command-execution tool into a {@link GenAIProvider}.
 *
 * <p>The installed tool is intended to execute shell commands from a controlled working directory.
 * The actual allow/deny policy is provider- or caller-defined; this class focuses on wiring the
 * tool and running the process.
 *
 * <h2>Installed tool</h2>
 * <ul>
 * <li>{@code run_command_line_tool} – executes a shell command and returns stdout (and a non-zero exit
 * code note when applicable).</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 */
public class CommandFunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(CommandFunctionTools.class);

	/**
	 * Installs the command-line tool function into the specified provider.
	 *
	 * @param provider provider instance to augment
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool("run_command_line_tool",
				"Execute allowed shell commands (Linux/OSX only, some commands are denied for safety).",
				this::executeCommand, "command:string:required:The command to run in the shell.");
	}

	/**
	 * Executes the supplied shell command and returns its output.
	 *
	 * <p>Expected parameters:
	 * <ol>
	 * <li>{@link JsonNode} containing {@code command}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return command output
	 * @throws IllegalArgumentException if execution fails
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

			File workingDir = (File) params[1];
			processBuilder.directory(workingDir);

			Process process = processBuilder.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				output.append("Command exited with code: ").append(exitCode);
			}
		} catch (IOException | CommandLineException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalArgumentException(e);
		}
		String outputStr = output.toString();
		logger.debug(outputStr);
		return outputStr;
	}
}
