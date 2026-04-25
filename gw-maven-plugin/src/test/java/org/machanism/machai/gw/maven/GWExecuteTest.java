package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.GuidanceProcessor;

public class GWExecuteTest {

	static class ThrowingGWMojo extends GWMojo {
		@Override
		protected void scanDocuments(GuidanceProcessor processor) throws MojoExecutionException {
			throw new ProcessTerminationException("stop", 7);
		}
	}

	@Test
	public void execute_whenScanDocumentsThrowsProcessTerminationException_wrapsInMojoExecutionException() throws Exception {
		ThrowingGWMojo gw = new ThrowingGWMojo();
		gw.project = new MavenProject();
		gw.project.setFile(new File("pom.xml"));
		gw.basedir = new File(".").getAbsoluteFile();
		gw.session = newSession();

		Field settingsField = AbstractGWMojo.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(gw, new Settings());

		try {
			gw.execute();
			org.junit.Assert.fail("Expected MojoExecutionException");
		} catch (MojoExecutionException ex) {
			assertTrue(ex.getMessage().contains("exit code: 7"));
			assertTrue(ex.getCause() instanceof ProcessTerminationException);
			assertSame("stop", ex.getCause().getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	private static MavenSession newSession() {
		DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setProjectPresent(true);
		return new MavenSession(null, null, request, null);
	}
}
