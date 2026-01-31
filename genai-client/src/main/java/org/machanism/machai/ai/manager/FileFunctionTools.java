package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Installs file-system tools into a {@link GenAIProvider}.
 *
 * <p>
 * The installed tools operate relative to a working directory supplied by the
 * provider/tool runtime.
 *
 * <h2>Installed tools</h2>
 * <ul>
 * <li>{@code read_file_from_file_system} – reads a file as UTF-8 text</li>
 * <li>{@code write_file_to_file_system} – writes a file (creating parent
 * directories as needed)</li>
 * <li>{@code list_files_in_directory} – lists immediate children of a
 * directory</li>
 * <li>{@code get_recursive_file_list} – recursively lists all files under a
 * directory</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 */
public class FileFunctionTools {

	private static final int MAXWIDTH = 120;

	/** Logger for file tool operations and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(FileFunctionTools.class);

	/**
	 * Installs file read/write/list tools into the provided provider.
	 *
	 * @param provider provider instance
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool("read_file_from_file_system", "Read the contents of a file from the disk.", this::readFile,
				"file_path:string:required:The path to the file to be read.");
		provider.addTool("write_file_to_file_system", "Write changes to a file on the file system.", this::writeFile,
				"file_path:string:required:The path to the file you want to write to or create.",
				"text:string:required:The content to be written into the file (text, code, etc.).");
		provider.addTool("list_files_in_directory", "List files and directories in a specified folder.",
				this::listFiles,
				"dir_path:string:optional:The path to the directory to list contents of.");
		provider.addTool("get_recursive_file_list",
				"List files recursively in a directory (includes files in subdirectories).", this::getRecursiveFiles,
				"dir_path:string:optional:Path to the folder to list contents recursively.");
	}

	/**
	 * Lists files recursively in a directory.
	 *
	 * <p>
	 * Expected parameters:
	 * <ol>
	 * <li>{@link JsonNode} optionally containing {@code dir_path}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return a newline-separated list of absolute file paths
	 */
	private Object getRecursiveFiles(Object[] params) {
		String result;
		File workingDir = (File) params[1];
		JsonNode jsonNode = ((JsonNode) params[0]).get("dir_path");
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
		List<File> listFiles = listFilesRecursively(directory);
		StringBuilder content = new StringBuilder();
		if (!listFiles.isEmpty()) {
			for (File file : listFiles) {
				String relatedPath = ProjectLayout.getRelatedPath(workingDir, file, true);
				content.append(relatedPath).append("\n");
			}
		} else {
			content.append("No files found in directory.");
		}
		result = content.toString();
		logger.info("List files recursively: {}, Result: {}", Arrays.toString(params),
				StringUtils.abbreviate(result, 60).replace("\n", ""));
		return result;
	}

	/**
	 * Lists files in a directory.
	 *
	 * <p>
	 * Expected parameters:
	 * <ol>
	 * <li>{@link JsonNode} containing {@code dir_path}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return a newline-separated list of absolute paths for directory children
	 */
	private Object listFiles(Object[] params) {
		JsonNode dirNode = ((JsonNode) params[0]).get("dir_path");
		String filePath = dirNode == null ? null : dirNode.asText();
		File workingDir = (File) params[1];
		File directory = new File(workingDir, StringUtils.defaultIfBlank(filePath, "."));
		if (directory.isDirectory()) {
			File[] listFiles = directory.listFiles();
			StringBuilder content = new StringBuilder();
			if (listFiles != null) {
				for (File file : listFiles) {
					String relatedPath = ProjectLayout.getRelatedPath(workingDir, file, false);
					content.append(relatedPath).append("\n");
				}

				return content.toString();
			}
		}

		return "No files found in directory.";
	}

	/**
	 * Writes content to a file in the working directory.
	 *
	 * <p>
	 * Expected parameters:
	 * <ol>
	 * <li>{@link JsonNode} containing {@code file_path} and {@code text}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return {@code true} if successful
	 * @throws IllegalArgumentException on I/O error
	 */
	private Object writeFile(Object[] params) {
		String filePath = ((JsonNode) params[0]).get("file_path").asText();
		String text = ((JsonNode) params[0]).get("text").asText();
		logger.info("Write file: {}", StringUtils.abbreviate(Arrays.toString(params), MAXWIDTH));
		File workingDir = (File) params[1];
		File file = new File(workingDir, filePath);
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

	/**
	 * Reads content from a file in the working directory.
	 *
	 * <p>
	 * Expected parameters:
	 * <ol>
	 * <li>{@link JsonNode} containing {@code file_path}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return file content as UTF-8 text, or a message if the file does not exist
	 * @throws IllegalArgumentException on I/O error
	 */
	private Object readFile(Object[] params) {
		String filePath = ((JsonNode) params[0]).get("file_path").asText();
		logger.info("Read file: {}", Arrays.toString(params));
		File workingDir = (File) params[1];
		String result;
		try (FileInputStream io = new FileInputStream(new File(workingDir, filePath))) {
			result = IOUtils.toString(io, "UTF8");
		} catch (FileNotFoundException e) {
			result = "File not found.";
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return result;
	}

	/**
	 * Helper to collect all files recursively.
	 *
	 * @param directory directory to start with
	 * @return list of files
	 */
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

}
