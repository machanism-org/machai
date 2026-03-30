package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
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
 * {@link Reviewer} implementation for Java source files ({@code .java}).
 *
 * <p>This reviewer reads Java source as UTF-8 and detects the presence of the
 * {@link GuidanceProcessor#GUIDANCE_TAG_NAME @guidance} tag in either block ({@code /* ... *&#47;})
 * or line ({@code // ...}) comments.
 *
 * <p>When processing {@code package-info.java}, the reviewer emits a package-level prompt fragment that only
 * includes path context; for other Java files it emits a prompt fragment containing the full file content.
 */
public class JavaReviewer implements Reviewer {

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Returns the file extensions supported by this reviewer.
	 *
	 * @return an array containing {@code "java"}
	 */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "java" };
	}

	/**
	 * Reviews the provided Java file and, if guidance is present, returns a formatted prompt fragment.
	 *
	 * @param projectDir    the project root directory used to compute a project-relative path for context
	 * @param guidancesFile the Java file to analyze
	 * @return a formatted prompt fragment, or {@code null} when the file does not contain guidance
	 * @throws IOException if an error occurs while reading the file
	 */
	@Override
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String result = null;
		try {
			String content = new String(Files.readAllBytes(guidancesFile.toPath()), StandardCharsets.UTF_8);

			if (Strings.CS.contains(content, GuidanceProcessor.GUIDANCE_TAG_NAME)) {
				Pattern pattern = Pattern.compile(
						"(?:/\\*.*?" + GuidanceProcessor.GUIDANCE_TAG_NAME + "\\s*(.*?)\\s*\\*/)|"
								+ "(?://\\s*" + GuidanceProcessor.GUIDANCE_TAG_NAME + "\\s*(.*))",
						Pattern.DOTALL);
				Matcher matcher = pattern.matcher(content);
				if (matcher.find()) {
					if (Strings.CS.equals(guidancesFile.getName(), "package-info.java")) {
						String relatedFilePath = ProjectLayout.getRelativePath(projectDir, guidancesFile);
						result = MessageFormat.format(promptBundle.getString("java_package_info_file"), relatedFilePath);
					} else {
						String relativePath = ProjectLayout.getRelativePath(projectDir, guidancesFile);
						String name = guidancesFile.getName();
						result = MessageFormat.format(promptBundle.getString("java_file"), name, relativePath, content);
					}
				}
			}
		} catch (MalformedInputException e) {
			throw new IllegalArgumentException("File: " + guidancesFile.getAbsolutePath(), e);
		}
		return result;
	}

	/**
	 * Extracts the declared package name from a Java source snippet.
	 *
	 * @param content the full text content of a Java source file
	 * @return the declared package name, or {@code "<default package>"} when no package declaration is present
	 */
	public static String extractPackageName(String content) {
		Pattern pattern = Pattern.compile("\\bpackage\\s+([a-zA-Z_][\\w\\.]*);");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "<default package>";
	}
}
