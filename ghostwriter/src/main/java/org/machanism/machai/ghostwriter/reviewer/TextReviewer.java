package org.machanism.machai.ghostwriter.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ghostwriter.DocsProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

public class TextReviewer implements Reviewer {

	private static final String GUIDANCE_FILE_NAME = DocsProcessor.GUIDANCE_TAG_NAME + ".txt";

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");
	private Map<String, String> dirGuidanceMap;

	public TextReviewer(Map<String, String> dirGuidanceMap) {
		super();
		this.dirGuidanceMap = dirGuidanceMap;
	}

	public String perform(File projectDir, File guidancesFile) throws IOException {
		String guidance = Files.readString(guidancesFile.toPath());
		if (StringUtils.equals(guidancesFile.getName(), GUIDANCE_FILE_NAME)) {
			if (StringUtils.isNotBlank(guidance)) {
				String parentsPath = ProjectLayout.getRelatedPath(projectDir, guidancesFile.getParentFile());
				guidance = MessageFormat.format(promptBundle.getString("guidance_file"), parentsPath, guidance);
				dirGuidanceMap.put("/" + parentsPath, guidance);
			}
		}

		return guidance;
	}

}
