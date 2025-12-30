package org.machanism.machai.project;

import java.io.File;
import java.io.FileNotFoundException;

import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

public class ProjectLayoutManager {

	public static ProjectLayout builder(File projectDir) throws FileNotFoundException {
		ProjectLayout projectLayout;
		if (MavenProjectLayout.isMavenProject(projectDir)) {
			projectLayout = new MavenProjectLayout();
		} else if (JScriptProjectLayout.isPackageJsonPresent(projectDir)) {
			projectLayout = new JScriptProjectLayout();
		} else if (PythonProjectLayout.isPythonProject(projectDir)) {
			projectLayout = new PythonProjectLayout();
		} else if (projectDir.exists()) {
			projectLayout = new DefaultProjectLayout();
		} else {
			throw new FileNotFoundException(projectDir.getAbsolutePath());
		}

		projectLayout.projectDir(projectDir);
		return projectLayout;
	}

}
