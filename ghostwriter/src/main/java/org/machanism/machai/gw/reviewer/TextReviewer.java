package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Reviewer implementation for generic guidance text files.
 *
 * <p>This reviewer only processes files named {@code @guidance.txt}. When such a file is found, its full contents
 * are returned formatted as a prompt fragment with directory context for downstream processing.
 */
public class TextReviewer implements Reviewer {

	private static final String GUIDANCE_FILE_NAME = "@guidance.txt";

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer.
	 *
	 * @return an array containing {@code "txt"}
	 */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "txt" };
	}

	/**
	 * Reads and formats the guidance file if the provided file is named {@code @guidance.txt}.
	 *
	 * @param projectDir the project root directory used to compute related paths for context
	 * @param guidancesFile the file to analyze
	 * @return the formatted guidance prompt, or {@code null} when the file is not a guidance file
	 * @throws IOException if an error occurs reading the file
	 */
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String guidance;
		if (Strings.CS.equals(guidancesFile.getName(), GUIDANCE_FILE_NAME)) {
			guidance = Files.readString(guidancesFile.toPath());
			guidance = getPrompt(projectDir, guidancesFile, guidance);
		} else {
			guidance = null;
		}

		return guidance;
	}

	/**
	 * Formats the raw guidance content into a prompt fragment.
	 *
	 * @param projectDir the project root directory used to compute related paths for context
	 * @param guidancesFile the guidance file (used to compute the parent directory context)
	 * @param guidance the raw guidance content
	 * @return the formatted prompt fragment, or the original guidance content if blank
	 */
	public String getPrompt(File projectDir, File guidancesFile, String guidance) {
		if (StringUtils.isNotBlank(guidance)) {
			String parentsPath = ProjectLayout.getRelativePath(projectDir, guidancesFile.getParentFile());
			guidance = MessageFormat.format(promptBundle.getString("guidance_file"), parentsPath, guidance);
		}
		return guidance;
	}

}
