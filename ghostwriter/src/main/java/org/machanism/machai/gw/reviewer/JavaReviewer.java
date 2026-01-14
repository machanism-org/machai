package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Reviewer implementation for Java source files (.java).
 * <p>
 * Extracts guidance information or comments annotated with the
 * {@link FileProcessor#GUIDANCE_TAG_NAME} for documentation input processing,
 * including support for package-info.java and regular Java files.
 */
public class JavaReviewer implements Reviewer {

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer. This reviewer
	 * processes files with extension: java.
	 *
	 * @return an array of supported file extension strings
	 */
	@Override
	public String[] getSupportedFileExtentions() {
		return new String[] { "java" };
	}

	/**
	 * Performs analysis on the specified Java source or package-info file,
	 * extracting documentation guidance if marked with the appropriate tag.
	 *
	 * @param projectDir    the root directory of the project for context
	 * @param guidancesFile the Java file to be analyzed
	 * @return formatted documentation guidance or {@code null} if none found
	 * @throws IOException if an error occurs reading the file
	 */
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = Files.readString(guidancesFile.toPath());
		String result = null;
		if (StringUtils.contains(content, FileProcessor.GUIDANCE_TAG_NAME)) {
			Pattern pattern = Pattern.compile("(?:/\\*.*?" + FileProcessor.GUIDANCE_TAG_NAME
					+ "\\s*(.*?)\\s*\\*/)|(?://\\s*" + FileProcessor.GUIDANCE_TAG_NAME + "\\s*(.*))", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				if (StringUtils.equals(guidancesFile.getName(), "package-info.java")) {
					String relatedFilePath = ProjectLayout.getRelatedPath(projectDir, guidancesFile);
					result = MessageFormat.format(promptBundle.getString("java_package_info_file"), relatedFilePath);
				} else {
					String relatedPath = ProjectLayout.getRelatedPath(projectDir, guidancesFile);
					String name = guidancesFile.getName();
					result = MessageFormat.format(promptBundle.getString("java_file"), name, relatedPath, content);
				}
			}
		}
		return result;
	}

	/**
	 * Extracts the package name from the content of a package-info.java file.
	 * 
	 * @param content The full text content of the package-info.java file.
	 * @return The package name, or null if not found.
	 */
	public static String extractPackageName(String content) {
		// Regex: matches 'package' followed by whitespace, then the package name,
		// ending with a semicolon
		Pattern pattern = Pattern.compile("\\bpackage\\s+([a-zA-Z_][\\w\\.]*);");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "<default package>";
	}
}
