package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.gw.DocsProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Reviewer implementation for generic text files (.txt).
 * <p>
 * Detects guidance in text files and prepares it for documentation workflows,
 * including context propagation by directory.
 */
public class TextReviewer implements Reviewer {

	private static final String GUIDANCE_FILE_NAME = DocsProcessor.GUIDANCE_TAG_NAME + ".txt";

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");
	private Map<String, String> dirGuidanceMap;

	/**
	 * Returns the file extensions supported by this reviewer. This reviewer handles
	 * files with the 'txt' extension.
	 *
	 * @return an array of supported file extension strings
	 */
	@Override
	public String[] getSupportedFileExtentions() {
		return new String[] { "txt" };
	}

	/**
	 * Analyzes a text file for documentation guidance and, if present, updates the
	 * context map.
	 *
	 * @param projectDir    the project context root directory
	 * @param guidancesFile the text file to review
	 * @return extracted or formatted guidance, or {@code null} if not applicable
	 * @throws IOException if an error occurs reading the file
	 */
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String guidance = Files.readString(guidancesFile.toPath());
		if (StringUtils.equals(guidancesFile.getName(), GUIDANCE_FILE_NAME)) {
			if (StringUtils.isNotBlank(guidance)) {
				String parentsPath = ProjectLayout.getRelatedPath(projectDir, guidancesFile.getParentFile());
				guidance = MessageFormat.format(promptBundle.getString("guidance_file"), parentsPath, guidance);
				dirGuidanceMap.put(parentsPath, guidance);
			}
		} else {
			guidance = null;
		}

		return guidance;
	}

	@Override
	public void setDirGuidanceMap(Map<String, String> dirGuidanceMap) {
		this.dirGuidanceMap = dirGuidanceMap;
	}
}
