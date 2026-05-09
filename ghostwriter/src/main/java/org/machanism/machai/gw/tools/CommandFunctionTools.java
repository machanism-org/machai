package org.machanism.machai.gw.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Installs command-execution and process-termination tools into a
 * {@link Genai}.
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

	static final String TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE = "Execution terminated by function tool.";

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(CommandFunctionTools.class);

	/**
	 * Default maximum number of characters to return from captured process output.
	 */
	private static final int DEFAULT_RESULT_TAIL_SIZE = 1024;

	/** Default character set used to decode process output streams. */
	private static final String DEFAULT_CHARSET = "UTF-8";

	/** Maximum time to wait for a started process to complete, in seconds. */
	private int processTimeoutSeconds = 600;

	/** Checker that rejects known-dangerous command fragments. */
	private CommandSecurityChecker checker;

	/**
	 * Configuration source used to resolve ${...} placeholders in commands and
	 * other tool parameters.
	 */
	private Configurator configurator;

	/**
	 * Reusable random instance used for generating lightweight command ids for
	 * logging.
	 */
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Registers system command and process control tools with the provided
	 * {@link Genai}.
	 *
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
	 * {@code DEFAULT_RESULT_TAIL_SIZE}.</li>
	 * <li><b>charsetName</b> (string, optional): Character set used to decode
	 * process output; defaults to {@code DEFAULT_CHARSET}.</li>
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
	 * <li><b>exitCode</b> (integer, optional): Exit code; defaults to 0.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param provider the provider instance to which tools will be registered
	 */
	public void applyTools(Genai provider) {
		provider.addTool(
				"run_command_line_tool",
				"Executes a system command using Java's ProcessBuilder for controlled and secure execution.\n"
						+ "Only explicitly allowed commands can be executed for security reasons.\n"
						+ "Supports setting environment variables, working directory, output tail size, and character encoding.\n"
						+ "\n"
						+ "**Shell Execution Instructions:**\n"
						+ "- For Windows, always wrap your command with `cmd /c` (e.g., `cmd /c your-command`).\n"
						+ "- For Unix/Linux, always wrap your command with `sh -c` (e.g., `sh -c 'your-command'`).\n"
						+ "- This ensures the command is executed within the appropriate system shell, enabling features like piping, "
						+ "redirection, and environment variable expansion.\n"
						+ "- If you do not use the correct shell wrapper, your command may fail or behave unexpectedly.\n"
						+ "\n"
						+ "Examples:\n"
						+ "- Windows: `cmd /c dir`\n"
						+ "- Unix/Linux: `sh -c 'ls -la | grep .java'`\n",
				this::executeCommand,
				"command:string:required:The command to execute. Must be wrapped as described above for your OS.",
				"env:string:optional:Environment variables for the subprocess, specified as NAME=VALUE pairs separated by newline (\\n). "
						+ "If omitted, the subprocess inherits the current process environment.",
				"dir:string:optional:The working directory for the subprocess. Must be a relative path within the project directory. "
						+ "If omitted, the current project directory is used.",
				"tailResultSize:integer:optional:The maximum number of characters to display from the end of the command output. "
						+ "If the output exceeds this limit, only the last tailResultSize characters are shown. Default: "
						+ DEFAULT_RESULT_TAIL_SIZE,
				"charsetName:string:optional:The character encoding to use for reading command output. Default: "
						+ DEFAULT_CHARSET);
		provider.addTool(
				"terminate_execution",
				"Terminate the application by sending an exit code.",
				this::terminateExecution,
				"message:string:optional:The exception message to use. Defaults to '"
						+ TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE + "'",
				"exitCode:integer:optional:The exit code to return when terminating the execution. Defaults to 0 if not specified.");
		provider.addTool(
				"complete_task",
				"Terminate the current task without terminating the application. This function is used to terminate an interactive session with the user within the context of the current task.",
				this::completeTask,
				"message:string:optional:The message to use upon completion.");
		provider.addTool(
				"get_previous_log_chunk",
				"Retrieves the previous chunk of log output from a command execution, immediately preceding the last returned tail result.\n"
						+ "Use this to fetch earlier log data when only the tail of the output was previously returned (e.g., for paginated log viewing or scrolling up).\n"
						+ "\n"
						+ "**Instructions:**\n"
						+ "- Specify the command execution session identifier (`commandId`).\n"
						+ "- Provide the current tail offset (the position in the log where the last tail result started).\n"
						+ "- Set the chunk size to match the previous tail size, unless a different size is desired.\n"
						+ "- The tool will return the log chunk immediately before the current tail, or as much as is available if the beginning of the log is reached.\n"
						+ "- No overlap with the current tail result is included.",
				this::getPreviousLogChunk,
				"commandId:string:required:The identifier of the command execution session.",
				"tailResultSize:integer:required:The size of the log chunk to retrieve (in characters or lines, as supported).",
				"currentTailOffset:integer:required:The offset or position in the log where the current tail result starts.",
				"charsetName:string:optional:The character encoding to use for reading log output. Default: "
						+ DEFAULT_CHARSET);
		provider.addTool(
				"get_command_log_matches",
				"Searches the command log for all text matching the provided regular expression (regexp).\n"
						+ "Use this to extract specific patterns, error messages, or any custom content from the log output of a command execution.\n"
						+ "\n"
						+ "**Instructions:**\n"
						+ "- Specify the command execution session identifier (`commandId`).\n"
						+ "- Provide a valid Java regular expression (`regexp`).\n"
						+ "- Optionally specify the character encoding for reading the log file.\n"
						+ "- The tool returns a list of all matching text segments from the log.",
				this::getCommandLogMatches,
				"commandId:string:required:The identifier of the command execution session.",
				"regexp:string:required:The Java regular expression to search for in the log.",
				"charsetName:string:optional:The character encoding to use for reading log output. Default: "
						+ DEFAULT_CHARSET);
	}

	/**
	 * Implements the {@code terminate_task} tool.
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
	public String terminateExecution(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Terminate the task: {}, {}", props, projectDir);
		}

		String message = props.has("message") ? props.get("message").asText(TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE)
				: TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE;
		int exitCode = props.has("exitCode") ? props.get("exitCode").asInt(0) : 0;

		throw new ProcessTerminationException(message, exitCode);
	}

	/**
	 * Completes the current task by throwing a {@link CompleteTask} exception.
	 * <p>
	 * This method is intended to be used as a function tool for terminating a
	 * process when requested by the user or dictated by process logic. It logs the
	 * task completion and uses a custom message if provided in the properties.
	 * </p>
	 *
	 * @param props      JSON node containing optional properties, such as a custom
	 *                   completion message.
	 * @param projectDir The project directory associated with the task.
	 * @return This method does not return normally; it always throws
	 *         {@link CompleteTask}.
	 * @throws CompleteTask Always thrown to signal task completion.
	 */
	public String completeTask(JsonNode props, File projectDir) {
		if (logger.isInfoEnabled()) {
			logger.info("Completing the task: {}, {}", props, projectDir);
		}

		String message = TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE;
		if (props != null && props.has("message")) {
			message = props.get("message").asText(null);
		}

		throw new CompleteTask(message);
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
	 * @throws IOException if the task cannot be started or I/O occurs while
	 *                     resolving paths
	 */
	public String executeCommand(JsonNode props, File projectDir) throws IOException {
		String commandId = Long.toHexString(RANDOM.nextLong());
		if (logger.isInfoEnabled()) {
			logger.info("Run shell command [{}]: {}, {}", commandId, props, projectDir);
		}

		String command = props.get("command").asText();

		command = replace(command, configurator);

		String dir = props.has("dir") ? props.get("dir").asText(".") : ".";

		File workingDir = resolveWorkingDir(projectDir, dir);
		if (workingDir == null) {
			return "Error: Invalid working directory.";
		}

		Path projectPath = projectDir.toPath().toRealPath();
		Path workingPath = workingDir.toPath().toRealPath();
		if (!workingPath.startsWith(projectPath)) {
			return "Error: Working directory must be within project directory.";
		}

		Integer tailResultSize = props.has("tailResultSize")
				? props.get("tailResultSize").asInt(DEFAULT_RESULT_TAIL_SIZE)
				: DEFAULT_RESULT_TAIL_SIZE;
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(DEFAULT_CHARSET)
				: DEFAULT_CHARSET;

		Process prc = null;
		LimitedStringBuilder output = new LimitedStringBuilder(tailResultSize, commandId, projectDir);

		try (ExecutorServiceAutoCloseable executor = new ExecutorServiceAutoCloseable(
				Executors.newFixedThreadPool(2))) {

			runDenyChecks(command);

			ProcessBuilder pb = new ProcessBuilder(CommandLineUtils.translateCommandline(command));
			pb.directory(workingDir);

			if (props.has("env")) {
				String envStr = props.get("env").asText();
				envStr = replace(envStr, configurator);
				Map<String, String> envMap = parseEnv(envStr);
				pb.environment().putAll(envMap);
			}

			final Process process = pb.start();
			prc = process;

			Future<?> stdoutFuture = executor.get()
					.submit(() -> readStream(process.getInputStream(), charsetName, output,
							line -> logger.info("[CMD {}] [STD] {}", commandId, line),
							e -> logger.error("[CMD {}] Error reading stdout", commandId, e)));

			Future<?> stderrFuture = executor.get()
					.submit(() -> readStream(process.getErrorStream(), charsetName, output,
							line -> logger.info("[CMD {}] [ERR] {}", commandId, line),
							e -> logger.error("[CMD {}] Error reading stderr", commandId, e)));

			return waitAndCollect(process, stdoutFuture, stderrFuture, output, commandId);

		} catch (DenyException e) {
			logger.error("[CMD {}] Invalid or unsafe command. {}", commandId, e.getMessage());
			return "Error: Invalid or unsafe command.";

		} catch (TimeoutException e) {
			output.append("Output reading timed out.").append(Genai.LINE_SEPARATOR);
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
		}
	}

	/**
	 * Retrieves the chunk of previously captured command output that immediately
	 * precedes the current tail window.
	 *
	 * <p>
	 * The command output is read from the persisted log file associated with the
	 * supplied {@code commandId}. The returned substring starts at
	 * {@code max(0, currentTailOffset - tailResultSize)} and ends at
	 * {@code currentTailOffset}, after being clamped to the available log length.
	 * </p>
	 *
	 * @param props      tool arguments containing {@code commandId},
	 *                   {@code tailResultSize}, {@code currentTailOffset}, and an
	 *                   optional {@code charsetName}
	 * @param projectDir project root directory used to resolve the command log file
	 * @return the previous log chunk, or an empty string if no earlier chunk exists
	 * @throws IOException if the command log path cannot be resolved
	 */
	public Object getPreviousLogChunk(JsonNode props, File projectDir) throws IOException {
		String commandId = props.get("commandId").asText();
		if (logger.isInfoEnabled()) {
			logger.info("Get log chunk [{}]: {}, {}", commandId, props, projectDir);
		}

		Integer tailResultSize = props.has("tailResultSize")
				? props.get("tailResultSize").asInt(DEFAULT_RESULT_TAIL_SIZE)
				: DEFAULT_RESULT_TAIL_SIZE;
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(DEFAULT_CHARSET)
				: DEFAULT_CHARSET;

		Integer currentTailOffset = props.get("currentTailOffset").asInt();

		if (commandId == null || tailResultSize == null || currentTailOffset == null) {
			throw new IllegalArgumentException("commandId, tailResultSize, and currentTailOffset are required.");
		}

		Path logPath = LimitedStringBuilder.getCommandLogPath(projectDir, commandId);
		if (!Files.exists(logPath)) {
			throw new IllegalArgumentException("Log file for commandId not found: " + commandId);
		}

		try {
			byte[] logBytes = Files.readAllBytes(logPath);
			String logContent = new String(logBytes, Charset.forName(charsetName));
			int start = Math.max(0, currentTailOffset - tailResultSize);
			int end = currentTailOffset;
			if (start >= end) {
				return ""; // No previous chunk available
			}

			start = start > logContent.length() - 1 ? logContent.length() - 1 : start;
			end = end > logContent.length() - 1 ? logContent.length() - 1 : end;

			return logContent.substring(start, end);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read log file: " + e.getMessage(), e);
		}
	}

	/**
	 * Searches a persisted command log for all substrings matching the supplied
	 * Java regular expression.
	 *
	 * @param props      tool arguments containing {@code commandId},
	 *                   {@code regexp}, and an optional {@code charsetName}
	 * @param projectDir project root directory used to resolve the command log file
	 * @return a list of match descriptors, each containing the matched text, start
	 *         and end offsets within the line, and the zero-based line number
	 * @throws IllegalArgumentException if required parameters are missing or the
	 *                                  log file does not exist
	 */
	public Object getCommandLogMatches(JsonNode props, File projectDir) {
		String commandId = props.has("commandId") ? props.get("commandId").asText() : "";
		String regexp = props.has("regexp") ? props.get("regexp").asText() : "";
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(DEFAULT_CHARSET)
				: DEFAULT_CHARSET;

		if (logger.isInfoEnabled()) {
			logger.info("Get command log matches [{}]: {}, {}", commandId, props, projectDir);
		}

		if (commandId.isEmpty() || regexp.isEmpty()) {
			throw new IllegalArgumentException("commandId and regexp are required.");
		}

		Path logPath = LimitedStringBuilder.getCommandLogPath(projectDir, commandId);
		if (!Files.exists(logPath)) {
			throw new IllegalArgumentException("Log file for commandId not found: " + commandId);
		}

		List<Map<String, Object>> matches = new ArrayList<>();
		Pattern pattern = Pattern.compile(regexp);

		try {
			List<String> lines = Files.readAllLines(logPath, Charset.forName(charsetName));
			int lineNumber = 0;
			for (String line : lines) {
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					Map<String, Object> matchInfo = new HashMap<>();
					matchInfo.put("text", matcher.group());
					matchInfo.put("start", matcher.start());
					matchInfo.put("end", matcher.end());
					matchInfo.put("line", lineNumber + 1);
					matches.add(matchInfo);
				}
				lineNumber++;
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to read log file: " + e.getMessage(), e);
		}

		return matches;
	}

	/**
	 * Applies deny-list checks to each token of the supplied command line.
	 *
	 * @param command the full command line
	 * @throws CommandLineException if the command cannot be tokenized
	 * @throws DenyException        if any token violates a deny-list rule
	 */
	private void runDenyChecks(String command) throws CommandLineException, DenyException {
		String[] commandParts = CommandLineUtils.translateCommandline(command);
		for (String commandPart : commandParts) {
			checker.denyCheck(commandPart);
		}
	}

	/**
	 * Waits for the process to finish (up to {@link #processTimeoutSeconds}) and
	 * then returns collected output.
	 *
	 * <p>
	 * This method also waits briefly for stdout/stderr reader tasks to complete so
	 * that output is not lost.
	 * </p>
	 *
	 * @param process      process being observed
	 * @param stdoutFuture future representing the stdout reader task
	 * @param stderrFuture future representing the stderr reader task
	 * @param output       bounded output buffer
	 * @param commandId    id used for log correlation
	 * @return collected output, followed by an exit-code line
	 * @throws InterruptedException if the current thread is interrupted while
	 *                              waiting
	 * @throws TimeoutException     if collecting output times out
	 * @throws ExecutionException   if a reader task fails
	 */
	String waitAndCollect(Process process, Future<?> stdoutFuture, Future<?> stderrFuture, LimitedStringBuilder output,
			String commandId) throws InterruptedException, TimeoutException, ExecutionException {
		boolean finished = process.waitFor(processTimeoutSeconds, TimeUnit.SECONDS);
		if (!finished) {
			process.destroyForcibly();
			output.append("Command timed out after ").append(Long.toString(processTimeoutSeconds)).append(" seconds.")
					.append(Genai.LINE_SEPARATOR);
			logger.warn("[CMD {}] Command timed out", commandId);
		}

		stdoutFuture.get(5, TimeUnit.SECONDS);
		stderrFuture.get(5, TimeUnit.SECONDS);

		int exitCode = process.exitValue();
		output.append("Command exited with code: ").append(Integer.toString(exitCode)).append(Genai.LINE_SEPARATOR);
		return output.getLastText();
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
	 * Lines are separated by {@code \n} (or {@code \n}); empty lines and lines
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
				if (key.matches("[A-Za-z_]\\w*")) {
					envMap.put(key, value);
				}
			}
		}
		return envMap;
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
				output.append(line).append(Genai.LINE_SEPARATOR);
				lineConsumer.accept(line);
			}
		} catch (IOException e) {
			errorConsumer.accept(e);
		}
	}

	/**
	 * Configures this tool set with the shared application configurator and
	 * initializes the command security checker.
	 *
	 * @param configurator configuration source used by command and environment
	 *                     processing
	 */
	@Override
	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
		try {
			checker = new CommandSecurityChecker(configurator);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Auto-closeable wrapper for {@link ExecutorService} so it can be used with
	 * try-with-resources.
	 */
	private static final class ExecutorServiceAutoCloseable implements AutoCloseable {
		private final ExecutorService executor;

		/**
		 * Creates a wrapper for the supplied executor service.
		 *
		 * @param executor executor service to expose through this closeable wrapper
		 */
		private ExecutorServiceAutoCloseable(ExecutorService executor) {
			this.executor = executor;
		}

		/**
		 * Returns the wrapped executor service.
		 *
		 * @return wrapped executor service
		 */
		private ExecutorService get() {
			return executor;
		}

		/**
		 * Shuts down the wrapped executor immediately.
		 */
		@Override
		public void close() {
			executor.shutdownNow();
		}
	}
}
