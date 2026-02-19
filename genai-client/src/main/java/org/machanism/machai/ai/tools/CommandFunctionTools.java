package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
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
 * Installs command-execution and process-termination tools into a
 * {@link GenAIProvider}.
 *
 * <p>
 * The primary tool executes a command line using {@link ProcessBuilder} in a
 * controlled working directory and captures stdout/stderr into a bounded buffer
 * (see {@link LimitedStringBuilder}). Hosts may optionally provide an
 * allow-list via {@link #isValidCommand(String)} semantics to restrict
 * execution.
 *
 * <h2>Installed tools</h2>
 * <ul>
 * <li>{@code run_command_line_tool} – executes a command line and returns
 * output</li>
 * <li>{@code terminate_process} – throws an exception to abort execution</li>
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
	 * Allow-list for executable base commands.
	 * <p>
	 * If {@code null}, all commands are considered allowed. If empty, command
	 * execution is effectively disabled.
	 */
	private Set<String> allowedCommands = null;

	/** Maximum time to wait for a started process to complete. */
	private int processTimeoutSeconds = 600;

	private CommandSecurityChecker checker;

	/**
	 * Exception to signal process termination with a specific exit code.
	 */
	public static class ProcessTerminationException extends RuntimeException {
		private static final long serialVersionUID = -4615360980518233932L;
		private final int exitCode;

		/**
		 * Creates a termination exception.
		 *
		 * @param message  message to expose to the host
		 * @param exitCode desired process exit code
		 */
		public ProcessTerminationException(String message, int exitCode) {
			super(message);
			this.exitCode = exitCode;
		}

		/**
		 * Creates a termination exception.
		 *
		 * @param message  message to expose to the host
		 * @param cause    underlying cause
		 * @param exitCode desired process exit code
		 */
		public ProcessTerminationException(String message, Throwable cause, int exitCode) {
			super(message, cause);
			this.exitCode = exitCode;
		}

		/**
		 * Returns the desired exit code.
		 *
		 * @return exit code
		 */
		public int getExitCode() {
			return exitCode;
		}
	}

	public CommandFunctionTools() {
		super();
		try {
			checker = new CommandSecurityChecker("denylist-windows.txt", "denylist-unix.txt");
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
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
	 * *
	 * 
	 * @param params tool invocation parameters (expects a {@link JsonNode} with
	 *               optional {@code message}, {@code cause}, and {@code exitCode}
	 *               fields)
	 * @return never returns; always throws
	 * @throws ProcessTerminationException always thrown to terminate the process
	 */
	public String terminateProcess(Object[] params) {
		JsonNode props = (JsonNode) params[0];
		String message = props.has("message") ? props.get("message").asText("Process terminated by function tool.")
				: "Process terminated by function tool.";
		String cause = props.has("cause") ? props.get("cause").asText(null) : null;
		int exitCode = props.has("exitCode") ? props.get("exitCode").asInt(1) : 1;

		if (cause != null) {
			throw new ProcessTerminationException(message, new Exception(cause), exitCode);
		}
		throw new ProcessTerminationException(message, exitCode);
	}

	/**
	 * Executes the supplied command and returns the captured output.
	 *
	 * <p>
	 * Expected parameters:
	 * <ol>
	 * <li>{@link JsonNode} containing {@code command} and optional settings</li>
	 * <li>{@link File} project working directory supplied by the provider
	 * runtime</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return command output (bounded to the configured tail size)
	 */
	public String executeCommand(Object[] params) {
		String commandId = Integer.toHexString(new Random().nextInt());
		logger.info("Run shell command [{}]: {}", commandId, Arrays.toString(params));

		JsonNode props = (JsonNode) params[0];
		String command = props.get("command").asText();

		if (!isValidCommand(command)) {
			return "Error: Invalid or unsafe command.";
		}

		String dir = props.has("dir") ? props.get("dir").asText(".") : ".";

		File projectDir = (File) params[1];
		File workingDir = resolveWorkingDir(projectDir, dir);
		if (workingDir == null) {
			return "Error: Invalid working directory.";
		}

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

		Process prc = null;
		ExecutorService executor = Executors.newFixedThreadPool(2);
		LimitedStringBuilder output = new LimitedStringBuilder(tailResultSize);

		try {
			String[] commandParts = CommandLineUtils.translateCommandline(command);

			ProcessBuilder pb = new ProcessBuilder(commandParts);
			pb.directory(workingDir);

			if (props.has("env")) {
				Map<String, String> envMap = parseEnv(props.get("env").asText());
				pb.environment().putAll(envMap);
			}

			final Process process = pb.start();
			prc = process;

			Future<?> stdoutFuture = executor.submit(() -> readStream(process.getInputStream(), charsetName, output,
					line -> logger.info("[CMD {}] [OUTPUT] {}", commandId, line),
					e -> logger.error("[CMD {}] Error reading stdout", commandId, e)));

			Future<?> stderrFuture = executor.submit(() -> readStream(process.getErrorStream(), charsetName, output,
					line -> logger.error("[CMD {}] [ERROR] {}", commandId, line),
					e -> logger.error("[CMD {}] Error reading stderr", commandId, e)));

			boolean finished = process.waitFor(processTimeoutSeconds, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				output.append("Command timed out after ").append(Long.toString(processTimeoutSeconds))
						.append(" seconds.").append(System.lineSeparator());
				logger.warn("[CMD {}] Command timed out", commandId);
			}

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
			executor.shutdownNow();
		}
	}

	/**
	 * Resolves a working directory relative to a canonical project directory.
	 *
	 * <p>
	 * Absolute paths are rejected and attempts to traverse outside the project
	 * directory are blocked.
	 *
	 * @param projectDir canonical project directory
	 * @param dir        requested relative directory (or {@code .})
	 * @return resolved directory, or {@code null} if invalid
	 */
	public File resolveWorkingDir(File projectDir, String dir) {
		if (projectDir == null || dir == null) {
			return null;
		}

		try {
			File baseDir = projectDir.getCanonicalFile();
			File candidate;

			if (".".equals(dir)) {
				candidate = baseDir;
			} else {
				File temp = new File(dir);
				if (temp.isAbsolute()) {
					return null;
				}
				candidate = new File(baseDir, dir).getCanonicalFile();
			}

			Path basePath = baseDir.toPath();
			Path candidatePath = candidate.toPath();
			if (!candidatePath.startsWith(basePath)) {
				return null;
			}

			return candidate;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Parses an {@code env} parameter string into a map.
	 *
	 * <p>
	 * Lines are separated by {@code \n} (or {@code \r\n}); empty lines and lines
	 * starting with {@code #} are ignored.
	 *
	 * @param envString environment string
	 * @return parsed environment variables
	 */
	public Map<String, String> parseEnv(String envString) {
		Map<String, String> envMap = new HashMap<>();
		if (envString == null || envString.isEmpty()) {
			return envMap;
		}

		String[] lines = envString.split("\\r?\\n");
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			int idx = line.indexOf('=');
			if (idx > 0 && idx < line.length() - 1) {
				String key = line.substring(0, idx).trim();
				String value = line.substring(idx + 1).trim();
				if (key.matches("[A-Za-z_][A-Za-z0-9_]*")) {
					envMap.put(key, value);
				}
			}
		}
		return envMap;
	}

	/**
	 * Validates a command against the configured allow-list.
	 *
	 * @param command raw command line string
	 * @return {@code true} if allowed
	 */
	public boolean isValidCommand(String command) {

		if (checker.isDangerous(command)) {
			return false;
		}

		if (allowedCommands == null) {
			return true;
		}

		if (command == null || command.trim().isEmpty()) {
			return false;
		}

		String baseCommand = command.trim().split("\\s+")[0];
		return allowedCommands.contains(baseCommand);
	}

	private interface LineConsumer {
		void accept(String line);
	}

	private interface ErrorConsumer {
		void accept(IOException e);
	}

	private void readStream(java.io.InputStream inputStream, String charsetName, LimitedStringBuilder output,
			LineConsumer lineConsumer, ErrorConsumer errorConsumer) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(inputStream, Charset.forName(charsetName)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append(System.lineSeparator());
				lineConsumer.accept(line);
			}
		} catch (IOException e) {
			errorConsumer.accept(e);
		}
	}
}
