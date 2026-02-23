package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.machanism.machai.gw.processor.FileProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Reviewer implementation for PlantUML files (.puml).
 * <p>
 * Extracts documented guidance from PlantUML content using a special tag for
 * input into automated documentation workflows.
 */
public class PumlReviewer implements Reviewer {

    private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

    /**
     * Returns the file extensions supported by this reviewer. This reviewer handles
     * files with the 'puml' extension.
     *
     * @return an array of supported file extension strings
     */
    @Override
    public String[] getSupportedFileExtensions() {
        return new String[] { "puml" };
    }

    /**
     * Reviews the given PlantUML file for contained guidance comments and formats
     * the content for input to documentation generators.
     *
     * @param projectDir    the root directory of the project for context
     * @param guidancesFile the PlantUML file to be analyzed
     * @return formatted documentation guidance or {@code null} if none found
     * @throws IOException if an error occurs reading the file
     */
    @Override
    public String perform(File projectDir, File guidancesFile) throws IOException {
        String content = IOUtils.toString(guidancesFile.toURI(), "utf-8");

        // PlantUML comments can be either ' (single quote) or '/* ... */'
        // We'll look for @guidance: in either single-line or block comments
        Pattern pattern = Pattern.compile("(?://|')\\s*" + FileProcessor.GUIDANCE_TAG_NAME + "(.*?)(?:\\n|$)|/\\*.*?" + FileProcessor.GUIDANCE_TAG_NAME + "(.*?)\\*/",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        String result = null;
        if (matcher.find()) {
            String relativePath = ProjectLayout.getRelativePath(projectDir, guidancesFile);
            result = MessageFormat.format(promptBundle.getString("puml_file"),
                    relativePath, content);
        }

        return result;
    }
}
