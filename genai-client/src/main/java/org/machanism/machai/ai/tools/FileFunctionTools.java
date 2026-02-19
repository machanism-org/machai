package org.machanism.machai.ai.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
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
 * <li>{@code read_file_from_file_system} – reads a file as text</li>
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
public class FileFunctionTools implements FunctionTools {

	private static final int MAXWIDTH = 120;

	/** Logger for file tool operations and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(FileFunctionTools.class);

	private static final String defaultCharset = "UTF-8";

	/**
	 * Installs file read/write/list tools into the provided provider.
	 *
	 * @param provider provider instance
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool("read_file_from_file_system", "Read the contents of a file from the disk.", this::readFile,
				"file_path:string:required:The path to the file to be read.",
				"charsetName:string:optional:the name of the requested charset, default: " + defaultCharset);
		provider.addTool("write_file_to_file_system", "Write changes to a file on the file system.", this::writeFile,
				"file_path:string:required:The path to the file you want to write to or create.",
				"text:string:required:The content to be written into the file (text, code, etc.).",
				"charsetName:string:optional:the name of the requested charset, default: " + defaultCharset);
		provider.addTool("list_files_in_directory", "List files and directories in a specified folder.",
				this::listFiles, "dir_path:string:optional:The path to the directory to list contents of.");
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
	 * @return a newline-separated list of project-relative file paths, or a message
	 *         if no files are found
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
				String relativePath = getRelativePath(workingDir, file, true);
				content.append(relativePath).append("\n");
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
	 * Lists direct children of a directory.
	 *
	 * <p>
	 * Expected parameters:
	 * <ol>
	 * <li>{@link JsonNode} optionally containing {@code dir_path}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 *
	 * @param params tool arguments
	 * @return a comma-separated list of project-relative paths, or a message if the
	 *         directory does not exist or is empty
	 */
	private Object listFiles(Object[] params) {
		JsonNode dirNode = ((JsonNode) params[0]).get("dir_path");
		String filePath = dirNode == null ? null : dirNode.asText();
		File workingDir = (File) params[1];
		logger.info("List files: [{}, {}]", StringUtils.abbreviate(params[0].toString(), MAXWIDTH), workingDir);

		File directory = new File(workingDir, StringUtils.defaultIfBlank(filePath, "."));
		if (directory.isDirectory()) {
			File[] listFiles = directory.listFiles();
			List<String> result = new ArrayList<>();
			if (listFiles != null) {
				for (File file : listFiles) {
					String relativePath = getRelativePath(workingDir, file, true);
					result.add(relativePath);
				}

				return StringUtils.join(result, ",");
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
	 * @return success message, or an error message if writing fails
	 */
	private Object writeFile(Object[] params) {
	    String result;
	    JsonNode props = (JsonNode) params[0];
	    String filePath = props.get("file_path").asText();
	    String text = props.get("text").asText();
	    String charsetName = props.has("charsetName") ? props.get("charsetName").asText() : defaultCharset;
	    File workingDir = (File) params[1];
	    logger.info("Write file: [{}, {}]", StringUtils.abbreviate(params[0].toString(), MAXWIDTH), workingDir);
	    File file = new File(workingDir, filePath);
	    if (file.getParentFile() != null) {
	        file.getParentFile().mkdirs();
	    }
	    try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(charsetName))) {
	        writer.write(text);
	        return "File written successfully: " + filePath;
	    } catch (IOException e) {
	        result = e.getMessage();
	    }
	    return result;
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
	 * @return file content as text, or a message if the file does not exist
	 * @throws IllegalArgumentException on I/O error
	 */
	private Object readFile(Object[] params) {
		JsonNode props = (JsonNode) params[0];
		String filePath = props.get("file_path").asText();
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(defaultCharset)
				: defaultCharset;

		logger.info("Read file: {}", Arrays.toString(params));
		File workingDir = (File) params[1];
		String result;
		try (FileInputStream io = new FileInputStream(new File(workingDir, filePath))) {
			result = IOUtils.toString(io, charsetName);
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
	 * @return list of files (not directories)
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

	public static String getRelativePath(File dir, File file, boolean addSingleDot) {
		if (dir == null || file == null) {
			return null;
		}

		Path dirPath = dir.toPath().toAbsolutePath().normalize();
		Path filePath = file.toPath().toAbsolutePath().normalize();

		// If file and dir are the same, return "."
		if (dirPath.equals(filePath)) {
			return ".";
		}

		String relativePath;
		try {
			relativePath = dirPath.relativize(filePath).toString().replace("\\", "/");
		} catch (IllegalArgumentException e) {
			// file is not a descendant of dir
			return null;
		}

		// Optionally add "./" prefix
		if (addSingleDot && !relativePath.startsWith(".")) {
			relativePath = "./" + relativePath;
		}

		// If the result is empty, return "."
		if (relativePath.isEmpty()) {
			return ".";
		}

		return relativePath;
	}
}
