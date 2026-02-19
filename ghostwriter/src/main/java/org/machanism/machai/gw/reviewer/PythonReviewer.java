package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Strings;
import org.machanism.machai.gw.processor.FileProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Reviewer implementation for Python source files (.py).
 * <p>
 * Extracts guidance information or comments annotated with the
 * {@link FileProcessor#GUIDANCE_TAG_NAME} for documentation input processing,
 * supporting Python file comment conventions.
 */
public class PythonReviewer implements Reviewer {

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer.
	 * This reviewer handles files with the 'py' extension.
	 *
	 * @return an array of supported file extension strings
	 */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "py" };
	}

	/**
	 * Performs analysis on the specified Python source file, extracting
	 * documentation guidance if marked with the appropriate tag.
	 *
	 * @param projectDir    the root directory of the project for context
	 * @param guidancesFile the Python file to be analyzed
	 * @return formatted documentation guidance or {@code null} if none found
	 * @throws IOException if an error occurs reading the file
	 */
	public String perform(File projectDir, File guidancesFile) throws IOException {
	    // Java 8 compatible file reading
	    String content = new String(Files.readAllBytes(guidancesFile.toPath()), StandardCharsets.UTF_8);
	    String result = null;
	    if (Strings.CS.contains(content, FileProcessor.GUIDANCE_TAG_NAME)) {
	        Pattern pattern = Pattern.compile(
	            "(?:#\\s*" + FileProcessor.GUIDANCE_TAG_NAME + "\\s*(.*))"
	            + "|(?:[\"']{3}\\s*" + FileProcessor.GUIDANCE_TAG_NAME + "\\s*(.*?)\\s*[\"']{3})",
	            Pattern.DOTALL
	        );
	        Matcher matcher = pattern.matcher(content);
	        if (matcher.find()) {
	            String guidanceText = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
	            if (guidanceText != null) {
	                String relativePath = ProjectLayout.getRelativePath(projectDir, guidancesFile);
	                String name = guidancesFile.getName();
	                result = MessageFormat.format(
	                    promptBundle.getString("python_file"),
	                    name, relativePath, guidanceText.trim()
	                );
	            }
	        }
	    }
	    return result;
	}

}