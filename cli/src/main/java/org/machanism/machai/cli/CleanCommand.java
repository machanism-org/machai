package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Provides shell commands for cleaning up Machai temporary folders.
 * <p>
 * The {@code CleanCommand} class includes methods for removing all ".machai"
 * template folders within a specified root directory.
 * <p>
 * Usage Example:
 * 
 * <pre>
 * {@code
 * CleanCommand cleanCmd = new CleanCommand();
 * cleanCmd.clean(new File("/path/to/root"));
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class CleanCommand {
	private static Logger logger = LoggerFactory.getLogger(CleanCommand.class);

	/** Name of the Machai temporary directory. */
	public static final String MACHAI_TEMP_DIR = ".machai";

	/**
	 * Removes all ".machai" template folders from the provided root directory.
	 *
	 * @param dir The path to the project directory. If null, uses the user
	 *            directory.
	 * @throws IOException if the cleanup process fails
	 */
	@ShellMethod("Removes all " + MACHAI_TEMP_DIR + " template folders from the root directory.")
	public void clean(
			@ShellOption(value = { "-d",
					"--dir" }, help = "The path fo the project directory.", defaultValue = ShellOption.NULL) File dir)
			throws IOException {
		dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile("dir", SystemUtils.getUserDir()));
		logger.info("Starting cleanup: Removing all '{}' temporary folders in {}.", MACHAI_TEMP_DIR, dir);
		removeAllDirectoriesByName(dir.toPath(), MACHAI_TEMP_DIR);
		logger.info("Cleanup process finished.");
	}

	/**
	 * Removes all directories with the specified name from the given root path.
	 *
	 * @param rootPath The root directory to start the search.
	 * @param dirName  The name of directories to remove.
	 * @throws IOException If an I/O error occurs.
	 */
	public static void removeAllDirectoriesByName(Path rootPath, String dirName) throws IOException {
		Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (dir.getFileName().toString().equals(dirName)) {
					// Recursively delete the directory and its contents
					deleteDirectoryRecursively(dir);
					// Skip visiting entries in this directory since it's deleted
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Recursively deletes a directory and all its contents.
	 * 
	 * <pre>
	 * Example:
	 * {@code
	 * CleanCommand.deleteDirectoryRecursively(Paths.get("/tmp/.machai"));
	 * }
	 * </pre>
	 * 
	 * @param dir The directory to delete.
	 * @throws IOException If an I/O error occurs.
	 */
	private static void deleteDirectoryRecursively(Path dir) throws IOException {
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
				Files.delete(directory);
				logger.debug(directory.toFile().getAbsolutePath());
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
