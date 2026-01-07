package org.machanism.machai.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.lang.SystemUtils;
import org.jline.reader.LineReader;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.gw.DocsProcessor;
import org.machanism.machai.gw.Ghostwriter;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell command for GenAI document processing operations.
 * <p>
 * Provides CLI functionality to scan and process documents using the selected
 * GenAI provider and model. Usage Example:
 * 
 * <pre>
 * {@code
 * DocsCommand docsCmd = new DocsCommand();
 * docsCmd.docs(new File("/projects/"), "OpenAI:gpt-5.1");
 * }
 * </pre>
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@ShellComponent
public class DocsCommand {

	private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);

	/** JLine line reader for shell interaction. */
	@Autowired
	@Lazy
	LineReader reader;

	/**
	 * Scans and processes documents in the given project directory using the
	 * specified GenAI chat model.
	 * 
	 * @param dir       The root directory to scan
	 * @param chatModel GenAI service provider/model (default is
	 *                  Ghostwriter.CHAT_MODEL)
	 * @throws IOException if scan or processing fails
	 */
	@ShellMethod("GenAI document processing command.")
	public void docs(
			@ShellOption(help = "The path to the directory to be processed.", value = "--scan", defaultValue = ShellOption.NULL) File dir,
			@ShellOption(help = "Removes all .machai template folders from the root directory.", value = "--clean", defaultValue = "false", optOut = true) boolean clean,
			@ShellOption(help = "The path fo the project directory.", value = "--root", defaultValue = ShellOption.NULL, optOut = true) File rootDir,
			@ShellOption(help = "Specifies the GenAI service provider and model (e.g., `OpenAI:gpt-5.1`). If `--genai` is empty, the default model '"
					+ Ghostwriter.CHAT_MODEL
					+ "' will be used.", value = "genai", defaultValue = "None", optOut = true) String chatModel)
			throws IOException {

		if (rootDir == null) {
			rootDir = SystemUtils.getUserDir();
		}

		if (dir == null) {
			dir = rootDir;
		}

		if (clean) {
			removeAllDirectoriesByName(rootDir.toPath(), ".machai");
			logger.info("Cleanup complete: All .machai temporary folders deleted.");
		}

		String relatedPath = ProjectLayout.getRelatedPath(rootDir, dir);
		if (relatedPath == null) {
			logger.warn(
					"The starting document scan directory: `{}` is not located within the root directory or the current directory path: `{}`",
					dir, rootDir);
			rootDir = dir;
		} else {
			logger.info("Root project directory: {}", rootDir);
			logger.info("Starting document scan in directory: {}", dir);
		}

		if (chatModel == null) {
			chatModel = Ghostwriter.CHAT_MODEL;
		}

		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel);
		DocsProcessor documents = new DocsProcessor(provider);
		documents.scanDocuments(rootDir, dir);
		logger.info("Scanning finished.");
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
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
