package org.machanism.machai.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProjectProcessor {
	private static Logger logger = LoggerFactory.getLogger(ProjectProcessor.class);

	public void scanFolder(File projectDir) throws IOException {
		ProjectLayout projectLayout = getProjectLayout(projectDir);
		List<String> modules = projectLayout.getModules();

		if (modules != null) {
			for (String module : modules) {
				processModule(projectDir, module);
			}
		} else {
			projectLayout = getProjectLayout(projectDir);
			try {
				processFolder(projectLayout);
			} catch (Exception e) {
				logger.error("Project dir: + projectDir", e);
			}
		}
	}

	protected void processModule(File projectDir, String module) throws IOException {
		scanFolder(new File(projectDir, module));
	}

	public abstract void processFolder(ProjectLayout processor);

	protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
		return ProjectLayoutManager.detectProjectLayout(projectDir);
	}
}