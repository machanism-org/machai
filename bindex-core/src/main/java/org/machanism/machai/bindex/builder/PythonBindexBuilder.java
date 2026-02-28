package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.project.layout.ProjectLayout;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

/**
 * {@link BindexBuilder} specialization for Python projects.
 *
 * <p>
 * This builder reads {@code pyproject.toml} from the project root and uses it to
 * discover the project name (from {@code project.name}). It then prompts the
 * configured GenAI provider with the manifest content and any regular files
 * found under the inferred source directory.
 *
 * <p>
 * Example:
 *
 * <pre>{@code
 * Bindex bindex = new PythonBindexBuilder(layout, "openai", config).build();
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see org.machanism.machai.bindex.BindexBuilderFactory
 * @see ProjectLayout
 */
public class PythonBindexBuilder extends BindexBuilder {

	private static final ResourceBundle promptBundle = ResourceBundle.getBundle("python_project_prompts");
	private static final String PROJECT_MODEL_FILE_NAME = "pyproject.toml";

	/**
	 * Creates a builder for a Python project.
	 *
	 * @param projectLayout layout describing the project directory and manifest
	 *                      location
	 * @param genai         provider identifier used by
	 *                      {@link org.machanism.machai.ai.manager.GenAIProviderManager}
	 * @param config        configuration used to initialize the provider
	 */
	public PythonBindexBuilder(ProjectLayout projectLayout, String genai, Configurator config) {
		super(projectLayout, genai, config);
	}

	/**
	 * Adds Python-specific project context to the provider.
	 *
	 * <p>
	 * The implementation:
	 * <ol>
	 * <li>prompts {@code pyproject.toml},</li>
	 * <li>infers a source directory from {@code project.name} (dots replaced with
	 * slashes),</li>
	 * <li>prompts all regular files directly under that directory,</li>
	 * <li>adds additional prompting rules for Python projects.</li>
	 * </ol>
	 *
	 * @return a prompt string containing Python project context
	 * @throws IOException if reading the manifest fails or prompting fails
	 */
	@Override
	public String projectContext() throws IOException {
		File pyprojectTomlFile = new File(getProjectLayout().getProjectDir(), PROJECT_MODEL_FILE_NAME);

		StringBuilder prompt = new StringBuilder();

		try (FileReader reader = new FileReader(pyprojectTomlFile)) {
			prompt.append(MessageFormat.format(promptBundle.getString("project_build_section"), IOUtils.toString(reader)));
		}

		TomlParseResult result = Toml.parse(pyprojectTomlFile.toPath());
		String projectName = result.getString("project.name");
		if (projectName != null) {
			File sourceDir = new File(getProjectLayout().getProjectDir(), projectName.replace(".", "/"));

			File[] listFiles = sourceDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});

			if (listFiles != null) {
				for (File file : listFiles) {
					prompt.append(promptFile(file, "source_resource_section"));
				}
			}
		}

		prompt.append(promptBundle.getString("additional_rules"));

		return prompt.toString();
	}

}
