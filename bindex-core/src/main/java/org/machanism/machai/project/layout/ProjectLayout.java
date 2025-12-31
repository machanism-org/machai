package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class ProjectLayout {

	public static final String[] EXCLUDE_DIRS = { "node_modules", ".git", ".nx", ".svn", ".machai", "target", "build",
			".venv", "__", ".pytest_cache", ".idea", ".egg-info", ".classpath", ".settings", "logs", ".settings", ".m2" };

	private File projectDir;

	public ProjectLayout projectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

	public File getProjectDir() {
		return projectDir;
	}

	public List<String> getModules() throws IOException {
		return null;
	};

	public String getRelatedPath(String currentPath, File file) {
		String relativePath = file.getAbsolutePath().replace("\\", "/").replace(currentPath, "");
		if (StringUtils.startsWith(relativePath, "/")) {
			relativePath = StringUtils.substring(relativePath, 1);
		}
		return relativePath;
	}

	public abstract List<String> getSources();

	public abstract List<String> getDocuments();

	public abstract List<String> getTests();

	public static String getRelatedPath(File dir, File file) {
		String currentPath = dir.getAbsolutePath().replace("\\", "/");
		String relativePath = file.getAbsolutePath().replace("\\", "/").replace(currentPath, "");
		if (StringUtils.startsWith(relativePath, "/")) {
			relativePath = StringUtils.substring(relativePath, 1);
		}
		return relativePath;
	}
}
