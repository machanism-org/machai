package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

class ReactorGWGetProjectLayoutTest {

	@Test
	void execute_shouldUseDetectedProjectLayoutAndPopulateMavenModel() throws Exception {
		GWPerModuleMojo goal = new GWPerModuleMojo();
		goal.basedir = new File(".");

		MavenProject project = new MavenProject();
		project.setModel(new org.apache.maven.model.Model());
		goal.project = project;

		MavenProjectLayout layout = new MavenProjectLayout();
		PropertiesConfigurator config = new PropertiesConfigurator();
		GuidanceProcessor processor = new GuidanceProcessor(new File("."), null, config) {
			@Override
			public ProjectLayout getProjectLayout(File projectDir) {
				MavenProjectLayout mavenProjectLayout = layout;
				mavenProjectLayout.projectDir(projectDir);
				mavenProjectLayout.model(project.getModel());
				return mavenProjectLayout;
			}
		};

		ProjectLayout detected = processor.getProjectLayout(new File("."));

		assertSame(layout, detected);
		assertSame(project.getModel(), ((MavenProjectLayout) detected).getModel());
	}
}
