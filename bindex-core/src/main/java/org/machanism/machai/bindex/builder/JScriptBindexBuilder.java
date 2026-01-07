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
 * JScriptBindexBuilder provides project context and source file analysis for JavaScript/TypeScript/Vue projects.
 * <p>
 * Reads package.json, walks the src tree, and generates prompts for AI-based  Bindex creation.
 * <p>
 * Usage example:
 * <pre>
 *     JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *      Bindex bindex = builder.build();
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see org.machanism.machai.bindex.BindexBuilderFactory
 * @see org.machanism.machai.project.layout.JScriptProjectLayout
 */
public class JScriptBindexBuilder extends BindexBuilder {

    /**
     * Constructs a JScriptBindexBuilder for a JS/TS/Vue project layout.
     * @param projectLayout Project layout describing the source and manifest.
     */
    public JScriptBindexBuilder(ProjectLayout projectLayout) {
        super(projectLayout);
    }

    private static Logger logger = LoggerFactory.getLogger(JScriptBindexBuilder.class);
    private static ResourceBundle promptBundle = ResourceBundle.getBundle("js_project_prompts");

    /**
     * Provides prompts for manifest and sources for GenAI model context.
     * Reads 'package.json', walks 'src', and prompts files for code context.
     * @throws IOException When resources cannot be read or prompt fails.
     */
    @Override
    public void projectContext() throws IOException {

        File packageFile = new File(getProjectLayout().getProjectDir(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
        try (FileReader reader = new FileReader(packageFile)) {
            String prompt = MessageFormat.format(promptBundle.getString("js_resource_section"),
                IOUtils.toString(reader));
            getGenAIProvider().prompt(prompt);
        }

        Path startPath = Paths.get(new File(getProjectLayout().getProjectDir(), "src").getAbsolutePath());

        if (Files.exists(startPath)) {
            Files.walk(startPath)
                .filter(f -> FilenameUtils.isExtension(f.toFile().getName(), "ts", "vue", "js"))
                .forEach((f) -> {
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
