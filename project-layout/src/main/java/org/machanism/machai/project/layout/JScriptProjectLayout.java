package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A project layout utility for JavaScript/TypeScript-based projects.
 * <p>
 * Scans for workspace modules defined in <code>package.json</code>, and
 * determines project sources, documents, and test directories.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class JScriptProjectLayout extends ProjectLayout {

	/** Name of the JS/TS project model file used to detect this layout. */
	public static final String PROJECT_MODEL_FILE_NAME = "package.json";

	/**
	 * Checks if the specified directory contains a <code>package.json</code> file,
	 * indicating a JS/TS project.
	 *
	 * @param projectDir directory to check
	 * @return {@code true} if <code>package.json</code> is present; otherwise {@code false}
	 */
	public static boolean isPackageJsonPresent(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	/**
	 * Returns workspace modules listed in <code>package.json</code> under the
	 * {@code workspaces} key.
	 *
	 * <p>
	 * When {@code workspaces} is an array of glob patterns, this method searches
	 * directories under the configured project root and returns those that match a
	 * workspace pattern and contain a <code>package.json</code>.
	 * </p>
	 *
	 * @return list of relative module paths, or {@code null} when the project does not
	 *         define workspaces
	 * @throws IllegalArgumentException if {@code package.json} cannot be read or parsed
	 */
	@Override
	public List<String> getModules() {
		JsonNode packageJson = getPackageJson();
		JsonNode workspacesNode = packageJson.get("workspaces");
		if (workspacesNode == null) {
			// Sonar java:S1168 - return an empty collection instead of null (kept as null for backward compatibility).
			return null;
		}

		Set<String> modules = new HashSet<>();
		if (workspacesNode.isArray()) {
			collectWorkspaceModules(workspacesNode, modules);
		}

		return new ArrayList<>(modules);
	}

	/**
	 * Collects workspace module directories matching glob patterns listed in the
	 * {@code workspaces} array.
	 */
	private void collectWorkspaceModules(JsonNode workspacesNode, Set<String> modules) {
		File baseDir = getProjectDir();
		List<File> directories = ProjectLayout.findDirectories(baseDir);

		Iterator<JsonNode> iterator = workspacesNode.iterator();
		while (iterator.hasNext()) {
			String globPattern = iterator.next().asText();
			String normalizedPattern = normalizeWorkspaceGlob(globPattern);
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + normalizedPattern);
			collectMatchingModules(directories, matcher, baseDir, modules);
		}
	}

	// Sonar java:S3776 - split complex method into smaller helpers to reduce cognitive complexity.
	private static String normalizeWorkspaceGlob(String globPattern) {
		String normalized = globPattern;
		if (Strings.CS.startsWith(normalized, "./")) {
			normalized = StringUtils.substringAfter(normalized, "./");
		}
		return normalized;
	}

	private static void collectMatchingModules(List<File> directories, PathMatcher matcher, File baseDir, Set<String> modules) {
		for (File dir : directories) {
			String relativePath = ProjectLayout.getRelativePath(baseDir, dir, false);
			if (relativePath == null) {
				continue;
			}

			if (!matcher.matches(new File(relativePath).toPath())) {
				continue;
			}

			if (isPackageJsonPresent(dir)) {
				modules.add(relativePath);
			}
		}
	}

	/**
	 * Loads and parses <code>package.json</code> in the current project directory.
	 *
	 * @return root JSON node of <code>package.json</code>
	 * @throws IllegalArgumentException if reading/parsing fails
	 */
	private JsonNode getPackageJson() {
		File packageFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);
		try {
			return new ObjectMapper().readTree(packageFile);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Returns a list of conventional source directories for JS/TS projects.
	 *
	 * @return empty list; not currently implemented
	 */
	@Override
	public List<String> getSources() {
		// Sonar java:S1168 - return an empty collection instead of null.
		return Collections.emptyList();
	}

	/**
	 * Returns a list of conventional documentation directories for JS/TS projects.
	 *
	 * @return empty list; not currently implemented
	 */
	@Override
	public List<String> getDocuments() {
		// Sonar java:S1168 - return an empty collection instead of null.
		return Collections.emptyList();
	}

	/**
	 * Returns a list of conventional test directories for JS/TS projects.
	 *
	 * @return empty list; not currently implemented
	 */
	@Override
	public List<String> getTests() {
		// Sonar java:S1168 - return an empty collection instead of null.
		return Collections.emptyList();
	}

	/**
	 * Sets the project directory and narrows the return type for fluent usage.
	 *
	 * @param projectDir project root directory
	 * @return this layout instance
	 */
	@Override
	public JScriptProjectLayout projectDir(File projectDir) {
		return (JScriptProjectLayout) super.projectDir(projectDir);
	}

	/**
	 * Returns the package name from <code>package.json</code>.
	 *
	 * @return package name
	 */
	@Override
	public String getProjectId() {
		return getPackageJson().get("name").asText();
	}
}
