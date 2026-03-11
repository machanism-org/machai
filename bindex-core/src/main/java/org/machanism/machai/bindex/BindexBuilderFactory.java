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
 * Creates {@link BindexBuilder} instances appropriate for a given {@link ProjectLayout}.
 *
 * <p>The factory selects a specialized builder when the layout is recognized (for example Maven,
 * JavaScript, or Python). When the layout is not recognized but the project directory exists,
 * a generic {@link BindexBuilder} is returned.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * ProjectLayout layout = ...;
 * Configurator config = ...;
 *
 * BindexBuilder builder = BindexBuilderFactory.create(layout, "openai", config);
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class BindexBuilderFactory {

	// Sonar java:S1118 - hide implicit public constructor for utility class.
	private BindexBuilderFactory() {
		// utility class
	}

	/**
	 * Creates a {@link BindexBuilder} suitable for the supplied project layout.
	 *
	 * @param projectLayout project layout to analyze
	 * @param genai         GenAI provider identifier passed to the created builder
	 * @param configurator  configurator used by the created builder
	 * @return a suitable {@link BindexBuilder} implementation
	 * @throws FileNotFoundException if {@code projectLayout}'s project directory does not exist
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public static BindexBuilder create(ProjectLayout projectLayout, String genai, Configurator configurator)
			throws FileNotFoundException {
		if (projectLayout == null) {
			throw new IllegalArgumentException("projectLayout must not be null");
		}
		if (genai == null) {
			throw new IllegalArgumentException("genai must not be null");
		}
		if (configurator == null) {
			throw new IllegalArgumentException("configurator must not be null");
		}

		if (projectLayout instanceof MavenProjectLayout) {
			return new MavenBindexBuilder((MavenProjectLayout) projectLayout, genai, configurator);
		}
		if (projectLayout instanceof JScriptProjectLayout) {
			return new JScriptBindexBuilder(projectLayout, genai, configurator);
		}
		if (projectLayout instanceof PythonProjectLayout) {
			return new PythonBindexBuilder(projectLayout, genai, configurator);
		}
		if (projectLayout.getProjectDir().exists()) {
			return new BindexBuilder(projectLayout, genai, configurator);
		}
		throw new FileNotFoundException(projectLayout.getProjectDir().getAbsolutePath());
	}

}
