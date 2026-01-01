package org.machanism.machai.ghostwriter.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ghostwriter.DocsProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Reviewer implementation for Python source files (.py).
 * <p>
 * Extracts guidance information or comments annotated with the
 * {@link DocsProcessor#GUIDANCE_TAG_NAME} for documentation input processing,
 * supporting Python file comment conventions.
 */
public class PythonReviewer implements Reviewer {

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

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
		String content = Files.readString(guidancesFile.toPath());
		String result = null;
		if (StringUtils.contains(content, DocsProcessor.GUIDANCE_TAG_NAME)) {
			// Match guidance in Python comments: # @guidance: ... or triple-quoted
			// docstrings
			Pattern pattern = Pattern.compile("(?:#\\s*" + DocsProcessor.GUIDANCE_TAG_NAME + ":\\s*(.*))"
					+ "|(?:[\"']{3}\\s*" + DocsProcessor.GUIDANCE_TAG_NAME + ":\\s*(.*?)\\s*[\"']{3})", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				String guidanceText = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
				if (guidanceText != null) {
					String relatedPath = ProjectLayout.getRelatedPath(projectDir, guidancesFile);
					String name = guidancesFile.getName();
					result = MessageFormat.format(promptBundle.getString("python_file"), name, relatedPath,
							guidanceText.trim());
				}
			}
		}
		return result;
	}

}