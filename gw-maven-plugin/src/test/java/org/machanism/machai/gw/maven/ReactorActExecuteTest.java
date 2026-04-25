package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;

public class ReactorActExecuteTest {

	static class CapturingActPerModuleMojo extends ActPerModuleMojo {
		ActProcessor captured;
		boolean failApply;

		@Override
		protected PropertiesConfigurator getConfiguration() {
			return new PropertiesConfigurator();
		}

		@Override
		protected void applyActPrompt(org.machanism.macha.core.commons.configurator.Configurator conf)
				throws MojoExecutionException {
			if (failApply) {
				throw new MojoExecutionException("boom");
			}
		}

		@Override
		protected void process(ActProcessor actProcessor) {
			this.captured = actProcessor;
		}
	}

	@Test
	public void execute_whenCalled_processesWithActProcessorThatUsesDetectedProjectLayout() throws Exception {
		CapturingActPerModuleMojo goal = new CapturingActPerModuleMojo();
		File basedir = new File(".").getCanonicalFile();
		goal.basedir = basedir;
		MavenProject project = new MavenProject();
		project.setFile(new File(basedir, "pom.xml"));
		project.setModel(new org.apache.maven.model.Model());
		goal.project = project;
		goal.session = newSession(basedir.getAbsolutePath());

		Field settingsField = AbstractGWMojo.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(goal, new Settings());

		goal.execute();

		assertNotNull(goal.captured);
		File createdProjectDir = getPrivateField(goal.captured, "projectDir");
		assertEquals(basedir.getCanonicalFile(), createdProjectDir.getCanonicalFile());
	}

	@Test
	public void execute_whenApplyActPromptFails_throwsMojoExecutionException() throws Exception {
		CapturingActPerModuleMojo goal = new CapturingActPerModuleMojo();
		goal.failApply = true;
		File basedir = new File(".").getCanonicalFile();
		goal.basedir = basedir;
		goal.project = new MavenProject();
		goal.session = newSession(basedir.getAbsolutePath());

		Field settingsField = AbstractGWMojo.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(goal, new Settings());

		try {
			goal.execute();
			org.junit.Assert.fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			assertTrue(e.getMessage().contains("boom"));
		}
	}

	@SuppressWarnings("deprecation")
	private static MavenSession newSession(String executionRoot) {
		DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
		return new MavenSession(null, null, request, null) {
			@Override
			public String getExecutionRootDirectory() {
				return executionRoot;
			}
		};
	}

	private static File getPrivateField(Object target, String fieldName) throws Exception {
		Class<?> c = target.getClass();
		while (c != null) {
			try {
				Field f = c.getDeclaredField(fieldName);
				f.setAccessible(true);
				return (File) f.get(target);
			} catch (NoSuchFieldException e) {
				c = c.getSuperclass();
			}
		}
		throw new NoSuchFieldException(fieldName);
	}
}
