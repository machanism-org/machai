package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Installs file-system tools into a {@link GenAIProvider}.
 *
 * <p>
 * Tools in this installer are intended for host-integrated use where the host
 * controls the base working directory. All paths provided to these tools are
 * interpreted relative to the working directory supplied by the
 * provider/runtime.
 * </p>
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

	private static final int MAXWIDTH = 160;

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final String CHARSET_NAME_FIELD = "charsetName";

	/** Logger for file tool operations and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(FileFunctionTools.class);

	/**
	 * Registers file read/write/list tools into the provided provider.
	 *
	 * @param provider provider instance
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool("read_file_from_file_system", "Read the contents of a file from the disk.", this::readFile,
				"file_path:string:required:The path to the file to be read.",
				"charsetName:string:optional:the name of the requested charset, default: " + DEFAULT_CHARSET);
		provider.addTool(
				"write_file_to_file_system",
				"Write changes to a file on the file system, either by replacing content at specific positions or writing the full content.",
				this::writeFile,
				"file_path:string:required:The path to the file you want to write to or create.",
				"text:string:required:The content to be written into the file or used as replacement.",
				"start_position:integer:optional:The starting position in the file where the text should replace existing content. Default: -1 (no replacement).",
				"end_position:integer:optional:The ending position in the file where the text should replace existing content. Default: -1 (no replacement).",
				"charsetName:string:optional:The name of the requested charset. Default: " + DEFAULT_CHARSET);
		provider.addTool("list_files_in_directory", "List files and directories in a specified folder.",
				this::listFiles, "dir_path:string:optional:The path to the directory to list contents of.");
		provider.addTool("get_recursive_file_list",
				"List files recursively in a directory (includes files in subdirectories).", this::getRecursiveFiles,
				"dir_path:string:optional:Path to the folder to list contents recursively.");
	}

	/**
	 * Implements {@code get_recursive_file_list}.
	 *
	 * <p>
	 * Expected parameters:
	 * </p>
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
				content.append(relativePath).append(StringUtils.LF);
			}
		} else {
			content.append("No files found in directory.");
		}
		result = content.toString();
		if (logger.isInfoEnabled()) {
			logger.info("List files recursively: {}, Result: {}", Arrays.toString(params),
					StringUtils.abbreviate(result, 60).replace(StringUtils.LF, ""));
		}
		logger.debug("List files recursively: {}, Result: {}", Arrays.toString(params), result);
		return result;
	}

	/**
	 * Implements {@code list_files_in_directory}.
	 *
	 * <p>
	 * Expected parameters:
	 * </p>
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
		if (logger.isInfoEnabled()) {
			logger.info("List files: [{}, {}]", StringUtils.abbreviate(String.valueOf(params[0]), MAXWIDTH),
					workingDir);
		}
		logger.debug("List files: [{}, {}]", params[0], workingDir);

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
	 * Implements {@code write_file_to_file_system}.
	 *
	 * <p>
	 * Expected parameters:
	 * </p>
	 * <ol>
	 * <li>{@link JsonNode} containing {@code file_path}, {@code text},
	 * {@code start_position}, and {@code end_position}</li>
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
		String charsetName = props.has(CHARSET_NAME_FIELD) ? props.get(CHARSET_NAME_FIELD).asText() : DEFAULT_CHARSET;
		File workingDir = (File) params[1];

		if (logger.isInfoEnabled()) {
			logger.info("Write file: [{}, {}]", StringUtils.abbreviate(String.valueOf(params[0]), MAXWIDTH),
					workingDir);
		}
		logger.debug("Write file: [{}, {}]", params[0], workingDir);

		File file = new File(workingDir, filePath);
		try {
			if (file.exists()) {
				// Read the existing content of the file
				StringBuilder fileContent = new StringBuilder();
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file), Charset.forName(charsetName)))) {
					String line;
					while ((line = reader.readLine()) != null) {
						fileContent.append(line).append(System.lineSeparator());
					}
				}

				int startPosition = props.has("start_position") ? props.get("start_position").asInt() : 0;
				int endPosition = props.has("start_position") ? props.get("end_position").asInt()
						: fileContent.length();

				// Validate positions
				if (startPosition < 0 || endPosition > fileContent.length() || startPosition >= endPosition) {
					return "Invalid start or end position for text replacement.";
				}

				// Replace the text between start and end positions
				fileContent.replace(startPosition, endPosition, text);

				// Write the updated content back to the file
				try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(charsetName))) {
					writer.write(fileContent.toString());
				}

				return "File updated successfully: " + filePath;

			} else {
				if (file.getParentFile() != null) {
					file.getParentFile().mkdirs();
				}
				try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(charsetName))) {
					writer.write(text);
					result = "File written successfully: " + filePath;
				}
			}

		} catch (IOException e) {
			result = e.getMessage();
		}

		return result;
	}

	/**
	 * Implements {@code read_file_from_file_system}.
	 *
	 * <p>
	 * Expected parameters:
	 * </p>
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
		String charsetName = props.has(CHARSET_NAME_FIELD) ? props.get(CHARSET_NAME_FIELD).asText(DEFAULT_CHARSET)
				: DEFAULT_CHARSET;

		File workingDir = (File) params[1];
		String result;
		try (FileInputStream io = new FileInputStream(new File(workingDir, filePath))) {
			result = IOUtils.toString(io, charsetName);
		} catch (FileNotFoundException e) {
			result = "File not found.";
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		if (logger.isInfoEnabled()) {
			logger.info("Read file: {}", Arrays.toString(params));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Read file: {}", Arrays.toString(params));
		}

		return result;
	}

	/**
	 * Collects all files under {@code directory} recursively.
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

	/**
	 * Computes a project-relative path string.
	 *
	 * <p>
	 * The returned path always uses forward slashes ({@code /}) for consistency
	 * across platforms.
	 * </p>
	 *
	 * @param dir          base directory used to relativize the {@code file}
	 * @param file         target file or directory
	 * @param addSingleDot whether to prefix relative paths with {@code ./}
	 * @return relative path, {@code .} if {@code dir} equals {@code file}, or
	 *         {@code null} if {@code file} is not a descendant of {@code dir}
	 */
	public static String getRelativePath(File dir, File file, boolean addSingleDot) {
		if (dir == null || file == null) {
			return null;
		}

		Path dirPath = dir.toPath().toAbsolutePath().normalize();
		Path filePath = file.toPath().toAbsolutePath().normalize();

		if (dirPath.equals(filePath)) {
			return ".";
		}

		String relativePath;
		try {
			relativePath = dirPath.relativize(filePath).toString().replace("\\", "/");
		} catch (IllegalArgumentException e) {
			return null;
		}

		if (addSingleDot && !relativePath.startsWith(".")) {
			relativePath = "./" + relativePath;
		}

		if (relativePath.isEmpty()) {
			return ".";
		}

		return relativePath;
	}
}
