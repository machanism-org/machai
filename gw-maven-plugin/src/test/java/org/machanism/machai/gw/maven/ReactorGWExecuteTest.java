package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertThrows;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.mockito.Mockito;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.apache.maven.model.Model;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import java.lang.reflect.Field;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReactorGW#execute()} exception handling.
 */
public class ReactorGWExecuteTest {

	@Test
	public void execute_whenScanDocumentsThrowsProcessTerminationException_wrapsInMojoExecutionException() throws Exception {
		// Arrange
		ReactorGW gw = Mockito.spy(new ReactorGW());

		gw.project = new MavenProject();
		gw.project.setFile(new File("pom.xml"));
		gw.basedir = new File(".").getAbsoluteFile();

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getExecutionRootDirectory()).thenReturn(gw.basedir.getAbsolutePath());
		Mockito.when(session.getUserProperties()).thenReturn(new java.util.Properties());
		gw.session = session;

		java.lang.reflect.Field settingsField = AbstractGWGoal.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(gw, new Settings());

		ProcessTerminationException pte = new ProcessTerminationException("stop", 9);
		Mockito.doThrow(pte).when(gw)
				.scanDocuments(Mockito.any(org.machanism.machai.gw.processor.GuidanceProcessor.class));

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, gw::execute);
		org.junit.Assert.assertTrue(ex.getMessage().contains("exit code: 9"));
		org.junit.Assert.assertSame(pte, ex.getCause());
	}

    @Test
	public void execute_whenScanDirIsNull_shouldDefaultToBasedir() throws Exception {
  // TestMate-c35094c34a9f8a04f2a6d803e13d5701
		// Arrange
		ReactorGW gw = spy(new ReactorGW());
		File dummyBasedir = new File("/projects/my-app").getAbsoluteFile();
		gw.basedir = dummyBasedir;
		gw.scanDir = null;
		MavenProject mavenProject = new MavenProject();
		mavenProject.setFile(new File(dummyBasedir, "pom.xml"));
		mavenProject.setModel(new Model());
		gw.project = mavenProject;
		MavenSession session = mock(MavenSession.class);
		when(session.getExecutionRootDirectory()).thenReturn(dummyBasedir.getAbsolutePath());
		when(session.getUserProperties()).thenReturn(new java.util.Properties());
		gw.session = session;
		Field settingsField = AbstractGWGoal.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(gw, new Settings());
		doNothing().when(gw).scanDocuments(any(GuidanceProcessor.class));
		// Act
		gw.execute();
		// Assert
		assertEquals(dummyBasedir.getAbsolutePath(), gw.scanDir);
		Mockito.verify(gw).scanDocuments(any(GuidanceProcessor.class));
	}
}
