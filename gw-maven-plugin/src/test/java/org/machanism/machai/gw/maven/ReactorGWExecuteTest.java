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
}
