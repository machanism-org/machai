package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import org.machanism.machai.ai.manager.GenAIProvider;
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
public class CommandFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(CommandFunctionTools.class);

	private static int defaultResultTailSize = 1024;

	private static final String defaultCharset = "UTF-8";

	/**
	 * Registers system command and process control tools with the provided
	 * {@link GenAIProvider}.
	 * <p>
	 * The following tools are installed:
	 * <ul>
	 * <li><b>run_command_line_tool</b> – Executes a system command using Java's
	 * {@code Process.exec()} method. <br>
	 * <b>Parameters:</b>
	 * <ul>
	 * <li><b>command</b> (string, required): The command to execute in the system
	 * shell.</li>
	 * <li><b>env</b> (string, optional): Environment variable settings as a single
	 * string, with each variable in the format {@code NAME=VALUE}, separated by
	 * newline characters ({@code \n}). If {@code null}, the subprocess inherits the
	 * environment variables from the current process.</li>
	 * <li><b>dir</b> (string, optional): The working directory for the subprocess.
	 * If {@code null}, the subprocess inherits the current project directory.</li>
	 * <li><b>tailResultSize</b> (integer, optional): Specifies the maximum number
	 * of characters to display from the end of the result content produced by the
	 * executed system command. If the command output exceeds this limit, only the
	 * last {@code tailResultSize} characters will be shown. Default value:
	 * {@code defaultResultTailSize}.</li>
	 * <li><b>charsetName</b> (string, optional): The name of the requested charset.
	 * Default: {@code defaultCharset}.</li>
	 * </ul>
	 * </li>
	 * <li><b>terminate_process</b> – Throws an exception to immediately terminate
	 * the process. Useful for signaling fatal errors or controlled shutdowns from
	 * within a function tool. <br>
	 * <b>Parameters:</b>
	 * <ul>
	 * <li><b>message</b> (string, optional): The exception message to use. Defaults
	 * to "Process terminated by function tool."</li>
	 * <li><b>cause</b> (string, optional): An optional cause message. If provided,
	 * it is wrapped in a new Exception as the cause.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param provider the {@link GenAIProvider} instance to which the tools will be
	 *                 registered
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool("run_command_line_tool", "Executes a system command using Java's Process.exec() method.",
				this::executeCommand, "command:string:required:The command to execute in the system shell.",
				"env:string:optional:Specifies environment variable settings as a single string, with each variable in the format NAME=VALUE, separated by newline characters (\\n). If null, the subprocess inherits the environment variables from the current process.",
				"dir:string:optional:The working directory for the subprocess. If null, the subprocess inherits the current project directory.",
				"tailResultSize:integer:optional:Specifies the maximum number of characters to display from the end of the result content produced by the executed system command. If the command output exceeds this limit, only the last tailResultSize characters will be shown. Default value: "
						+ defaultResultTailSize,
				"charsetName:string:optional:The name of the requested charset. Default: " + defaultCharset);

		provider.addTool("terminate_process",
				"Throws an exception to immediately terminate the process. Useful for signaling fatal errors or controlled shutdowns from within a function tool.",
				this::terminateProcess,
				"message:string:optional:The exception message to use. Defaults to 'Process terminated by function tool.'",
				"cause:string:optional:An optional cause message. If provided, it is wrapped in a new Exception as the cause.");
	}

	/**
	 * Throws a {@link RuntimeException} to signal that the function tool should
	 * terminate the process.
	 * <p>
	 * The exception message and cause can be specified via the {@code params}
	 * array:
	 * <ul>
	 * <li><b>message</b> (string, optional): The exception message. Defaults to
	 * "Process terminated by function tool."</li>
	 * <li><b>cause</b> (string, optional): The cause message. If provided, it is
	 * wrapped in a new {@link Exception} as the cause.</li>
	 * </ul>
	 *
	 * @param params tool invocation parameters (expects a JsonNode with optional
	 *               "message" and "cause" fields)
	 * @throws RuntimeException always thrown to terminate the process
	 */
	private String terminateProcess(Object[] params) {
		JsonNode props = (JsonNode) params[0];
		String message = props.has("message") ? props.get("message").asText("Process terminated by function tool.")
				: "Process terminated by function tool.";
		String cause = props.has("cause") ? props.get("cause").asText(null) : null;

		if (cause != null) {
			throw new RuntimeException(message, new Exception(cause));
		} else {
			throw new RuntimeException(message);
		}
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

		String dir = props.has("dir") ? props.get("dir").asText(".") : ".";
		String env = props.has("env") ? props.get("env").asText(null) : null;
		Integer tailResultSize = props.has("tailResultSize") ? props.get("tailResultSize").asInt(defaultResultTailSize)
				: defaultResultTailSize;
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(defaultCharset)
				: defaultCharset;

		File projectDir = (File) params[1];
		File workingDir;
		if (dir != null) {
			workingDir = new File(dir);
			if (!workingDir.isAbsolute()) {
				if (".".equals(dir)) {
					workingDir = projectDir;
				} else {
					workingDir = new File(projectDir, dir);
				}
			} else {
				return "Error: The specified working directory must be relative to the project path.";
			}

		} else {
			workingDir = projectDir;
		}

		LimitedStringBuilder output = new LimitedStringBuilder(tailResultSize);

		try {

			String[] envArray = null;
			if (env != null) {
				envArray = env.lines().collect(Collectors.toList()).toArray(new String[0]);
			} else {
				logger.warn("System Command Functional tool uses local environment variables.");
			}

			Process process = Runtime.getRuntime().exec(command, envArray, workingDir);

			Thread stdoutThread = new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream(), charsetName))) {
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
						new InputStreamReader(process.getErrorStream(), charsetName))) {
					String line;
					while ((line = errorReader.readLine()) != null) {
						output.append(line).append(System.lineSeparator());
						logger.error("[CMD {}] [ERROR] {}", commandId, line);
					}
				} catch (IOException e) {
					logger.error("[CMD {}] Error reading stderr", commandId, e);
				}
			});

			stdoutThread.start();
			stderrThread.start();

			stdoutThread.join();
			stderrThread.join();

			int exitCode = process.waitFor();
			output.append("Command exited with code: ").append(Integer.toString(exitCode))
					.append(System.lineSeparator());

			String resultOutput = output.getLastText();
			return resultOutput;

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
