package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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

	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "ts" };
	}

	@Override
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = new String(Files.readAllBytes(guidancesFile.toPath()), StandardCharsets.UTF_8);
		if (!content.contains(GuidanceProcessor.GUIDANCE_TAG_NAME)) {
			return null;
		}

		Matcher lineMatcher = Pattern.compile("(?m)^\\s*//\\s*" + Pattern.quote(GuidanceProcessor.GUIDANCE_TAG_NAME)
				+ "\\s*:?[ \\t]*(.*)$").matcher(content);
		if (lineMatcher.find()) {
			String guidanceText = lineMatcher.group(1);
			if (StringUtils.isBlank(guidanceText)) {
				return null;
			}
			return MessageFormat.format(promptBundle.getString("typescript_file"), guidancesFile.getName(),
					ProjectLayout.getRelativePath(projectDir, guidancesFile), guidanceText.trim());
		}

		Matcher blockMatcher = Pattern.compile("/\\*\\s*" + Pattern.quote(GuidanceProcessor.GUIDANCE_TAG_NAME)
				+ "\\s*:?[ \\t]*(.*?)\\s*\\*/", Pattern.DOTALL).matcher(content);
		if (blockMatcher.find()) {
			String guidanceText = blockMatcher.group(1);
			if (StringUtils.isBlank(guidanceText)) {
				return null;
			}
			return MessageFormat.format(promptBundle.getString("typescript_file"), guidancesFile.getName(),
					ProjectLayout.getRelativePath(projectDir, guidancesFile), guidanceText.trim());
		}

		return null;
	}
}
