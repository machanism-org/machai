package org.machanism.machai.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traverses a detected project structure and delegates leaf-folder processing
 * to subclasses.
 *
 * <p>When a layout reports {@code null} modules, this processor invokes
 * {@link #processFolder(ProjectLayout)} for that layout. Otherwise, it scans
 * every reported module recursively. Consequently, a layout that reports an
 * empty module list does not invoke {@code processFolder}.</p>
 *
 * <h2>Usage</h2>
 * <pre><code>
 * ProjectProcessor processor = createProcessor();
 * processor.scanFolder(new File("path/to/project"));
 * </code></pre>
 *
 * </p>
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
	 * Detects the layout of the specified directory and processes its leaf
	 * projects. A layout with {@code null} modules is processed directly; a
	 * layout with a non-null module list causes each listed module to be scanned
	 * recursively.
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
	 * Processes a reported project module by recursively scanning its directory.
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
	 * Processes one detected leaf project layout. Subclasses implement this hook
	 * to perform their repository-specific work.
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
