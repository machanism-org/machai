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
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BindexBuilder} specialization for JavaScript/TypeScript/Vue projects.
 *
 * <p>This builder reads {@code package.json} from the project root and walks the {@code src} directory,
 * prompting the configured GenAI provider with all discovered source files (extensions: {@code .js},
 * {@code .ts}, {@code .vue}).
 *
 * <p>Example:
 * {@code
 * JScriptBindexBuilder builder = new JScriptBindexBuilder(layout)
 *     .genAIProvider(provider);
 * Bindex bindex = builder.build();
 * }
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
     * @param projectLayout layout describing the project directory and manifest location
     */
    public JScriptBindexBuilder(ProjectLayout projectLayout) {
        super(projectLayout);
    }

    /**
     * Adds JavaScript project context to the provider.
     *
     * <p>The implementation:
     * <ol>
     *   <li>prompts the contents of {@code package.json},</li>
     *   <li>walks the {@code src} tree and prompts each {@code .js}/{@code .ts}/{@code .vue} file,</li>
     *   <li>adds additional prompting rules for JavaScript projects.</li>
     * </ol>
     *
     * @throws IOException if reading files fails or prompting fails
     */
    @Override
    public void projectContext() throws IOException {
        File packageFile = new File(getProjectLayout().getProjectDir(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
        try (FileReader reader = new FileReader(packageFile)) {
            String prompt = MessageFormat.format(promptBundle.getString("js_resource_section"), IOUtils.toString(reader));
            getGenAIProvider().prompt(prompt);
        }

        Path startPath = Paths.get(new File(getProjectLayout().getProjectDir(), "src").getAbsolutePath());
        if (Files.exists(startPath)) {
            Files.walk(startPath)
                .filter(f -> FilenameUtils.isExtension(f.toFile().getName(), "ts", "vue", "js"))
                .forEach(f -> {
                    try {
                        getGenAIProvider().promptFile(f.toFile(), "source_resource_section");
                    } catch (IOException e) {
                        logger.warn("File: {} adding failed.", f);
                    }
                });
        }

        String prompt = promptBundle.getString("additional_rules");
        getGenAIProvider().prompt(prompt);
    }

}
