package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.machanism.machai.project.layout.ProjectLayout;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

/**
 * PythonBindexBuilder provides project context and manifest aggregation for Python (pyproject.toml) projects.
 * <p>
 * Reads TOML manifest and main source files; prepares prompts for GenAI  Bindex creation.
 * <p>
 * Usage example:
 * <pre>
 *     PythonBindexBuilder builder = new PythonBindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *      Bindex bindex = builder.build();
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see org.machanism.machai.bindex.BindexBuilderFactory
 * @see org.machanism.machai.project.layout.ProjectLayout
 */
public class PythonBindexBuilder extends BindexBuilder {
    private static ResourceBundle promptBundle = ResourceBundle.getBundle("python_project_prompts");
    private static final String PROJECT_MODEL_FILE_NAME = "pyproject.toml";

    /**
     * Constructs a PythonBindexBuilder for Python projects with pyproject.toml.
     * @param projectLayout Project layout instance with directory info.
     */
    public PythonBindexBuilder(ProjectLayout projectLayout) {
        super(projectLayout);
    }

    /**
     * Provides project manifest and main sources context to GenAIProvider for  Bindex generation.
     * Reads pyproject.toml and sends Python source files as prompt context.
     * @throws IOException On file or prompt failure.
     */
    @Override
    public void projectContext() throws IOException {
        File pyprojectTomlFile = new File(getProjectLayout().getProjectDir(), PROJECT_MODEL_FILE_NAME);

        try (FileReader reader = new FileReader(pyprojectTomlFile)) {
            String prompt = MessageFormat.format(promptBundle.getString("project_build_section"),
                IOUtils.toString(reader));
            getGenAIProvider().prompt(prompt);
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
                    getGenAIProvider().promptFile(file, "source_resource_section");
                }
            }
        }

        String prompt = promptBundle.getString("additional_rules");
        getGenAIProvider().prompt(prompt);
    }
}
