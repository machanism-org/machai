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

public class JavaReviewer implements Reviewer {

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());
		String result = null;
		if (StringUtils.contains(content, DocsProcessor.GUIDANCE_TAG_NAME)) {
			Pattern pattern = Pattern.compile("(?:/\\*.*?" + DocsProcessor.GUIDANCE_TAG_NAME
					+ ":\\s*(.*?)\\s*\\*/)|(?://\\s*" + DocsProcessor.GUIDANCE_TAG_NAME + ":\\s*(.*))", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				if (StringUtils.equals(guidancesFile.getName(), "package-info.java")) {
					String guidanceText = matcher.group(1).replaceAll("\\s*\\*\\s?", " ").trim();
					result = MessageFormat.format(promptBundle.getString("java_package_info_file"),
							ProjectLayout.getRelatedPath(projectDir, guidancesFile.getParentFile()), guidanceText);
				} else {
					String relatedPath = ProjectLayout.getRelatedPath(projectDir, guidancesFile);
					String name = guidancesFile.getName();
					result = MessageFormat.format(promptBundle.getString("java_file"), name, relatedPath, content);
				}
			}
		}
		return result;
	}

}
