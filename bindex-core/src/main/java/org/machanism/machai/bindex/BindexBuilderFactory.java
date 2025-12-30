package org.machanism.machai.bindex;

import java.io.FileNotFoundException;

import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.bindex.builder.JScriptBindexBuilder;
import org.machanism.machai.bindex.builder.MavenBindexBuilder;
import org.machanism.machai.bindex.builder.PythonBindexBuilder;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

public class BindexBuilderFactory {

	public static BindexBuilder create(ProjectLayout projectLayout) throws FileNotFoundException {
		BindexBuilder bindexBuilder;
		if (projectLayout instanceof MavenProjectLayout) {
			bindexBuilder = new MavenBindexBuilder((MavenProjectLayout) projectLayout);
		} else if (projectLayout instanceof JScriptProjectLayout) {
			bindexBuilder = new JScriptBindexBuilder((JScriptProjectLayout) projectLayout);
		} else if (projectLayout instanceof PythonProjectLayout) {
			bindexBuilder = new PythonBindexBuilder((PythonProjectLayout) projectLayout);
		} else if (projectLayout.getProjectDir().exists()) {
			bindexBuilder = new BindexBuilder(projectLayout);
		} else {
			throw new FileNotFoundException(projectLayout.getProjectDir().getAbsolutePath());
		}

		return bindexBuilder;
	}

}
