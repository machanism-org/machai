package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

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
	public static final String PROJECT_MODEL_FILE_NAME = "package.json";

	/**
	 * Checks if the specified directory contains a <code>package.json</code> file,
	 * indicating a JS/TS project.
	 *
	 * @param projectDir Directory to check
	 * @return <code>true</code> if <code>package.json</code> is present; otherwise
	 *         <code>false</code>
	 */
	public static boolean isPackageJsonPresent(File projectDir) {
		return new File(projectDir, PROJECT_MODEL_FILE_NAME).exists();
	}

	/**
	 * Gets workspace modules listed in <code>package.json</code> under the
	 * "workspaces" key.
	 *
	 * @return List of relative module paths, or <code>null</code> if not applicable
	 * @throws IOException if reading <code>package.json</code> fails
	 * @see #getPackageJson()
	 * 
	 * <pre>
	 * Example usage:
	 * JScriptProjectLayout layout = new JScriptProjectLayout();
	 * List<String> modules = layout.projectDir(new File("/repo")).getModules();
	 * </pre>
	 */
	@Override
	public List<String> getModules() throws IOException {
		List<String> result = null;

		JsonNode packageJson = getPackageJson();
		JsonNode workspacesNode = packageJson.get("workspaces");
		if (workspacesNode != null) {
			List<String> modules = new ArrayList<String>();

			if (workspacesNode.isArray()) {
				Iterator<JsonNode> iterator = workspacesNode.iterator();
				while (iterator.hasNext()) {
					String module = iterator.next().asText();

					String requiredStartWith = StringUtils.substringBefore(module, "**");
					File dirToScan = new File(getProjectDir(), requiredStartWith);

					try (Stream<Path> stream = Files.walk(dirToScan.toPath())) {
						stream.filter(p -> {
							File file = p.toFile();
							boolean containsAny = StringUtils.containsAny(file.getAbsolutePath(), EXCLUDE_DIRS);
							boolean isProjectBuildFile = StringUtils.equals(file.getName(), PROJECT_MODEL_FILE_NAME);
							if (isProjectBuildFile) {
								if (!containsAny) {
									return true;
								}
							}
							return false;
						}).forEach(p -> {
							File dir = p.toFile().getParentFile();
							String relativePath = ProjectLayout.getRelatedPath(getProjectDir(), dir);
							modules.add(relativePath);
						});
					}
				}
				result = modules;
			}
		}

		return result;
	}

	/**
	 * Loads and parses <code>package.json</code> in the current project directory.
	 *
	 * @return JsonNode root of <code>package.json</code>
	 * @throws IOException if read/parse fails
	 */
	private JsonNode getPackageJson() throws IOException {
		File packageFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);
		JsonNode packageJson = new ObjectMapper().readTree(packageFile);
		return packageJson;
	}

	/**
	 * Retrieves source directories for JS/TS projects.
	 * <p>
	 * Not implemented in this class.
	 * </p>
	 * 
	 * @return always <code>null</code>
	 */
	@Override
	public List<String> getSources() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Retrieves documentation sources for JS/TS projects.
	 * <p>
	 * Not implemented in this class.
	 * </p>
	 * 
	 * @return always <code>null</code>
	 */
	@Override
	public List<String> getDocuments() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Retrieves test sources for JS/TS projects.
	 * <p>
	 * Not implemented in this class.
	 * </p>
	 * 
	 * @return always <code>null</code>
	 */
	@Override
	public List<String> getTests() {
		// TODO Auto-generated method stub
		return null;
	}

}
