package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * {@link Reviewer} implementation for Markdown files ({@code .md}).
 *
 * <p>Guidance is expected to be embedded in an HTML comment block that contains the
 * {@link GuidanceProcessor#GUIDANCE_TAG_NAME @guidance} tag.
 */
public class MarkdownReviewer implements Reviewer {

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer.
	 *
	 * @return an array containing {@code "md"}
	 */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "md" };
	}

	/**
	 * Reviews the given Markdown file and returns a formatted prompt fragment when guidance is present.
	 *
	 * @param projectDir    the project root directory used to compute a project-relative path for context
	 * @param guidancesFile the Markdown file to analyze
	 * @return a formatted prompt fragment, or {@code null} when the file does not contain guidance
	 * @throws IOException if an error occurs while reading the file
	 */
	@Override
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = IOUtils.toString(guidancesFile.toURI(), StandardCharsets.UTF_8);

		Pattern pattern = Pattern.compile(
				"<!--.*?" + GuidanceProcessor.GUIDANCE_TAG_NAME + "(.*?)(?:-->|\\Z)",
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		if (!matcher.find()) {
			return null;
		}

		String relativePath = ProjectLayout.getRelativePath(projectDir, guidancesFile);
		return MessageFormat.format(promptBundle.getString("markdown_file"), relativePath, content);
	}
}
