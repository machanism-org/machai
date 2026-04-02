package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

class ReactorGWGetProjectLayoutTest {

	@Test
	void execute_shouldUseDetectedProjectLayoutAndPopulateMavenModel() throws Exception {
		// Arrange
		ReactorGW goal = new ReactorGW();
		goal.basedir = new File(".");

		MavenProject project = new MavenProject();
		project.setModel(new org.apache.maven.model.Model());
		goal.project = project;

		MavenSession session = mock(MavenSession.class);
		when(session.getExecutionRootDirectory()).thenReturn(new File(".").getAbsolutePath());
		goal.session = session;

		MavenProjectLayout layout = new MavenProjectLayout();

		try (org.mockito.MockedStatic<ProjectLayoutManager> mocked = mockStatic(ProjectLayoutManager.class)) {
			mocked.when(() -> ProjectLayoutManager.detectProjectLayout(any(File.class))).thenReturn(layout);

			PropertiesConfigurator config = new PropertiesConfigurator();
			GuidanceProcessor processor = new GuidanceProcessor(new File("."), null, config) {
				@Override
				public ProjectLayout getProjectLayout(File projectDir) throws java.io.FileNotFoundException {
					ProjectLayout projectLayout = ProjectLayoutManager.detectProjectLayout(projectDir);
					if (projectLayout instanceof MavenProjectLayout) {
						MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
						mavenProjectLayout.projectDir(projectDir);
						mavenProjectLayout.model(project.getModel());
					}
					return projectLayout;
				}
			};

			// Act
			ProjectLayout detected = processor.getProjectLayout(new File("."));

			// Assert
			assertSame(layout, detected);
			assertSame(project.getModel(), ((MavenProjectLayout) detected).getModel());
		}
	}
}
