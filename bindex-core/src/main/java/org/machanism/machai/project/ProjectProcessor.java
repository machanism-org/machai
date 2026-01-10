package org.machanism.machai.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for processing project structures and modules.
 * <p>
 * Handles project folder scanning and delegates module or folder-specific
 * processing to subclasses.
 *
 * <p>
 * Usage Example:
 * 
 * <pre>{@code
 *   ProjectProcessor processor = ...;
 *   processor.scanFolder(new File("/path/to/project"));
 * }</pre>
 *
 * @author machanism
 * @since 0.0.2
 */
public abstract class ProjectProcessor {
	/** Name of the directory holding temporary documentation inputs. */
	public static final String MACHAI_TEMP_DIR = ".machai";

	/** Logger instance for this processor */
	private static Logger logger = LoggerFactory.getLogger(ProjectProcessor.class);

	/**
	 * Scans the main project directory, detects modules, and processes them. If
	 * modules are present, each module is processed. Otherwise, the entire folder
	 * structure is processed.
	 *
	 * @param projectDir The root project directory to scan.
	 * @throws IOException If an error occurs reading folders or files.
	 */
	public void scanFolder(File projectDir) throws IOException {
		ProjectLayout projectLayout = getProjectLayout(projectDir);
		List<String> modules = projectLayout.getModules();

		if (modules != null) {
			for (String module : modules) {
				processModule(projectDir, module);
			}
		} else {
			projectLayout = getProjectLayout(projectDir);
			try {
				processFolder(projectLayout);
			} catch (Exception e) {
				logger.error("Project dir: " + projectDir, e);
			}
		}
	}

	/**
	 * Processes a given project module by recursively scanning.
	 * 
	 * @param projectDir The main project directory.
	 * @param module     The module name to process.
	 * @throws IOException If an error occurs during processing.
	 */
	protected void processModule(File projectDir, String module) throws IOException {
		scanFolder(new File(projectDir, module));
	}

	/**
	 * Processes a project folder layout. Must be implemented by subclasses to
	 * define custom logic.
	 * 
	 * @param processor The {@link ProjectLayout} object representing the folder
	 *                  structure to process.
	 */
	public abstract void processFolder(ProjectLayout processor);

	/**
	 * Returns the detected {@link ProjectLayout} for the specified project
	 * directory.
	 * 
	 * @param projectDir The root project directory to analyze.
	 * @return Detected {@link ProjectLayout}.
	 * @throws FileNotFoundException If the directory does not exist.
	 */
	protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
		return ProjectLayoutManager.detectProjectLayout(projectDir);
	}
}
