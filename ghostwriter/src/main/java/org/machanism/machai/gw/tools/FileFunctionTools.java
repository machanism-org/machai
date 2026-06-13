package org.machanism.machai.gw.tools;

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
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.ToolParam;
import org.machanism.machai.project.layout.ProjectLayout;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Installs file-system tools into a {@link Genai}.
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

	/**
	 * Default character set used when reading or writing text files.
	 */
	private static final String DEFAULT_CHARSET = "UTF-8";

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
	 */
	@Tool(name = "get_recursive_file_list", description = "List files recursively in a directory (includes files in subdirectories).")
	public Object getRecursiveFiles(
			@ToolParam(name = "dir", description = "Path to the folder to list contents recursively.", defaultValue = "") String path,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		File directory = new File(projectDir, path);

		List<File> listFiles = ProjectLayout.findFiles(directory);
		List<String> files = new ArrayList<>();
		Object result;
		if (!listFiles.isEmpty()) {
			for (File file : listFiles) {
				files.add(getRelativePath(projectDir, file, true));
			}
			result = files;
		} else {
			result = "No files found in directory.";
		}

		return result;
	}

	/**
	 * Implements {@code get_recursive_folder_list}.
	 *
	 * <p>
	 * Expected parameters:
	 * </p>
	 * <ol>
	 * <li>{@link JsonNode} optionally containing {@code dir_path}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 */
	@Tool(name = "get_recursive_folder_list", description = "List folder recursively in a directory.")
	public Object getRecursiveFolders(
			@ToolParam(name = "dir", description = "Path to the folder to list contents recursively.", defaultValue = "") String path,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		File directory = new File(projectDir, path);

		List<File> listFiles = ProjectLayout.findDirectories(directory);
		List<String> files = new ArrayList<>();
		Object result;
		if (!listFiles.isEmpty()) {
			for (File file : listFiles) {
				files.add(getRelativePath(projectDir, file, true));
			}
			result = files;
		} else {
			result = "No folders found in directory.";
		}

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
	 */
	@Tool(name = "list_files_in_directory", description = "List files and directories in a specified folder.")
	public Object listFiles(
			@ToolParam(name = "dirPath", description = "The path to the directory to list contents of.", defaultValue = ".") String dirPath,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		File directory = new File(projectDir, dirPath);
		if (directory.isDirectory()) {
			File[] listFiles = directory.listFiles();
			List<String> result = new ArrayList<>();
			if (listFiles != null) {
				for (File file : listFiles) {
					result.add(getRelativePath(projectDir, file, true));
				}

				return StringUtils.join(result, ",");
			}
		}

		return "No files found in directory.";
	}

	/**
	 * Implements {@code write_file_to_file_system}.
	 */
	@Tool(name = "write_file_to_file_system", description = "Write changes to a file on the file system, either by replacing content at specific positions or writing the full content.")
	public Object writeFile(
			@ToolParam(name = "file_path", description = "The path to the file you want to write to or create.") String filePath,
			@ToolParam(name = "text", description = "The content to be written into the file or used as replacement.") String text,
			@ToolParam(name = "charsetName", description = "The name of the requested charset. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		String result;
		File file = new File(projectDir, filePath);
		try {
			if (file.exists()) {
				writeFileContent(file, text, charsetName);
				return "File updated successfully: " + filePath;
			}

			return writeNewFile(file, text, charsetName, filePath);

		} catch (IOException e) {
			result = e.getMessage();
		}

		return result;
	}

	/**
	 * Writes {@code content} to {@code file} using {@code charsetName}.
	 *
	 * @param file        destination file
	 * @param content     content to write
	 * @param charsetName character set name
	 * @throws IOException if writing fails
	 */
	private void writeFileContent(File file, String content, String charsetName) throws IOException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(charsetName))) {
			writer.write(content);
		}
	}

	/**
	 * Creates the file (and parent directories as needed) and writes {@code text}
	 * using the requested character set.
	 *
	 * @param file        file to create
	 * @param text        content
	 * @param charsetName character set name
	 * @param filePath    original (relative) file path used for messaging
	 * @return success message
	 * @throws IOException if an I/O error occurs
	 */
	private Object writeNewFile(File file, String text, String charsetName, String filePath) throws IOException {
		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		writeFileContent(file, text, charsetName);
		return "File written successfully: " + filePath;
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
	 */
	@ToolParam(name = "read_file_from_file_system", description = "Read the contents of a file from the disk.")
	public Object readFile(@ToolParam(name = "file_path", description = "The path to the file to be read.") String filePath,
			@ToolParam(name = "charsetName", description = "the name of the requested charset, default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		String result;
		try (FileInputStream io = new FileInputStream(new File(projectDir, filePath))) {
			result = IOUtils.toString(io, charsetName);
		} catch (FileNotFoundException e) {
			result = "File not found.";
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return result;
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

	@Tool(name = "apply_patch_to_file", description = "Use this tool to update a small part of a file efficiently. "
			+ "Apply a unified diff patch to a file, updating only the specified parts. The patch must be in unified diff "
			+ "format (as produced by `diff -u` or `git diff`) and should apply only the specified change.")
	public Object applyPatchToFile(
			@ToolParam(name = "filePath", description = "The path to the file to be patched.") String filePath,
			@ToolParam(name = "patch", description = "The unified diff patch to apply.") String patch,
			@ToolParam(name = "charsetName", description = "The name of the requested charset. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		try {
			// Split patch into lines
			List<String> patchLines = Arrays.asList(patch.split("\\r?\\n"));
			// Apply patch (using the PatchApplier from previous example)
			PatchApplier.applyPatch(filePath, patchLines, Charset.forName(charsetName));
			return "Patch applied successfully.";
		} catch (Exception e) {
			return "Failed to apply patch: " + e.getMessage();
		}
	}
}
