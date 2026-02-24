package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.machanism.machai.gw.processor.FileProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Reviewer implementation for PlantUML files (.puml).
 * <p>
 * Extracts documented guidance from PlantUML content using a special tag for input into automated documentation workflows.
 */
public class PumlReviewer implements Reviewer {

    private static final ResourceBundle PROMPT_BUNDLE = ResourceBundle.getBundle("document-prompts");

    /**
     * Returns the file extensions supported by this reviewer. This reviewer handles files with the 'puml' extension.
     *
     * @return an array of supported file extension strings
     */
    @Override
    public String[] getSupportedFileExtensions() {
        return new String[] { "puml" };
    }

    /**
     * Reviews the given PlantUML file for contained guidance comments and formats the content for input to documentation generators.
     *
     * @param projectDir the root directory of the project for context
     * @param guidancesFile the PlantUML file to be analyzed
     * @return formatted documentation guidance or {@code null} if none found
     * @throws IOException if an error occurs reading the file
     */
    @Override
    public String perform(File projectDir, File guidancesFile) throws IOException {
        Objects.requireNonNull(projectDir, "projectDir must not be null");
        Objects.requireNonNull(guidancesFile, "guidancesFile must not be null");

        String content = FileUtils.readFileToString(guidancesFile, StandardCharsets.UTF_8);

        if (!content.contains(FileProcessor.GUIDANCE_TAG_NAME)) {
            return null;
        }

        String relativePath = ProjectLayout.getRelativePath(projectDir, guidancesFile);
        return MessageFormat.format(PROMPT_BUNDLE.getString("puml_file"), relativePath, content);
    }
}
