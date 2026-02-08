package org.machanism.machai.ai.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Installs a command-execution tool into a {@link GenAIProvider}.
 *
 * <p>
 * The installed tool executes a shell command in the provider working directory
 * supplied at runtime. This class performs the process execution and output
 * capture; any command allow/deny policy must be enforced by the caller and/or
 * the hosting environment.
 *
 * <h2>Installed tool</h2>
 * <ul>
 * <li>{@code run_command_line_tool} – executes a shell command and returns
 * stdout (and stderr on failure)</li>
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
				this::executeCommand, "command:string:required:The command to run in the shell.",
				"env:array:optional:array of strings, each element of which has environment variable settings in the format name=value, "
						+ "or null if the subprocess should inherit the environment of the current process.",
				"dir:string:optional:the working directory of the subprocess, or null if the subprocess should inherit the project directory.");
	}

	/**
	 * Executes the supplied shell command and returns its output.
	 *
	 * <p>
	 * Expected parameters:
	 * <ol>
	 * <li>{@link JsonNode} containing {@code command}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return command output (stdout and, on non-zero exit, stderr)
	 */
	private String executeCommand(Object[] params) {

		String commandId = Integer.toHexString(new Random().nextInt());

		logger.info("Run shell command [{}]: {}", commandId, Arrays.toString(params));

		JsonNode props = (JsonNode) params[0];
		String command = props.get("command").asText();
		String dir = props.get("dir").asText();
		JsonNode env = props.get("env");

		File projectDir = (File) params[1];
		File workingDir;
		if (dir == null) {
			workingDir = new File(dir);
			if (!workingDir.isAbsolute()) {
				workingDir = new File(projectDir, dir);
			} else {
				return "Error: The requested working directory should be related from project path.";
			}

		} else {
			workingDir = projectDir;
		}

		// Prepare shell command
		String shellCommand;
		if (SystemUtils.IS_OS_WINDOWS) {
			shellCommand = "cmd /c " + command;
		} else {
			shellCommand = "sh -c " + command;
		}

		StringBuilder output = new StringBuilder();

		try {

			String[] envArray = null;
			if (env == null) {
				logger.warn("Potential security risk detected: a local environment variable is being used by the System Command Functional tool.");
				
				Map<String, String> envMap = System.getenv();
				envArray = envMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
						.toArray(String[]::new);
			}

			Process process = Runtime.getRuntime().exec(shellCommand, envArray, workingDir);

			Thread stdoutThread = new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
					String line;
					while ((line = reader.readLine()) != null) {
						output.append(line).append(System.lineSeparator());
						logger.info("[CMD {}] [OUTPUT] {}", commandId, line);
					}
				} catch (IOException e) {
					logger.error("[CMD {}] Error reading stdout", commandId, e);
				}
			});

			Thread stderrThread = new Thread(() -> {
				try (BufferedReader errorReader = new BufferedReader(
						new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
					String line;
					while ((line = errorReader.readLine()) != null) {
						output.append(line).append(System.lineSeparator());
						logger.error("[CMD {}] [ERROR] {}", commandId, line);
					}
				} catch (IOException e) {
					logger.error("[CMD {}] Error reading stderr", commandId, e);
				}
			});

			// Start both threads
			stdoutThread.start();
			stderrThread.start();

			// Wait for both threads to finish
			stdoutThread.join();
			stderrThread.join();

			// Wait for the process to finish
			int exitCode = process.waitFor();
			output.append("Command exited with code: ").append(exitCode).append(System.lineSeparator());
			return output.toString();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("[CMD " + commandId + "] Command execution interrupted", e);
			return output.append("Interrupted: ").append(e.getMessage()).toString();
		} catch (IOException e) {
			logger.error("[CMD " + commandId + "] IO error during command execution", e);
			return output.append("IO Error: ").append(e.getMessage()).toString();
		}
	}
}
