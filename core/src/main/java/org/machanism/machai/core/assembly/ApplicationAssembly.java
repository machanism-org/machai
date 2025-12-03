package org.machanism.machai.core.assembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class ApplicationAssembly {
	private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);

	private final String SYSTEM_INSTRUCTIONS = "You are smart software engineer and developer. "
			+ "You are expert in all popular programming languages, frameworks, platforms.\r\n"
			+ "You must implement user tasks.\r\n"
			+ "\r\n"
			+ "You MUST follow the following steps:\r\n"
			+ "1. Analyse user request to implement task.\r\n"
			+ "2. Search relevant context using repo tree and search tools. Plan your actions to implement task.\r\n"
			+ "3. Write all relevant changes in file system using file system tools (before making changes you must read content from file and generate new content).\r\n"
			+ "\r\n"
			+ "Constraints:\r\n"
			+ "1. You must implement comprehensive, correct code.\r\n"
			+ "2. You must strictly follow the plan to implement correct code.\r\n"
			+ "\r\n"
			+ "Important:\r\n"
			+ "1. You have ability to work with local file system and command line.\r\n"
			+ "2. If possible, you should use the recommended and described by bindex artifacts.";

	private GenAIProvider provider;

	public ApplicationAssembly provider(GenAIProvider provider) {
		this.provider = provider;
		return this;
	}

	public void assembly(String prompt, List<BIndex> bindexList) {
		provider.addTool("read_file_from_file_system", "Read the contents of a file from the disk.", p -> readFile(p),
				"file_path:string:required:The path to the file to be read.");
		provider.addTool("write_file_to_file_system", "Write changes to a file on the file system.", p -> writeFile(p),
				"file_path:string:required:The path to the file you want to write to or create.",
				"text:string:required:The content to be written into the file (text, code, etc.).");
		provider.addTool("list_files_in_directory", "List files and directories in a specified folder.",
				p -> listFiles(p),
				"dir_path:string:optional:The path to the directory to list contents of.");
		provider.addTool("get_recursive_file_list",
				"List files recursively in a directory (includes files in subdirectories).", p -> getRecursiveFiles(p),
				"dir_path:string:optional:Path to the folder to list contents recursively.");
		provider.addTool("run_command_line_tool",
				"Execute allowed shell commands (Linux/OSX only, some commands are denied for safety).",
				p -> executeCommand(p),
				"command:string:required:The command to run in the shell.");

		provider.prompt(SYSTEM_INSTRUCTIONS);

		try {
			URL systemResource = getClass().getResource("/schema/bindex-schema-v1.json");
			String schema = IOUtils.toString(systemResource, "UTF8");
			provider.prompt("The bindex schema https://machanism.org/machai/schema/bindex-schema-v1.json:\n" + schema);

			for (BIndex bindex : bindexList) {
				String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(bindex);
				provider.prompt("Recommended to use artifact: " + bindex.getId() + "\n"
						+ "Bindex json for this artifact:\n"
						+ "```json\n" + bindexStr + "\n```\n\n");
			}

			provider.prompt(prompt);

			provider.saveInput(new File("inputs.txt"));
			provider.perform();

		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Object getRecursiveFiles(JsonNode params) {
		JsonNode jsonNode = params.get("file_path");
		File directory;
		if (jsonNode != null) {
			String filePath = jsonNode.textValue();
			if (StringUtils.isBlank(filePath)) {
				directory = SystemUtils.getUserDir();
			} else {
				directory = new File(filePath);
			}
		} else {
			directory = SystemUtils.getUserDir();
		}

		logger.info("List files recursively: " + params);

		List<File> listFiles = listFilesRecursively(directory);
		StringBuilder content = new StringBuilder();
		for (File file : listFiles) {
			content.append(file.getAbsolutePath() + "\n");
		}

		return content.toString();
	}

	private List<File> listFilesRecursively(File directory) {
		List<File> allFiles = new ArrayList<>();

		if (directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						allFiles.add(file);
					} else if (file.isDirectory()) {
						allFiles.addAll(listFilesRecursively(file));
					}
				}
			}
		}
		return allFiles;
	}

	private Object listFiles(JsonNode params) {
		String filePath = params.get("dir_path").asText();
		File directory = new File(StringUtils.defaultIfBlank(filePath, "."));

		String result;
		if (directory.isDirectory()) {
			File[] listFiles = directory.listFiles();
			StringBuilder content = new StringBuilder();
			for (File file : listFiles) {
				content.append(file.getAbsolutePath() + "\n");
			}

			result = content.toString();
		} else {
			result = "No files found in directory.";
		}
		return result;
	}

	private Object writeFile(JsonNode params) {
		String filePath = params.get("file_path").asText();
		String text = params.get("text").asText();

		logger.info("Write file: " + params);

		File file = new File(filePath);
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}
		try (Writer writer = new FileWriter(file)) {
			IOUtils.write(text, writer);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return true;
	}

	private Object readFile(JsonNode params) {
		String filePath = params.get("file_path").asText();

		logger.info("Read file: " + params);

		try (FileInputStream io = new FileInputStream(new File(filePath))) {
			return IOUtils.toString(io, "UTF8");
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String executeCommand(JsonNode params) {
		logger.info("Run shell command: " + params);

		String command = params.get("command").asText();

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

			Process process;
			process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
				output.append("\n");
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				output.append("Command exited with code: " + exitCode);
			}
		} catch (IOException | CommandLineException | InterruptedException e) {
			throw new IllegalArgumentException(e);
		}

		String outputStr = output.toString();
		logger.info(outputStr);
		return outputStr;
	}

}
