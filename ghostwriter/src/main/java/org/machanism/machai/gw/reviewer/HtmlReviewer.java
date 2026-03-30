package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * {@link Reviewer} implementation for HTML and XML files ({@code .html}, {@code .htm}, {@code .xml}).
 *
 * <p>Guidance is expected to appear in an HTML/XML comment block (for example
 * {@code <!-- @guidance ... -->}).
 */
public class HtmlReviewer implements Reviewer {

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer.
	 *
	 * @return an array containing {@code "html"}, {@code "htm"}, and {@code "xml"}
	 */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "html", "htm", "xml" };
	}

	/**
	 * Reviews the given file and returns a formatted prompt fragment when guidance is present.
	 *
	 * @param projectDir    the project root directory used to compute a project-relative path for context
	 * @param guidancesFile the HTML/XML file to analyze
	 * @return a formatted prompt fragment, or {@code null} when the file does not contain guidance
	 * @throws IOException if an error occurs while reading the file
	 */
	@Override
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = new String(Files.readAllBytes(guidancesFile.toPath()), StandardCharsets.UTF_8);

		Pattern pattern = Pattern.compile(
				"<!--\\s*" + GuidanceProcessor.GUIDANCE_TAG_NAME + "\\s*(.*?)\\s*-->",
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		if (!matcher.find()) {
			return null;
		}

		return MessageFormat.format(
				promptBundle.getString("html_file"),
				guidancesFile.getName(),
				ProjectLayout.getRelativePath(projectDir, guidancesFile),
				content);
	}

}
