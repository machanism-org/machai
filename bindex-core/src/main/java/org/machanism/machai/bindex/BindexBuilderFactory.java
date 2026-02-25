package org.machanism.machai.bindex;

import java.io.FileNotFoundException;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.bindex.builder.JScriptBindexBuilder;
import org.machanism.machai.bindex.builder.MavenBindexBuilder;
import org.machanism.machai.bindex.builder.PythonBindexBuilder;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

/**
 * Factory class for creating BindexBuilder instances based on specific
 * ProjectLayout types.
 * <p>
 * Usage example:
 * 
 * <pre>
 *     ProjectLayout layout = ...;
 *     BindexBuilder builder = BindexBuilderFactory.create(layout);
 * </pre>
 * 
 * Depending on the layout type, returns an appropriate builder instance, or
 * throws if directory is missing.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class BindexBuilderFactory {

	/**
	 * Creates a BindexBuilder suitable for the given ProjectLayout type.
	 *
	 * @param projectLayout the project layout to analyze (Maven, JScript, Python,
	 *                      or generic)
	 * @param genai
	 * @param configurator
	 * @return a suitable BindexBuilder implementation
	 * @throws FileNotFoundException if the project directory does not exist
	 */
	public static BindexBuilder create(ProjectLayout projectLayout, String genai, Configurator configurator)
			throws FileNotFoundException {
		BindexBuilder bindexBuilder;
		if (projectLayout instanceof MavenProjectLayout) {
			bindexBuilder = new MavenBindexBuilder((MavenProjectLayout) projectLayout, genai, configurator);
		} else if (projectLayout instanceof JScriptProjectLayout) {
			bindexBuilder = new JScriptBindexBuilder((JScriptProjectLayout) projectLayout, genai, configurator);
		} else if (projectLayout instanceof PythonProjectLayout) {
			bindexBuilder = new PythonBindexBuilder((PythonProjectLayout) projectLayout, genai, configurator);
		} else if (projectLayout.getProjectDir().exists()) {
			bindexBuilder = new BindexBuilder(projectLayout, genai, configurator);
		} else {
			throw new FileNotFoundException(projectLayout.getProjectDir().getAbsolutePath());
		}
		return bindexBuilder;
	}

}
