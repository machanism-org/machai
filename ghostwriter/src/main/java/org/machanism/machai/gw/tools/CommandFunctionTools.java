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
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.Function;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installs command-execution and process-termination tools into a
 * {@link Genai}.
 * <p>
 * Provides tools for secure and controlled execution of system commands, log
 * retrieval, and log searching within a project context.
 * <ul>
 * <li>Executes shell commands with environment and directory restrictions.</li>
 * <li>Retrieves previous log chunks for paginated log viewing.</li>
 * <li>Searches command logs for matches to a regular expression.</li>
 * </ul>
 * <h2>Security model</h2>
 * <ul>
 * <li>Uses a deny-list heuristic check via {@link CommandSecurityChecker} to
 * block unsafe commands.</li>
 * <li>Enforces project-root confinement for working directories.</li>
 * </ul>
 * <p>
 * Tools are registered via {@link #applyTools(Genai)} and can be configured at
 * runtime using {@link #setConfigurator(Configurator)}.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class CommandFunctionTools implements FunctionTools {

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(CommandFunctionTools.class);

	private static final String CMD_LOG_PREFIX = "[CMD {}] ";

	/**
	 * Default maximum number of characters to return from captured process output.
	 */
	private static final String DEFAULT_RESULT_TAIL_SIZE = "1024";

	/** Default character set used to decode process output streams. */
	private static final String DEFAULT_CHARSET = "UTF-8";

	/** Maximum time to wait for a started process to complete, in seconds. */
	private int processTimeoutSeconds = 600;

	/**
	 * Reusable random instance used for generating lightweight command ids for
	 * logging.
	 */
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Executes a system command using Java's ProcessBuilder for controlled and
	 * secure execution.
	 * <p>
	 * Only explicitly allowed commands can be executed for security reasons.
	 * Supports setting environment variables, working directory, output tail size,
	 * and character encoding.
	 * 
	 * @param configurator
	 */
	@Function(name = "run_command_line_tool", description = "Executes a system command while ensuring safe execution.\n"
			+ "Only explicitly allowed commands can be executed for security reasons.\n"
			+ "Supports setting environment variables, working directory, output tail size, and character encoding.")
	public String executeCommand(
			@Param(name = "command", description = "The command to execute.") String command,
			@Param(name = "env", description = "Environment variables for the subprocess, specified as NAME=VALUE pairs separated by newline (\\n)."
					+ "If omitted, the subprocess inherits the current process environment.", defaultValue = "") String env,
			@Param(name = "dir", description = "The working directory for the subprocess. Must be a relative path within the project directory. "
					+ "If omitted, the current project directory is used.", defaultValue = ".") String dir,
			@Param(name = "tailResultSize", description = "The maximum number of characters to display from the end of the command output. "
					+ "If the output exceeds this limit, only the last tailResultSize characters are shown. Default: "
					+ DEFAULT_RESULT_TAIL_SIZE, defaultValue = DEFAULT_RESULT_TAIL_SIZE) int tailResultSize,
			@Param(name = "charsetName", description = "The character encoding to use for reading command output. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "projectDir", description = "The project dir.") File projectDir, Configurator configurator)
			throws IOException {
		String commandId = Long.toHexString(RANDOM.nextLong());
		command = replace(command, configurator);
		File workingDir = resolveWorkingDir(projectDir, dir);
		if (workingDir == null) {
			return "Error: Invalid working directory.";
		}

		Path projectPath = projectDir.toPath().toRealPath();
		Path workingPath = workingDir.toPath().toRealPath();
		if (!workingPath.startsWith(projectPath)) {
			return "Error: Working directory must be within project directory.";
		}

		Process prc = null;
		LimitedStringBuilder output = new LimitedStringBuilder(tailResultSize, commandId, projectDir);

		try (ExecutorServiceAutoCloseable executor = new ExecutorServiceAutoCloseable(
				Executors.newFixedThreadPool(2))) {

			if (SystemUtils.IS_OS_WINDOWS) {
				command = "cmd /c " + command;
			} else {
				command = "sh -c " + command;
			}

			String[] translateCommandline = CommandLineUtils.translateCommandline(command);

			for (String commandPart : translateCommandline) {
				CommandSecurityChecker checker = new CommandSecurityChecker(configurator);
				checker.denyCheck(commandPart);
			}

			ProcessBuilder pb = new ProcessBuilder(translateCommandline);
			pb.directory(workingDir);

			if (!env.isEmpty()) {
				Map<String, String> envMap = parseEnv(env, configurator);
				pb.environment().putAll(envMap);
			}

			final Process process = pb.start();
			prc = process;

			Future<?> stdoutFuture = executor.get()
					.submit(() -> readStream(process.getInputStream(), charsetName, output,
							line -> logger.info(CMD_LOG_PREFIX + "[STD] {}", commandId, line),
							e -> logger.error(CMD_LOG_PREFIX + "Error reading stdout", commandId, e)));

			Future<?> stderrFuture = executor.get()
					.submit(() -> readStream(process.getErrorStream(), charsetName, output,
							line -> logger.info(CMD_LOG_PREFIX + "[ERR] {}", commandId, line),
							e -> logger.error(CMD_LOG_PREFIX + "Error reading stderr", commandId, e)));

			return waitAndCollect(process, stdoutFuture, stderrFuture, output, commandId);

		} catch (DenyException e) {
			logger.error(CMD_LOG_PREFIX + "Invalid or unsafe command. {}", commandId, e.getMessage());
			return "Error: Invalid or unsafe command.";

		} catch (TimeoutException e) {
			output.append("Output reading timed out.").append(AbstractAIProvider.LINE_SEPARATOR);
			logger.error(CMD_LOG_PREFIX + "Output reading timed out", commandId, e);
			return output.getLastText();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(CMD_LOG_PREFIX + "Command execution interrupted", commandId, e);
			return output.append("Interrupted: ").append(e.getMessage()).toString();

		} catch (IOException | ExecutionException e) {
			logger.error(CMD_LOG_PREFIX + "IO error during command execution", commandId, e);
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
	 * <p>
	 * The command output is read from the persisted log file associated with the
	 * supplied {@code commandId}. The returned substring starts at
	 * {@code max(0, currentTailOffset - tailResultSize)} and ends at
	 * {@code currentTailOffset}.
	 * </p>
	 */
	@Function(name = "get_previous_log_chunk", description = "Retrieves the previous chunk of log output from a command execution, immediately preceding the last returned tail result.\n"
			+ "Use this to fetch earlier log data when only the tail of the output was previously returned (e.g., for paginated log viewing or scrolling up).\n"
			+ "\n"
			+ "**Instructions:**\n"
			+ "- Specify the command execution session identifier (`commandId`).\n"
			+ "- Provide the current tail offset (the position in the log where the last tail result started).\n"
			+ "- Set the chunk size to match the previous tail size, unless a different size is desired.\n"
			+ "- The tool will return the log chunk immediately before the current tail, or as much as is available if the beginning of the log is reached.\n"
			+ "- No overlap with the current tail result is included.")
	public Object getPreviousLogChunk(
			@Param(name = "commandId", description = "The identifier of the command execution session.") String commandId,
			@Param(name = "tailResultSize", description = "The size of the log chunk to retrieve (in characters or lines, as supported).", defaultValue = DEFAULT_RESULT_TAIL_SIZE) int tailResultSize,
			@Param(name = "currentTailOffset", description = "The offset or position in the log where the current tail result starts.") int currentTailOffset,
			@Param(name = "charsetName", description = "The character encoding to use for reading log output. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "projectDir", description = "The project dir.") File projectDir) throws IOException {

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
	 */
	@Function(name = "get_command_log_matches", description = "Searches the command log for all text matching the provided regular expression (regexp).\n"
			+ "Use this to extract specific patterns, error messages, or any custom content from the log output of a command execution.\n"
			+ "\n"
			+ "**Instructions:**\n"
			+ "- Specify the command execution session identifier (`commandId`).\n"
			+ "- Provide a valid Java regular expression (`regexp`).\n"
			+ "- Optionally specify the character encoding for reading the log file.\n"
			+ "- The tool returns a list of all matching text segments from the log.")
	public Object getCommandLogMatches(
			@Param(name = "commandId", description = "The identifier of the command execution session.") String commandId,
			@Param(name = "regexp", description = "The Java regular expression to search for in the log.") String regexp,
			@Param(name = "charsetName", description = "The character encoding to use for reading log output. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "projectDir", description = "The project dir.") File projectDir) {

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
					.append(AbstractAIProvider.LINE_SEPARATOR);
			logger.warn(CMD_LOG_PREFIX + "Command timed out", commandId);
		}

		stdoutFuture.get(5, TimeUnit.SECONDS);
		stderrFuture.get(5, TimeUnit.SECONDS);

		int exitCode = process.exitValue();
		output.append("Command exited with code: ").append(Integer.toString(exitCode))
				.append(AbstractAIProvider.LINE_SEPARATOR);
		return output.getLastText();
	}

	/**
	 * Resolves a working directory relative to a canonical project directory.
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
	 * <p>
	 * Lines are separated by {@code \n}; empty lines and lines starting with
	 * {@code #} are ignored.
	 * </p>
	 *
	 * @param envString environment string
	 * @param conf      configurator for placeholder resolution
	 * @return parsed environment variables
	 */
	public static Map<String, String> parseEnv(String envString, Configurator conf) {
		envString = replace(envString, conf);

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
				if (key.matches("[A-Za-z_]\\w*(\\.\\w+)*")) {
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
				output.append(line).append(AbstractAIProvider.LINE_SEPARATOR);
				lineConsumer.accept(line);
			}
		} catch (IOException e) {
			errorConsumer.accept(e);
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

	/**
	 * Resolves ${...} placeholders using the provided configurator.
	 * <p>
	 * Unresolvable placeholders are left as-is.
	 * </p>
	 *
	 * @param value raw value that may contain placeholders
	 * @param conf  configurator used for lookup; if {@code null}, the value is
	 *              returned unchanged
	 * @return resolved value
	 */
	public static String replace(String value, Configurator conf) {
		if (value == null || conf == null) {
			return value;
		}

		String current = value;
		for (int i = 0; i < 10; i++) {
			Properties properties = new Properties();

			Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
			Matcher matcher = pattern.matcher(current);
			while (matcher.find()) {
				String propName = matcher.group(1);
				String propValue = conf.get(propName, null);
				if (propValue != null) {
					properties.put(propName, propValue);
				}
			}

			String replaced = StringSubstitutor.replace(current, properties);
			if (replaced.equals(current) || !Strings.CS.contains(replaced, "${")) {
				return replaced;
			}
			current = replaced;
		}

		return current;
	}
}
