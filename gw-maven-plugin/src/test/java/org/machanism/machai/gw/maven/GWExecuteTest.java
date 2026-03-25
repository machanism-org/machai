package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertThrows;

import java.io.File;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.mockito.Mockito;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;

/**
 * Tests for {@link GW#execute()} error-handling paths.
 */
public class GWExecuteTest {

	@Test
	public void execute_whenScanDocumentsThrowsProcessTerminationException_wrapsInMojoExecutionException() throws Exception {
		// Arrange
		GW gw = Mockito.spy(new GW());

		gw.project = new MavenProject();
		gw.project.setFile(new File("pom.xml"));
		gw.basedir = new File(".").getAbsoluteFile();

		MavenExecutionRequest request = Mockito.mock(MavenExecutionRequest.class);
		Mockito.when(request.getDegreeOfConcurrency()).thenReturn(2);

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getExecutionRootDirectory()).thenReturn(gw.basedir.getAbsolutePath());
		Mockito.when(session.getAllProjects()).thenReturn(java.util.Collections.singletonList(new MavenProject()));
		Mockito.when(session.isParallel()).thenReturn(false);
		Mockito.when(session.getRequest()).thenReturn(request);
		Mockito.when(session.getUserProperties()).thenReturn(new java.util.Properties());
		gw.session = session;

		// Settings is required by AbstractGWGoal.getConfiguration()
		java.lang.reflect.Field settingsField = AbstractGWGoal.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(gw, new Settings());

		ProcessTerminationException pte = new ProcessTerminationException("stop", 7);
		Mockito.doThrow(pte).when(gw)
				.scanDocuments(Mockito.any(org.machanism.machai.gw.processor.GuidanceProcessor.class));

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, gw::execute);
		org.junit.Assert.assertTrue(ex.getMessage().contains("exit code: 7"));
		org.junit.Assert.assertSame(pte, ex.getCause());
	}
}
