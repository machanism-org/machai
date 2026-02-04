package org.machanism.machai.project.layout;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Strings;

/**
 * Provides a default implementation for project layout handling.
 * <p>
 * This class determines modules, sources, documents, and tests within a
 * standard project directory. It excludes common build and version control
 * directories based on {@link ProjectLayout#EXCLUDE_DIRS}.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see ProjectLayout
 */
public class DefaultProjectLayout extends ProjectLayout {

	private List<String> modules;

	/**
	 * Returns the list of module names present in the project directory excluding
	 * standard excluded directories.
	 *
	 * @return a list of module names
	 * @throws IOException if an I/O error occurs during directory scan
	 * @see ProjectLayout#EXCLUDE_DIRS
	 * 
	 *      <pre>
	 * Example usage:
	 * DefaultProjectLayout layout = new DefaultProjectLayout();
	 * List<String> modules = layout.projectDir(new File("/my/project/path")).getModules();
	 *      </pre>
	 */
	@Override
	public List<String> getModules() {
		if (modules == null) {
			modules = new ArrayList<>();
			
			File projectDir = getProjectDir();
			File[] listFiles = projectDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return (pathname.isDirectory() && !Strings.CS.startsWithAny(pathname.getName(), EXCLUDE_DIRS));
				}
			});

			if (listFiles != null) {
				for (File file : listFiles) {
					modules.add(file.getName());
				}
			}
		}

		return modules;
	}

	/**
	 * Returns a list of sources for this project layout.
	 * <p>
	 * Currently not implemented for the default layout.
	 *
	 * @return always {@code null}
	 */
	@Override
	public List<String> getSources() {
		return null;
	}

	/**
	 * Returns a list of document sources for this project layout.
	 * <p>
	 * Currently not implemented for the default layout.
	 *
	 * @return always {@code null}
	 */
	@Override
	public List<String> getDocuments() {
		return null;
	}

	/**
	 * Returns a list of test sources for this project layout.
	 * <p>
	 * Currently not implemented for the default layout.
	 *
	 * @return always {@code null}
	 */
	@Override
	public List<String> getTests() {
		return null;
	}

	@Override
	public DefaultProjectLayout projectDir(File projectDir) {
		return (DefaultProjectLayout) super.projectDir(projectDir);
	}

}
