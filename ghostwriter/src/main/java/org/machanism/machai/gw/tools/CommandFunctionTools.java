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
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Tool;
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
	@Tool(name = "run_sys_command", description = "Executes a system command while ensuring safe execution.\n"
			+ "Only explicitly allowed commands can be executed for security reasons.\n"
			+ "Supports setting environment variables, working directory, output tail size, and character encoding.")
	public Object executeCommand(
			@Param(name = "command", description = "The command to execute.") String command,
			@Param(name = "env", description = "Environment variables for the subprocess."
					+ "If omitted, the subprocess inherits the current process environment.", defaultValue = Param.NULL) Map<String, String> properties,
			@Param(name = "dir", description = "The working directory for the subprocess. Must be a relative path within the project directory. "
					+ "If omitted, the current project directory is used.", defaultValue = ".") String dir,
			@Param(name = "tail_result_size", description = "The maximum number of characters to display from the end of the command output. Default: "
					+ DEFAULT_RESULT_TAIL_SIZE, defaultValue = DEFAULT_RESULT_TAIL_SIZE) int tailResultSize,
			@Param(name = "charset_name", description = "The character encoding to use for reading command output. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "project_dir", description = "The project dir.") File projectDir, Configurator configurator)
			throws IOException {
		String logId = Long.toHexString(RANDOM.nextLong());
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
		LogBuilder output = new LogBuilder(tailResultSize, logId, projectDir);

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

			if (properties != null) {
				for (Map.Entry<String, String> e : properties.entrySet()) {
					String value = CommandFunctionTools.replace(e.getValue(), configurator);
					pb.environment().put(e.getKey(), value);
				}
			}

			final Process process = pb.start();
			prc = process;

			Future<?> stdoutFuture = executor.get()
					.submit(() -> readStream(process.getInputStream(), charsetName, output,
							line -> logger.info(CMD_LOG_PREFIX + "[STD] {}", logId, line),
							e -> logger.error(CMD_LOG_PREFIX + "Error reading stdout", logId, e)));

			Future<?> stderrFuture = executor.get()
					.submit(() -> readStream(process.getErrorStream(), charsetName, output,
							line -> logger.info(CMD_LOG_PREFIX + "[ERR] {}", logId, line),
							e -> logger.error(CMD_LOG_PREFIX + "Error reading stderr", logId, e)));

			Map<String, Object> report = new HashMap<>();
			Map<String, Object> logReport = waitAndCollect(process, output, logId);

			stdoutFuture.get(5, TimeUnit.SECONDS);
			stderrFuture.get(5, TimeUnit.SECONDS);

			int exitCode = process.exitValue();
			report.put("exitCode", exitCode);
			report.put("log", logReport);
			return report;

		} catch (DenyException e) {
			logger.error(CMD_LOG_PREFIX + "Invalid or unsafe command. {}", logId, e.getMessage());
			return "Error: Invalid or unsafe command.";

		} catch (TimeoutException e) {
			output.append("Output reading timed out.").append(AbstractAIProvider.LINE_SEPARATOR);
			logger.error(CMD_LOG_PREFIX + "Output reading timed out", logId, e);
			return output.getTail();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(CMD_LOG_PREFIX + "Command execution interrupted", logId, e);
			return output.append("Interrupted: ").append(e.getMessage()).toString();

		} catch (IOException | ExecutionException e) {
			logger.error(CMD_LOG_PREFIX + "IO error during command execution", logId, e);
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
	 * supplied {@code logId}. The returned substring starts at
	 * {@code max(0, currentTailOffset - tailResultSize)} and ends at
	 * {@code currentTailOffset}.
	 * </p>
	 * 
	 * @throws IOException
	 */
	@Tool(name = "get_log_chunk", description = "Extracts a log fragment from a command execution. "
			+ "Use this to retrieve earlier log data if only the end of the output was previously retrieved "
			+ "(for example, to page through the log or scroll up).")
	public Object getPreviousLogChunk(
			@Param(name = "logId", description = "The identifier of the command execution session.") String logId,
			@Param(name = "tail_result_size", description = "The size of the log fragment to extract in characters. Default: "
					+ DEFAULT_RESULT_TAIL_SIZE, defaultValue = DEFAULT_RESULT_TAIL_SIZE) int tailResultSize,
			@Param(name = "current_tail_offset", description = "The offset or position in the log where the current tail result starts.") int currentTailOffset,
			@Param(name = "charset_name", description = "The character encoding to use for reading log output. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName)
			throws IOException {

		Path logPath = LogBuilder.getCommandLogPath(logId);
		if (!Files.exists(logPath)) {
			throw new IOException("Log file for logId not found: " + logId);
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
	@Tool(name = "get_log_matches", description = "Searches the command log for all text matching the provided regular expression (regexp).\n"
			+ "Use this to extract specific patterns, error messages, or any custom content from the log output of a command execution.\n"
			+ "\n"
			+ "**Instructions:**\n"
			+ "- Specify the command execution session identifier (`logId`).\n"
			+ "- Provide a valid Java regular expression (`regexp`).\n"
			+ "- Optionally specify the character encoding for reading the log file.\n"
			+ "- The tool returns a list of all matching text segments from the log.")
	public Object getLogMatches(
			@Param(name = "logId", description = "The identifier of the command execution session.") String logId,
			@Param(name = "regexp", description = "The Java regular expression to search for in the log.") String regexp,
			@Param(name = "charset_name", description = "The character encoding to use for reading log output. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName) {

		Path logPath = LogBuilder.getCommandLogPath(logId);
		if (!Files.exists(logPath)) {
			throw new IllegalArgumentException("Log file for logId not found: " + logId);
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
	 * @param logId    id used for log correlation
	 * @return collected output, followed by an exit-code line
	 * @throws InterruptedException if the current thread is interrupted while
	 *                              waiting
	 * @throws TimeoutException     if collecting output times out
	 * @throws ExecutionException   if a reader task fails
	 */
	Map<String, Object> waitAndCollect(Process process, LogBuilder output, String logId)
			throws InterruptedException, TimeoutException, ExecutionException {
		boolean finished = process.waitFor(processTimeoutSeconds, TimeUnit.SECONDS);
		if (!finished) {
			process.destroyForcibly();
			output.append("Command timed out after ").append(Long.toString(processTimeoutSeconds)).append(" seconds.")
					.append(AbstractAIProvider.LINE_SEPARATOR);
			logger.warn(CMD_LOG_PREFIX + "Command timed out", logId);
		}

		return output.getReport();
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
	private void readStream(java.io.InputStream inputStream, String charsetName, LogBuilder output,
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
