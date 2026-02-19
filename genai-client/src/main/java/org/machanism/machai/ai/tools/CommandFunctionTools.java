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
 * The installed tools provide a controlled wrapper around
 * {@link ProcessBuilder} to execute a host command line within a validated
 * working directory and capture both stdout and stderr into a bounded buffer
 * (see {@link LimitedStringBuilder}).
 * </p>
 *
 * <h2>Security model</h2>
 * <p>
 * Command execution is intended to be restricted by the host application. This
 * implementation supports:
 * </p>
 * <ul>
 * <li>A deny-list heuristic check via {@link CommandSecurityChecker}</li>
 * <li>An optional allow-list via {@link #isValidCommand(String)}</li>
 * <li>Project-root confinement for the working directory (see
 * {@link #resolveWorkingDir(File, String)})</li>
 * </ul>
 *
 * <h2>Installed tools</h2>
 * <ul>
 * <li>{@code run_command_line_tool} – executes a command line and returns
 * output</li>
 * <li>{@code terminate_process} – aborts execution by throwing a
 * {@link ProcessTerminationException}</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 */
public class CommandFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(CommandFunctionTools.class);

	/**
	 * Default maximum number of characters to return from captured process output.
	 */
	private static int defaultResultTailSize = 1024;

	/** Default character set used to decode process output streams. */
	private static final String defaultCharset = "UTF-8";

	/**
	 * Allow-list for executable base commands.
	 * <p>
	 * If {@code null}, all commands are considered allowed (subject to deny-list
	 * checks). If empty, command execution is effectively disabled.
	 */
	private Set<String> allowedCommands = null;

	/** Maximum time to wait for a started process to complete. */
	private int processTimeoutSeconds = 600;

	/** Checker that rejects known-dangerous command fragments. */
	private CommandSecurityChecker checker;

	/**
	 * Runtime exception used by {@code terminate_process} to signal early
	 * termination to the host.
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

	/**
	 * Creates a tool installer and initializes the default deny-lists.
	 *
	 * @throws IllegalArgumentException if deny-list resources cannot be loaded
	 */
	public CommandFunctionTools() {
		super();
		try {
			checker = new CommandSecurityChecker();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Registers system command and process control tools with the provided
	 * {@link GenAIProvider}.
	 * <p>
	 * The following tools are installed:
	 * </p>
	 * <ul>
	 * <li><b>run_command_line_tool</b> – Executes a system command using Java's
	 * {@link ProcessBuilder}.<br>
	 * <b>Parameters:</b>
	 * <ul>
	 * <li><b>command</b> (string, required): The command to execute.</li>
	 * <li><b>env</b> (string, optional): Environment variables as
	 * {@code NAME=VALUE} pairs separated by newline characters ({@code \n}).</li>
	 * <li><b>dir</b> (string, optional): Working directory relative to the project
	 * directory; defaults to {@code .}.</li>
	 * <li><b>tailResultSize</b> (integer, optional): Maximum number of characters
	 * to return from captured output; defaults to
	 * {@code defaultResultTailSize}.</li>
	 * <li><b>charsetName</b> (string, optional): Character set used to decode
	 * process output; defaults to {@code defaultCharset}.</li>
	 * </ul>
	 * </li>
	 * <li><b>terminate_process</b> – Throws an exception to immediately terminate
	 * execution.<br>
	 * <b>Parameters:</b>
	 * <ul>
	 * <li><b>message</b> (string, optional): Exception message; defaults to
	 * "Process terminated by function tool."</li>
	 * <li><b>cause</b> (string, optional): Optional cause message; wrapped in an
	 * {@link Exception}.</li>
	 * <li><b>exitCode</b> (integer, optional): Exit code; defaults to 1.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param provider the provider instance to which tools will be registered
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
	 * Implements the {@code terminate_process} tool.
	 *
	 * <p>
	 * Reads {@code message}, {@code cause}, and {@code exitCode} from the supplied
	 * {@link JsonNode} and throws a {@link ProcessTerminationException}. This
	 * mechanism allows a tool invocation to abort the overall workflow with an
	 * explicit exit code.
	 * </p>
	 *
	 * @param params tool invocation parameters (expects a single {@link JsonNode}
	 *               argument)
	 * @return never returns; always throws
	 * @throws ProcessTerminationException always thrown to terminate execution
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
	 * Implements the {@code run_command_line_tool} tool.
	 *
	 * <p>
	 * Expected parameters:
	 * </p>
	 * <ol>
	 * <li>{@link JsonNode} containing {@code command} and optional settings</li>
	 * <li>{@link File} project working directory supplied by the provider
	 * runtime</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return command output bounded to the configured tail size
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
	 * </p>
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
	 * Parses the {@code env} parameter string into a map of environment variables.
	 *
	 * <p>
	 * Lines are separated by {@code \n} (or {@code \r\n}); empty lines and lines
	 * starting with {@code #} are ignored.
	 * </p>
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
	 * Validates the provided command line against configured security policies.
	 *
	 * <p>
	 * This method applies the deny-list check (via {@link CommandSecurityChecker})
	 * and then enforces the optional allow-list.
	 * </p>
	 *
	 * @param command raw command line string
	 * @return {@code true} if the command is allowed; {@code false} otherwise
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

	/**
	 * Functional interface used for streaming output line processing.
	 */
	private interface LineConsumer {
		/**
		 * Consumes a single line of output.
		 *
		 * @param line output line
		 */
		void accept(String line);
	}

	/**
	 * Functional interface used for handling stream read failures.
	 */
	private interface ErrorConsumer {
		/**
		 * Called when an {@link IOException} occurs while reading a stream.
		 *
		 * @param e exception
		 */
		void accept(IOException e);
	}

	/**
	 * Reads a process stream and appends its content to {@code output} while also
	 * passing each line to the provided consumer.
	 *
	 * @param inputStream   process stream
	 * @param charsetName   stream character set
	 * @param output        bounded output buffer
	 * @param lineConsumer  callback invoked for each line read
	 * @param errorConsumer callback invoked if reading fails
	 */
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
