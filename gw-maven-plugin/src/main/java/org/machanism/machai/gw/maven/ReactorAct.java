package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

@Mojo(name = "act-reactor", threadSafe = true)
public class ReactorAct extends Act {

	@Override
	public void execute() throws MojoExecutionException {
		PropertiesConfigurator configuration = getConfiguration();

		ActProcessor actProcessor = new ActProcessor(basedir, configuration, configuration.get("gw.model", genai)) {

			@Override
			public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = ProjectLayoutManager.detectProjectLayout(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
					mavenProjectLayout.projectDir(projectDir);
					Model model = project.getModel();
					mavenProjectLayout.model(model);
				}

				return projectLayout;
			}

			@Override
			protected void processModule(File projectDir, String module) throws IOException {
				// No-op for this implementation
			}
		};
		actProcessor.setNonRecursive(true);
		actProcessor.setModuleMultiThread(false);

		process(configuration, actProcessor);
	}

}
