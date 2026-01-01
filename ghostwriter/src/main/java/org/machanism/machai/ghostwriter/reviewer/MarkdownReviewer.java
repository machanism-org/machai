package org.machanism.machai.ghostwriter.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.machanism.machai.ghostwriter.DocsProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

public class MarkdownReviewer implements Reviewer {

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());

		Pattern pattern = Pattern.compile("\\[" + DocsProcessor.GUIDANCE_TAG_NAME + "\\]:\\s*#\\s*\\((.*?)\\)",
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		String result = null;
		if (matcher.find()) {
			result = MessageFormat.format(promptBundle.getString("markdown_file"), guidancesFile.getName(),
					ProjectLayout.getRelatedPath(projectDir, guidancesFile), content);
		}

		return result;
	}

}
