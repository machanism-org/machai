package org.machanism.machai.project.layout;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Strings;

/**
 * Minimal fallback {@link ProjectLayout} implementation.
 *
 * <p>
 * This layout performs a lightweight filesystem inspection and treats each
 * immediate subdirectory of the configured project root as a potential module
 * (excluding entries listed in {@link ProjectLayout#excludeDirs}). It does not
 * try to infer language-specific source, test or documentation roots; those
 * accessors return {@code null}.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see ProjectLayout
 */
public class DefaultProjectLayout extends ProjectLayout {

	private List<String> modules;

	/**
	 * Returns a list of module directory names present in the configured project
	 * directory.
	 *
	 * <p>
	 * The default implementation treats each immediate subdirectory as a module.
	 * </p>
	 *
	 * <pre>
	 * <code>
	 * DefaultProjectLayout layout = new DefaultProjectLayout();
	 * java.util.List&lt;String&gt; modules = layout.projectDir(new java.io.File("C:\\repo")).getModules();
	 * </code>
	 * </pre>
	 *
	 * @return a list of module directory names (never {@code null})
	 */
	@Override
	public List<String> getModules() {
		if (modules == null) {
			modules = new ArrayList<>();

			File projectDir = getProjectDir();
			FileFilter filter = pathname -> pathname.isDirectory()
					&& !Strings.CS.startsWithAny(pathname.getName(), getExcludeDirs());
			File[] listFiles = projectDir == null ? null : projectDir.listFiles(filter);

			if (listFiles != null) {
				for (File file : listFiles) {
					modules.add(file.getName());
				}
			}
		}

		return modules;
	}

	/**
	 * Returns a list of source roots for this layout.
	 *
	 * @return empty list; not inferred by the default layout
	 */
	@Override
	public List<String> getSources() {
		return Collections.emptyList();
	}

	/**
	 * Returns a list of documentation roots for this layout.
	 *
	 * @return empty list; not inferred by the default layout
	 */
	@Override
	public List<String> getDocuments() {
		return Collections.emptyList();
	}

	/**
	 * Returns a list of test source roots for this layout.
	 *
	 * @return empty list; not inferred by the default layout
	 */
	@Override
	public List<String> getTests() {
		return Collections.emptyList();
	}

	@Override
	public DefaultProjectLayout projectDir(File projectDir) {
		return (DefaultProjectLayout) super.projectDir(projectDir);
	}

}
