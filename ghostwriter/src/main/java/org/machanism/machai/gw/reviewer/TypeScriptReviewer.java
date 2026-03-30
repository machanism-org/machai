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
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * {@link Reviewer} implementation for TypeScript source files ({@code .ts}).
 *
 * <p>Guidance may be embedded in either line comments ({@code //}) or block comments ({@code /* ... *&#47;})
 * containing the {@link GuidanceProcessor#GUIDANCE_TAG_NAME @guidance} tag.
 */
public class TypeScriptReviewer implements Reviewer {

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer.
	 *
	 * @return an array containing {@code "ts"}
	 */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "ts" };
	}

	/**
	 * Reviews the provided TypeScript file and, if guidance is present, returns a formatted prompt fragment.
	 *
	 * @param projectDir    the project root directory used to compute a project-relative path for context
	 * @param guidancesFile the TypeScript file to analyze
	 * @return a formatted prompt fragment, or {@code null} when the file does not contain guidance
	 * @throws IOException if an error occurs while reading the file
	 */
	@Override
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = new String(Files.readAllBytes(guidancesFile.toPath()), StandardCharsets.UTF_8);
		if (!Strings.CS.contains(content, GuidanceProcessor.GUIDANCE_TAG_NAME)) {
			return null;
		}

		Pattern pattern = Pattern.compile(
				"(?://\\s*" + GuidanceProcessor.GUIDANCE_TAG_NAME + "\\s*(.*))"
						+ "|(?:/\\*.*?" + GuidanceProcessor.GUIDANCE_TAG_NAME + "\\s*(.*?)\\s*\\*/)",
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		if (!matcher.find()) {
			return null;
		}

		String guidanceText = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
		if (guidanceText == null) {
			return null;
		}

		String relativePath = ProjectLayout.getRelativePath(projectDir, guidancesFile);
		return MessageFormat.format(
				promptBundle.getString("typescript_file"),
				guidancesFile.getName(),
				relativePath,
				guidanceText.trim());
	}
}
