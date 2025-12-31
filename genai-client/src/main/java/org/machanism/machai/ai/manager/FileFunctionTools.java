package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class FileFunctionTools {

	private static Logger logger = LoggerFactory.getLogger(FileFunctionTools.class);
	private File workingDir;

	public FileFunctionTools(File workingDir) {
		super();
		this.setWorkingDir(workingDir);
	}

	public void applyTools(GenAIProvider provider) {
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
	}

	private Object getRecursiveFiles(JsonNode params) {

		File workingDir = getWorkingDir();

		JsonNode jsonNode = params.get("dir_path");
		File directory;

		if (jsonNode != null) {
			String filePath = jsonNode.textValue();
			if (StringUtils.isBlank(filePath)) {
				directory = workingDir;
			} else {
				directory = new File(workingDir, filePath);
			}
		} else {
			directory = workingDir;
		}

		logger.info("List files recursively: {}", params);

		List<File> listFiles = listFilesRecursively(directory);
		StringBuilder content = new StringBuilder();
		if (!listFiles.isEmpty()) {
			for (File file : listFiles) {
				content.append(file.getAbsolutePath() + "\n");
			}
		} else {
			content.append("No files found in directory.");
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
		File directory = new File(getWorkingDir(), StringUtils.defaultIfBlank(filePath, "."));

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

		logger.info("Write file: {}", StringUtils.abbreviate(params.toString(), 80));

		File file = new File(getWorkingDir(), filePath);
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

		logger.info("Read file: {}", params);

		try (FileInputStream io = new FileInputStream(new File(getWorkingDir(), filePath))) {
			return IOUtils.toString(io, "UTF8");
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public File getWorkingDir() {
		if (workingDir == null) {
			throw new IllegalArgumentException("The function tool working dir is not defined.");
		}
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}
}
