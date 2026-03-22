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
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Spring Shell command that removes Machai temporary folders from a directory
 * tree.
 *
 * <p>The primary target is the {@value #MACHAI_TEMP_DIR} directory, which is used
 * to store intermediate artifacts produced by Machai workflows.
 *
 * <h2>Example</h2>
 * <pre>
 * clean --dir .\\my-project
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class CleanCommand {
	private static final Logger logger = LoggerFactory.getLogger(CleanCommand.class);

	/** Name of the Machai temporary directory. */
	public static final String MACHAI_TEMP_DIR = ".machai";

	/**
	 * Removes all {@value #MACHAI_TEMP_DIR} directories from the provided root
	 * directory.
	 *
	 * @param dir the root directory to clean; if {@code null}, uses the configured
	 *            default or the current working directory
	 * @throws IOException if the cleanup process fails
	 */
	@ShellMethod("Removes all " + MACHAI_TEMP_DIR + " template folders from the root directory.")
	public void clean(
			@ShellOption(value = { "-d",
					ProjectLayout.PROJECT_DIR_PROP_NAME }, help = "The path fo the project directory.", defaultValue = ShellOption.NULL) File dir)
			throws IOException {
		dir = Optional.ofNullable(dir).orElse(ConfigCommand.config.getFile(ProjectLayout.PROJECT_DIR_PROP_NAME, SystemUtils.getUserDir()));
		logger.info("Starting cleanup: Removing all '{}' temporary folders in {}.", MACHAI_TEMP_DIR, dir);
		removeAllDirectoriesByName(dir.toPath(), MACHAI_TEMP_DIR);
		logger.info("Cleanup process finished.");
	}

	/**
	 * Removes all directories with the specified name from the given root path.
	 *
	 * @param rootPath the root directory to start the search
	 * @param dirName  the directory name to remove
	 * @throws IOException if an I/O error occurs
	 */
	public static void removeAllDirectoriesByName(Path rootPath, String dirName) throws IOException {
		Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (dir.getFileName().toString().equals(dirName)) {
					deleteDirectoryRecursively(dir);
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Recursively deletes a directory and all its contents.
	 *
	 * @param dir the directory to delete
	 * @throws IOException if an I/O error occurs
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
