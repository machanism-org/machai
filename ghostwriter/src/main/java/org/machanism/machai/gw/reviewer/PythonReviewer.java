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
 * {@link Reviewer} implementation for Python source files ({@code .py}).
 *
 * <p>Guidance may appear in either single-line comments ({@code #}) or in triple-quoted strings that contain
 * the {@link GuidanceProcessor#GUIDANCE_TAG_NAME @guidance} tag.
 */
public class PythonReviewer implements Reviewer {

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "py" };
	}

	@Override
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = new String(Files.readAllBytes(guidancesFile.toPath()), StandardCharsets.UTF_8);
		if (!content.contains(GuidanceProcessor.GUIDANCE_TAG_NAME)) {
			return null;
		}

		Matcher lineMatcher = Pattern.compile("(?m)^\\s*#\\s*" + Pattern.quote(GuidanceProcessor.GUIDANCE_TAG_NAME)
				+ "\\s*:?[ \\t]*(.*)$").matcher(content);
		if (lineMatcher.find()) {
			String guidanceText = lineMatcher.group(1);
			if (StringUtils.isBlank(guidanceText)) {
				return null;
			}
			return MessageFormat.format(promptBundle.getString("python_file"), guidancesFile.getName(),
					ProjectLayout.getRelativePath(projectDir, guidancesFile), guidanceText.trim());
		}

		Matcher tripleMatcher = Pattern.compile("(?:\"\"\"|''')\\s*" + Pattern.quote(GuidanceProcessor.GUIDANCE_TAG_NAME)
				+ "\\s*:?[ \\t]*(.*?)(?:\"\"\"|''')", Pattern.DOTALL).matcher(content);
		if (tripleMatcher.find()) {
			String guidanceText = tripleMatcher.group(1);
			if (StringUtils.isBlank(guidanceText)) {
				return null;
			}
			return MessageFormat.format(promptBundle.getString("python_file"), guidancesFile.getName(),
					ProjectLayout.getRelativePath(projectDir, guidancesFile), guidanceText.trim());
		}

		return null;
	}

}
