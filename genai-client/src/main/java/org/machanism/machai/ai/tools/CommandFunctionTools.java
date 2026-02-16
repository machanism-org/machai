package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
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

	/** Define allowed commands (add as needed) */
	private Set<String> allowedCommands = null;

	private int processTimeoutSeconds = 600;

	/**
	 * Exception to signal process termination with a specific exit code.
	 */
	public static class ProcessTerminationException extends RuntimeException {
		private static final long serialVersionUID = -4615360980518233932L;
		private final int exitCode;

		public ProcessTerminationException(String message, int exitCode) {
			super(message);
			this.exitCode = exitCode;
		}

		public ProcessTerminationException(String message, Throwable cause, int exitCode) {
			super(message, cause);
			this.exitCode = exitCode;
		}

		public int getExitCode() {
			return exitCode;
		}
	}

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
		provider.addTool("run_command_line_tool",
				"Executes a system command using Java's ProcessBuilder for controlled and secure execution."
						+ (allowedCommands == null ? ""
								: allowedCommands.isEmpty() ? " (Disabled)"
										: " Allowed commands: " + StringUtils.join(allowedCommands, ", "))
						+ " Only explicitly allowed commands can be executed for security reasons. "
						+ "The tool supports setting environment variables, working directory, output tail size, and character encoding.",
				this::executeCommand,
				"command:string:required:The system command to execute. Only the following commands are permitted: "
						+ (allowedCommands == null || allowedCommands.isEmpty()
								? "None (command execution is disabled)."
								: StringUtils.join(allowedCommands, ", ")),
				"env:string:optional:Environment variables for the subprocess, specified as NAME=VALUE pairs separated by newline (\\n). If omitted, the subprocess inherits the current process environment.",
				"dir:string:optional:The working directory for the subprocess. Must be a relative path within the project directory. If omitted, the current project directory is used.",
				"tailResultSize:integer:optional:The maximum number of characters to display from the end of the command output. If the output exceeds this limit, only the last tailResultSize characters are shown. Default: "
						+ defaultResultTailSize,
				"charsetName:string:optional:The character encoding to use for reading command output. Default: "
						+ defaultCharset);

		provider.addTool("terminate_process",
				"Throws an exception to immediately terminate the process. Useful for signaling fatal errors or controlled shutdowns from within a function tool. Supports specifying a custom exit code.",
				this::terminateProcess,
				"message:string:optional:The exception message to use. Defaults to 'Process terminated by function tool.'",
				"cause:string:optional:An optional cause message. If provided, it is wrapped in a new Exception as the cause.",
				"exitCode:integer:optional:The exit code to return when terminating the process. Defaults to 1 if not specified.");
	}

	/**
	 * Throws a {@link ProcessTerminationException} to signal that the function tool
	 * should terminate the process with a specific exit code.
	 * <p>
	 * The exception message, cause, and exit code can be specified via the
	 * {@code params} array:
	 * <ul>
	 * <li><b>message</b> (string, optional): The exception message. Defaults to
	 * "Process terminated by function tool."</li>
	 * <li><b>cause</b> (string, optional): The cause message. If provided, it is
	 * wrapped in a new {@link Exception} as the cause.</li>
	 * <li><b>exitCode</b> (integer, optional): The exit code to return. Defaults to
	 * 1 if not specified.</li>
	 * </ul>
	 *
	 * @param params tool invocation parameters (expects a JsonNode with optional
	 *               "message", "cause", and "exitCode" fields)
	 * @throws ProcessTerminationException always thrown to terminate the process
	 *                                     with the specified exit code
	 */
	public String terminateProcess(Object[] params) {
		JsonNode props = (JsonNode) params[0];
		String message = props.has("message") ? props.get("message").asText("Process terminated by function tool.")
				: "Process terminated by function tool.";
		String cause = props.has("cause") ? props.get("cause").asText(null) : null;
		int exitCode = props.has("exitCode") ? props.get("exitCode").asInt(1) : 1;

		if (cause != null) {
			throw new ProcessTerminationException(message, new Exception(cause), exitCode);
		} else {
			throw new ProcessTerminationException(message, exitCode);
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
	public String executeCommand(Object[] params) {
		String commandId = Integer.toHexString(new Random().nextInt());
		logger.info("Run shell command [{}]: {}", commandId, Arrays.toString(params));

		JsonNode props = (JsonNode) params[0];
		String command = props.get("command").asText();

		// Validate command: allow only whitelisted commands or patterns
		if (!isValidCommand(command)) {
			return "Error: Invalid or unsafe command.";
		}

		String dir = props.has("dir") ? props.get("dir").asText(".") : ".";

		File projectDir = (File) params[1];
		File workingDir = resolveWorkingDir(projectDir, dir);
		if (workingDir == null) {
			return "Error: Invalid working directory.";
		}

		// Validate working directory
		try {
			Path projectPath = projectDir.toPath().toRealPath();
			Path workingPath = workingDir.toPath().toRealPath();
			if (!workingPath.startsWith(projectPath)) {
				return "Error: Working directory must be within project directory.";
			}
		} catch (IOException e) {
			return "Error: Unable to resolve working directory.";
		}

		Integer tailResultSize = props.has("tailResultSize") ? props.get("tailResultSize").asInt(defaultResultTailSize)
				: defaultResultTailSize;
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(defaultCharset)
				: defaultCharset;

		// Parse command safely
		Process prc = null;
		ExecutorService executor = Executors.newFixedThreadPool(2);
		LimitedStringBuilder output = new LimitedStringBuilder(tailResultSize);

		try {
			String[] commandParts = CommandLineUtils.translateCommandline(command);

			ProcessBuilder pb = new ProcessBuilder(commandParts);
			pb.directory(workingDir);

			// Set environment variables safely
			if (props.has("env")) {
				Map<String, String> envMap = parseEnv(props.get("env").asText());
				pb.environment().putAll(envMap);
			}

			Future<?> stdoutFuture = null;
			Future<?> stderrFuture = null;

			final Process process = pb.start();
			prc = process;

			// Read stdout
			stdoutFuture = executor.submit(() -> {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream(), Charset.forName(charsetName)))) {
					String line;
					while ((line = reader.readLine()) != null) {
						output.append(line).append(System.lineSeparator());
						logger.info("[CMD {}] [OUTPUT] {}", commandId, line);
					}
				} catch (IOException e) {
					logger.error("[CMD {}] Error reading stdout", commandId, e);
				}
			});

			// Read stderr
			stderrFuture = executor.submit(() -> {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getErrorStream(), Charset.forName(charsetName)))) {
					String line;
					while ((line = reader.readLine()) != null) {
						output.append(line).append(System.lineSeparator());
						logger.error("[CMD {}] [ERROR] {}", commandId, line);
					}
				} catch (IOException e) {
					logger.error("[CMD {}] Error reading stderr", commandId, e);
				}
			});

			boolean finished = process.waitFor(processTimeoutSeconds, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				output.append("Command timed out after ").append(Long.toString(processTimeoutSeconds))
						.append(" seconds.").append(System.lineSeparator());
				logger.warn("[CMD {}] Command timed out", commandId);
			}

			// Wait for output threads to finish
			stdoutFuture.get(5, TimeUnit.SECONDS);
			stderrFuture.get(5, TimeUnit.SECONDS);

			int exitCode = process.exitValue();
			output.append("Command exited with code: ").append(Integer.toString(exitCode))
					.append(System.lineSeparator());

			return output.getLastText();

		} catch (TimeoutException e) {
			output.append("Output reading timed out.").append(System.lineSeparator());
			logger.error("[CMD {}] Output reading timed out", commandId, e);
			return output.getLastText();
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("[CMD {}] Command execution interrupted", commandId, e);
			return output.append("Interrupted: ").append(e.getMessage()).toString();
			
		} catch (IOException | ExecutionException e) {
			logger.error("[CMD {}] IO error during command execution", commandId, e);
			return output.append("IO Error: ").append(e.getMessage()).toString();
			
		} catch (CommandLineException e) {
			return "Error: " + e.getMessage();
			
		} finally {
			if (prc != null && prc.isAlive()) {
				prc.destroyForcibly();
			}
			if (executor != null) {
				executor.shutdownNow();
			}
		}
	}

	public File resolveWorkingDir(File projectDir, String dir) {
		if (projectDir == null || dir == null) {
			return null;
		}

		try {
			// Always resolve against the canonical project directory
			File baseDir = projectDir.getCanonicalFile();
			File candidate;

			if (".".equals(dir)) {
				candidate = baseDir;
			} else {
				File temp = new File(dir);
				if (temp.isAbsolute()) {
					// Reject absolute paths for security
					return null;
				}
				candidate = new File(baseDir, dir).getCanonicalFile();
			}

			// Ensure the candidate is within the project directory (no path traversal)
			Path basePath = baseDir.toPath();
			Path candidatePath = candidate.toPath();
			if (!candidatePath.startsWith(basePath)) {
				return null;
			}

			return candidate;
		} catch (IOException e) {
			// Could not resolve canonical path
			return null;
		}
	}

	public Map<String, String> parseEnv(String envString) {
		Map<String, String> envMap = new HashMap<>();
		if (envString == null || envString.isEmpty()) {
			return envMap;
		}

		String[] lines = envString.split("\\r?\\n");
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue; // Skip empty lines and comments
			}
			int idx = line.indexOf('=');
			if (idx > 0 && idx < line.length() - 1) {
				String key = line.substring(0, idx).trim();
				String value = line.substring(idx + 1).trim();
				// Optionally, validate key (e.g., only allow [A-Z_][A-Z0-9_]*)
				if (key.matches("[A-Za-z_][A-Za-z0-9_]*")) {
					envMap.put(key, value);
				}
			}
		}
		return envMap;
	}

	public boolean isValidCommand(String command) {
		if (allowedCommands == null) {
			return true;
		}

		if (command == null || command.trim().isEmpty()) {
			return false;
		}

		// Extract the base command (first word)
		String baseCommand = command.trim().split("\\s+")[0];
		return allowedCommands.contains(baseCommand);
	}
}
