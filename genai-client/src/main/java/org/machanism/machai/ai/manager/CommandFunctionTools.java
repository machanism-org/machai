package org.machanism.machai.ai.manager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

/**
 * Installs a command-execution tool into a {@link GenAIProvider}.
 *
 * <p>
 * The installed tool is intended to execute shell commands from a controlled
 * working directory. The actual allow/deny policy is provider- or
 * caller-defined; this class focuses on wiring the tool and running the
 * process.
 *
 * <h2>Installed tool</h2>
 * <ul>
 * <li>{@code run_command_line_tool} – executes a shell command and returns
 * stdout (and a non-zero exit code note when applicable).</li>
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
	 * <p>
	 * Expected parameters:
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
		logger.info("Run shell command: {}", Arrays.toString(params));

		String command = ((JsonNode) params[0]).get("command").asText();
		StringBuilder output = new StringBuilder();
		String os = System.getProperty("os.name").toLowerCase();
		ProcessBuilder processBuilder;
		String result;
		try {
			List<String> argList;
			if (os.contains("win")) {
				argList = Lists.asList("cmd", "/c", CommandLineUtils.translateCommandline(command));
			} else {
				argList = Lists.asList("sh", "-c", CommandLineUtils.translateCommandline(command));
			}
			processBuilder = new ProcessBuilder(argList);

			File workingDir = (File) params[1];
			processBuilder.directory(workingDir);

			Process process = processBuilder.start();

			try (InputStream inputStream = process.getInputStream();
					InputStreamReader in = new InputStreamReader(inputStream);
					BufferedReader reader = new BufferedReader(in)) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
					logger.info("Output: {}", line);
				}
			}

			int exitCode = process.waitFor();

			if (exitCode != 0) {
				ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
				process.getErrorStream().transferTo(errorOutput);
				String errorString = errorOutput.toString().replace("\0", "");
				output.append("Command exited with code: ").append(exitCode);
				if (StringUtils.isNotBlank(errorString)) {
					output.append("\r\nError output: ").append(errorString);
				}
			}

		} catch (IOException | CommandLineException | InterruptedException e) {
			Thread.currentThread().interrupt();
			result = e.getMessage();
		} finally {
			result = output.toString();
		}

		logger.debug(result);
		return result;
	}
}
