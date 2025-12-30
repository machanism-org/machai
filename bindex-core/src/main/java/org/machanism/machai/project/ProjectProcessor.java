package org.machanism.machai.project;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProjectProcessor {
	private static Logger logger = LoggerFactory.getLogger(ProjectProcessor.class);

	public void scanProjects(File projectDir) throws IOException {
		ProjectLayout projectLayout = ProjectLayoutManager.builder(projectDir);
		List<String> modules = projectLayout.getModules();

		if (modules != null) {
			for (String module : modules) {
				scanProjects(new File(projectDir, module));
			}
		} else {
			projectLayout = ProjectLayoutManager.builder(projectDir);
			try {
				processProject(projectLayout);
			} catch (Exception e) {
				logger.error("Project dir: + projectDir", e);
			}
		}
	}

	public abstract void processProject(ProjectLayout processor);

	public static String getRelatedPath(File dir, File file) {
		String currentPath = dir.getAbsolutePath().replace("\\", "/");
		String relativePath = file.getAbsolutePath().replace("\\", "/").replace(currentPath, "");
		if (StringUtils.startsWith(relativePath, "/")) {
			relativePath = StringUtils.substring(relativePath, 1);
		}
		return relativePath;
	}
}