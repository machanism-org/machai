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
 * <pre>
 *   ProjectProcessor processor = ...;
 *   processor.scanFolder(new File("/path/to/project"));
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public abstract class ProjectProcessor {

	/**
	 * Creates a project processor instance.
	 */
	protected ProjectProcessor() {
	}

	/** Logger used to record module traversal. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectProcessor.class);

	/**
	 * Scans the main project directory, detects modules, and processes them. If
	 * modules are present, each module is processed. Otherwise, the entire folder
	 * structure is processed.
	 *
	 * @param projectDir the root project directory to scan
	 * @throws IOException if layout detection or processing encounters an I/O error
	 */
	public void scanFolder(File projectDir) throws IOException {
		ProjectLayout projectLayout = getProjectLayout(projectDir);
		List<String> modules = projectLayout.getModules();

		if (modules != null) {
			for (String module : modules) {
				processModule(projectDir, module);
			}
		} else {
			processFolder(projectLayout);
		}
	}

	/**
	 * Processes a given project module by recursively scanning.
	 * 
	 * @param projectDir the main project directory
	 * @param module     the module path relative to {@code projectDir}
	 * @throws IOException if recursive scanning encounters an I/O error
	 */
	protected void processModule(File projectDir, String module) throws IOException {
		LOGGER.debug("Module: `{}`", module);
		File moduleDir = new File(projectDir, module);
		scanFolder(moduleDir);
	}

	/**
	 * Processes a project folder layout. Must be implemented by subclasses to
	 * define custom logic.
	 * 
	 * @param processor the layout representing the folder structure to process
	 * @throws IOException if processing the folder or its contents encounters an
	 *                     I/O error
	 */
	public abstract void processFolder(ProjectLayout processor) throws IOException;

	/**
	 * Returns the detected {@link ProjectLayout} for the specified project
	 * directory.
	 * 
	 * @param projectDir the root project directory to analyze
	 * @return the detected and configured project layout
	 * @throws FileNotFoundException if the directory does not exist
	 */
	public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
		return ProjectLayoutManager.detectProjectLayout(projectDir);
	}

}
