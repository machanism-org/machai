package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.FileInputStream;
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
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.project.layout.ProjectLayout;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Installs file-system tools into a {@link Genai}.
 *
 * <p>
 * Tools in this installer are intended for host-integrated use where the host
 * controls the base working directory. All path provided to these tools are
 * interpreted relative to the working directory supplied by the
 * provider/runtime.
 * </p>
 *
 * <h2>Installed tools</h2>
 * <ul>
 * <li>{@code read_file} – reads a file as text</li>
 * <li>{@code write_file} – writes a file (creating parent directories as
 * needed)</li>
 * <li>{@code list_files_in_directory} – lists immediate children of a
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
	 * Implements {@code list_files_in_directory}.
	 *
	 * <p>
	 * This AI functional tool lists the immediate children of a directory.
	 * </p>
	 *
	 * @param dirPath    directory to list, resolved relative to {@code projectDir}
	 * @param projectDir project root used to resolve the directory
	 * @return project-relative paths of immediate children, or an empty list when
	 *         the path is not a directory
	 *
	 *         <p>
	 *         Expected parameters:
	 *         </p>
	 *         <ol>
	 *         <li>{@link JsonNode} optionally containing {@code dir_path}</li>
	 *         <li>{@link File} working directory</li>
	 *         </ol>
	 */
	@Tool(name = "list-files-in-directory", description = "List files and directories in a specified folder.")
	public List<String> listFiles(
			@Param(name = "dir-path", description = "The path to the directory to list contents of.", defaultValue = ".") File dirPath,
			@Param(name = "project-dir", description = "The project dir.") File projectDir) {
		File directory = getFile(dirPath, projectDir);
		List<String> result = new ArrayList<>();
		if (directory.isDirectory()) {
			File[] listFiles = directory.listFiles();
			if (listFiles != null) {
				for (File file : listFiles) {
					result.add(getRelativePath(projectDir, file, true));
				}
			}
		}
		return result;
	}

	/**
	 * Lists files recursively in a directory up to a specified maximum limit.
	 *
	 * <p>
	 * This AI functional tool returns the files discovered below a directory.
	 * </p>
	 *
	 * @param dir        the relative or absolute path of the directory to scan
	 * @param max_count  the maximum number of files allowed in the result; throws
	 *                   an error if exceeded
	 * @param projectDir the root project directory context
	 * @return a {@link List} of relative file path strings, or a message string
	 *         indicating no files were found
	 * @throws IllegalArgumentException if the number of discovered files exceeds
	 *                                  {@code max_count}
	 */
	@Tool(name = "get-recursive-file-list", description = "List files recursively in a directory (includes files in subdirectories).")
	public Object getRecursiveFiles(
			@Param(name = "dir", description = "Path to the folder to list contents recursively.", defaultValue = "") File dir,
			@Param(name = "max-count", description = "The maximum number of files allowed in the results. Used to prevent overly large context payloads.", defaultValue = "50") int maxCount,
			@Param(name = "project-dir", description = "The project dir.") File projectDir) {
		File directory = getFile(dir, projectDir);

		List<File> listFiles = ProjectLayout.listFiles(directory);
		List<String> files = new ArrayList<>();
		Object result;
		if (!listFiles.isEmpty()) {
			for (File file : listFiles) {
				files.add(getRelativePath(projectDir, file, true));
			}
			if (files.size() > maxCount) {
				throw new IllegalArgumentException(
						String.format(
								"Result is too long. The number of discovered files (%d) exceeds the allowed limit of %d.",
								files.size(), maxCount));
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
	 * This AI functional tool recursively discovers and returns only the folder structure
	 * (directories) within a specified path. It does not return files or contents stored 
	 * inside those folders.
	 * </p>
	 *
	 * @param dir        directory path relative to {@code projectDir} to start scanning from
	 * @param max_count  maximum number of folders allowed in the result
	 * @param projectDir project root used to resolve the directory
	 * @return project-relative folder paths as a list, or a message when none are found
	 * @throws IllegalArgumentException if the number of discovered folders exceeds
	 *                                  {@code max_count}
	 */
	@Tool(
		name = "get-recursive-folder-list", 
		description = "Recursively lists only the folder structure (directories) within a directory. Does not include files."
	)
	public Object getRecursiveFolders(
			@Param(name = "dir", description = "Path to the root folder to recursively list sub-directories for. Returns directories only, no files.", defaultValue = "") File dir,
			@Param(name = "max-count", description = "The maximum number of folders allowed in the results. Used to prevent overly large context payloads.", defaultValue = "50") int maxCount,
			@Param(name = "project-dir", description = "The project root directory.") File projectDir) {
		File directory = getFile(dir, projectDir);

		List<File> listFiles = ProjectLayout.listDirectories(directory);
		List<String> files = new ArrayList<>();
		Object result;
		if (!listFiles.isEmpty()) {
			for (File file : listFiles) {
				files.add(getRelativePath(projectDir, file, true));
			}
			if (files.size() > maxCount) {
				throw new IllegalArgumentException(
						String.format(
								"Result is too long. The number of discovered folders (%d) exceeds the allowed limit of %d.",
								files.size(), maxCount));
			}
			result = files;
		} else {
			result = "No folders found in directory.";
		}

		return result;
	}

	/**
	 * Implements {@code write_file}.
	 *
	 * <p>
	 * This AI functional tool creates or replaces a file with the supplied text.
	 * </p>
	 *
	 * @param filePath    file to create or replace, relative to {@code projectDir}
	 * @param text        content to write
	 * @param charsetName character set used to encode the content
	 * @param projectDir  project root used to resolve the file
	 * @return a success message or an error message when writing fails
	 */
	@Tool(name = "write-file", description = "Write changes to a file on the file system, either by replacing content at specific positions or writing the full content.")
	public String writeFile(
			@Param(name = "file-path", description = "The path to the file you want to write to or create.") File filePath,
			@Param(name = "text", description = "The content to be written into the file or used as replacement.") String text,
			@Param(name = "charset-name", description = "The name of the requested charset.", defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "project-dir", description = "The project dir.") File projectDir) {
		String result;
		File file = getFile(filePath, projectDir);
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
	private String writeNewFile(File file, String text, String charsetName, File filePath) throws IOException {
		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		writeFileContent(file, text, charsetName);
		return "File written successfully: " + filePath;
	}

	/**
	 * Implements {@code read_file}.
	 *
	 * <p>
	 * This AI functional tool reads a file and returns its text content.
	 * </p>
	 *
	 * <p>
	 * Expected parameters:
	 * </p>
	 * <ol>
	 * <li>{@link JsonNode} containing {@code file_path}</li>
	 * <li>{@link File} working directory</li>
	 * </ol>
	 * 
	 * @param filePath    file to read, relative to {@code projectDir}
	 * @param charsetName character set used to decode the file
	 * @param projectDir  project root used to resolve the file
	 * @return the file contents as text
	 * @throws IOException if the path is not a regular file or cannot be read
	 */
	@Tool(name = "read-file", description = "Read the contents of a file from the disk.")
	public String readFile(@Param(name = "file-path", description = "The path to the file to be read.") File filePath,
			@Param(name = "charset-name", description = "the name of the requested charset.", defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "project-dir", description = "The project dir.") File projectDir) throws IOException {
		String result;
		filePath = getFile(filePath, projectDir);
		if (!filePath.isFile()) {
			String detail = filePath.isDirectory() ? "is a directory" : "does not exist";
			throw new IOException(String.format("Expected a file, but '%s' %s.", filePath, detail));
		}
		try (FileInputStream io = new FileInputStream(filePath)) {
			result = IOUtils.toString(io, charsetName);
		}
		return result;
	}

	/**
	 * Resolves a requested path beneath the canonical project root.
	 *
	 * @param filePath requested file or directory
	 * @param projectDir project root
	 * @return canonical file located under the project root
	 * @throws IllegalArgumentException if a path is invalid or escapes the root
	 */
	File getFile(File filePath, File projectDir) {
		if (filePath == null || projectDir == null) {
			throw new IllegalArgumentException("File path and project directory must not be null.");
		}

		try {
			File baseDir = projectDir.getCanonicalFile();
			File candidate = filePath.isAbsolute() ? filePath : new File(baseDir, filePath.getPath());
			File canonicalCandidate = candidate.getCanonicalFile();
			Path basePath = baseDir.toPath();
			Path candidatePath = canonicalCandidate.toPath();
			if (!candidatePath.startsWith(basePath)) {
				throw new IllegalArgumentException("Access denied: file path is outside the project root.");
			}
			// Sonar java:S2083: canonicalize before the containment check to block ../ and symlink escapes.
			return canonicalCandidate;
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to resolve file path within the project root.", e);
		}
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
	 * @param addSingleDot whether to prefix relative path with {@code ./}
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

	/**
	 * Implements {@code apply_patch_to_file}.
	 *
	 * <p>
	 * This AI functional tool applies a targeted unified or simplified
	 * search-and-replace patch to a file within the project directory.
	 * </p>
	 */
	@Tool(name = "apply-patch-to-file", description = "Use this tool to update a part of a file efficiently "
			+ "by applying a targeted diff patch. Supports two formats:\n"
			+ "1. Standard Unified Diff (as produced by `diff -u` or `git diff`) containing @@ coordinates (e.g., '@@ -12,4 +12,18 @@').\n"
			+ "2. Simplified Search-and-Replace Diff containing a plain '@@' header with exact line-matching blocks "
			+ "starting with '-' (lines to find and remove) and '+' (lines to insert).\n"
			+ "Make sure your patch matches the surrounding target context uniquely to ensure successful application.")
	public String applyPatchToFile(
			@Param(name = "file", description = "The path to the file to be patched.") File file,
			@Param(name = "patch", description = "The unified diff patch to apply.") String patch,
			@Param(name = "charset-name", description = "The name of the requested charset.", defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "project-dir", description = "The project dir.") File projectDir) {
		try {
			List<String> patchLines = Arrays.asList(patch.split("\\r?\\n"));
			file = getFile(file, projectDir);
			PatchApplier.applyPatch(file, patchLines, Charset.forName(charsetName));
			return "Patch applied successfully.";
		} catch (Exception e) {
			return "Failed to apply patch: " + e.getMessage();
		}
	}
}
