package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BindexBuilder} specialization for JavaScript/TypeScript/Vue projects.
 *
 * <p>
 * This builder reads {@code package.json} from the project root and walks the
 * {@code src} directory, prompting the configured GenAI provider with all
 * discovered source files (extensions: {@code .js}, {@code .ts}, {@code .vue}).
 *
 * <p>
 * Example:
 *
 * <pre>{@code
 * Bindex bindex = new JScriptBindexBuilder(layout, "openai", config).build();
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see org.machanism.machai.bindex.BindexBuilderFactory
 * @see JScriptProjectLayout
 */
public class JScriptBindexBuilder extends BindexBuilder {

	private static final Logger logger = LoggerFactory.getLogger(JScriptBindexBuilder.class);
	private static final ResourceBundle promptBundle = ResourceBundle.getBundle("js_project_prompts");

	/**
	 * Creates a builder for a JavaScript/TypeScript/Vue project.
	 *
	 * @param projectLayout layout describing the project directory and manifest
	 *                      location
	 * @param genai         provider identifier used by
	 *                      {@link org.machanism.machai.ai.manager.GenAIProviderManager}
	 * @param config        configuration used to initialize the provider
	 */
	public JScriptBindexBuilder(ProjectLayout projectLayout, String genai, Configurator config) {
		super(projectLayout, genai, config);
	}

	/**
	 * Adds JavaScript project context to the provider.
	 *
	 * <p>
	 * The implementation:
	 * <ol>
	 * <li>prompts the contents of {@code package.json},</li>
	 * <li>walks the {@code src} tree and prompts each
	 * {@code .js}/{@code .ts}/{@code .vue} file,</li>
	 * <li>adds additional prompting rules for JavaScript projects.</li>
	 * </ol>
	 *
	 * @return a prompt string containing JavaScript project context
	 * @throws IOException if reading files fails or prompting fails
	 */
	@Override
	public String projectContext() throws IOException {
		StringBuilder prompt = new StringBuilder();

		File packageFile = new File(getProjectLayout().getProjectDir(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
		try (FileReader reader = new FileReader(packageFile)) {
			prompt.append(MessageFormat.format(promptBundle.getString("js_resource_section"), IOUtils.toString(reader)));
		}

		Path startPath = Paths.get(new File(getProjectLayout().getProjectDir(), "src").getAbsolutePath());
		if (Files.exists(startPath)) {
			Files.walk(startPath).filter(f -> FilenameUtils.isExtension(f.toFile().getName(), "ts", "vue", "js"))
					.forEach(f -> {
						try {
							prompt.append(promptFile(f.toFile(), "source_resource_section"));
						} catch (IOException e) {
							logger.warn("File: {} adding failed.", f);
						}
					});
		}

		prompt.append(promptBundle.getString("additional_rules"));
		return prompt.toString();
	}

}
