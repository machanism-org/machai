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
 * Reviewer implementation for Markdown files (.md).
 * <p>
 * Extracts documented guidance from Markdown content using a special tag for
 * input into automated documentation workflows.
 */
public class MarkdownReviewer implements Reviewer {

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer. This reviewer handles
	 * files with the 'md' extension.
	 *
	 * @return an array of supported file extension strings
	 */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "md" };
	}

	/**
	 * Reviews the given Markdown file for contained guidance comments and formats
	 * the content for input to documentation generators.
	 *
	 * @param projectDir    the root directory of the project for context
	 * @param guidancesFile the Markdown file to be analyzed
	 * @return formatted documentation guidance or {@code null} if none found
	 * @throws IOException if an error occurs reading the file
	 */
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());

		Pattern pattern = Pattern.compile("<!--.*?" + FileProcessor.GUIDANCE_TAG_NAME + "(.*?)(?:-->|\\Z)",
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		String result = null;
		if (matcher.find()) {
			result = MessageFormat.format(promptBundle.getString("markdown_file"),
					ProjectLayout.getRelatedPath(projectDir, guidancesFile), content);
		}

		return result;
	}
}
