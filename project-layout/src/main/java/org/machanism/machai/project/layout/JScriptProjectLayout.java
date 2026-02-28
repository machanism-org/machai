package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
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
		List<String> result = null;

		JsonNode packageJson = getPackageJson();
		JsonNode workspacesNode = packageJson.get("workspaces");
		if (workspacesNode != null) {
			Set<String> modules = new HashSet<>();

			if (workspacesNode.isArray()) {
				Iterator<JsonNode> iterator = workspacesNode.iterator();
				while (iterator.hasNext()) {
					String globPattern = iterator.next().asText();

					if (Strings.CS.startsWith(globPattern, "./")) {
						globPattern = StringUtils.substringAfter(globPattern, "./");
					}

					PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
					File baseDir = getProjectDir();
					List<File> files = ProjectLayout.findDirectories(baseDir);

					for (File file : files) {
						String path = ProjectLayout.getRelativePath(getProjectDir(), file, false);
						if (path == null) {
							continue;
						}
						Path pathToMatch = new File(path).toPath();

						if (matcher.matches(pathToMatch) && isPackageJsonPresent(file)) {
							String relativePath = ProjectLayout.getRelativePath(baseDir, file);
							if (relativePath != null) {
								modules.add(relativePath);
							}
						}
					}
				}
				result = new ArrayList<>(modules);
			}
		}

		return result;
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
	 * @return {@code null}; not currently implemented
	 */
	@Override
	public List<String> getSources() {
		return null;
	}

	/**
	 * Returns a list of conventional documentation directories for JS/TS projects.
	 *
	 * @return {@code null}; not currently implemented
	 */
	@Override
	public List<String> getDocuments() {
		return null;
	}

	/**
	 * Returns a list of conventional test directories for JS/TS projects.
	 *
	 * @return {@code null}; not currently implemented
	 */
	@Override
	public List<String> getTests() {
		return null;
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
