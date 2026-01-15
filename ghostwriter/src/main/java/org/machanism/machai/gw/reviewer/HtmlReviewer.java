package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Reviewer implementation for HTML files (.html, .htm).
 * <p>
 * Extracts documented guidance from HTML content using a special tag for input
 * into automated documentation workflows.
 */
public class HtmlReviewer implements Reviewer {

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer.
	 * This reviewer analyzes files with extensions: html, htm, and xml.
	 *
	 * @return an array of supported file extensions
	 */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "html", "htm", "xml" };
	}

	/**
	 * Reviews the given HTML file for contained guidance comments and formats the
	 * content for input to documentation generators.
	 *
	 * @param projectDir    the root directory of the project for context
	 * @param guidancesFile the HTML file to be analyzed
	 * @return formatted documentation guidance or {@code null} if none found
	 * @throws IOException if an error occurs reading the file
	 */
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());

		Pattern pattern = Pattern.compile("<!--\\s*" + FileProcessor.GUIDANCE_TAG_NAME + "\\s*(.*?)\\s*-->",
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		String result = null;
		if (matcher.find()) {
			result = MessageFormat.format(promptBundle.getString("html_file"), guidancesFile.getName(),
					ProjectLayout.getRelatedPath(projectDir, guidancesFile), content);
		}

		return result;
	}

}