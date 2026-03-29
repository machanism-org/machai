package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;

public class ReactorActExecuteTest {

	@Test
	public void execute_whenCalled_processesWithActProcessorThatUsesDetectedProjectLayout() throws Exception {
		// Arrange
		ReactorAct goal = Mockito.spy(new ReactorAct());

		File basedir = new File(".").getCanonicalFile();
		goal.basedir = basedir;

		MavenProject project = new MavenProject();
		project.setFile(new File(basedir, "pom.xml"));
		goal.project = project;

		MavenExecutionRequest request = Mockito.mock(MavenExecutionRequest.class);
		Mockito.when(request.getDegreeOfConcurrency()).thenReturn(1);

		Properties userProps = new Properties();
		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getExecutionRootDirectory()).thenReturn(basedir.getAbsolutePath());
		Mockito.when(session.getRequest()).thenReturn(request);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		goal.session = session;

		Settings settings = new Settings();
		Field settingsField = AbstractGWGoal.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(goal, settings);

		Prompter prompter = Mockito.mock(Prompter.class);
		Mockito.when(prompter.prompt(Mockito.anyString())).thenReturn("some-act");
		goal.prompter = prompter;

		PropertiesConfigurator config = new PropertiesConfigurator();
		Mockito.doReturn(config).when(goal).getConfiguration();

		ArgumentCaptor<ActProcessor> processorCaptor = ArgumentCaptor.forClass(ActProcessor.class);
		Mockito.doNothing().when(goal).process(processorCaptor.capture());

		// Act
		goal.execute();

		// Assert
		ActProcessor created = processorCaptor.getValue();
		File createdProjectDir = getPrivateField(created, "projectDir", File.class);
		assertEquals(basedir.getCanonicalFile(), createdProjectDir.getCanonicalFile());

		created.getProjectLayout(basedir);

		Mockito.verify(prompter, Mockito.atLeastOnce()).prompt(Mockito.anyString());
	}

	@Test(expected = MojoExecutionException.class)
	public void execute_whenApplyActPromptFails_throwsMojoExecutionException() throws Exception {
		// Arrange
		ReactorAct goal = Mockito.spy(new ReactorAct());

		File basedir = new File(".").getCanonicalFile();
		goal.basedir = basedir;
		goal.project = new MavenProject();

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getExecutionRootDirectory()).thenReturn(basedir.getAbsolutePath());
		goal.session = session;

		Settings settings = new Settings();
		Field settingsField = AbstractGWGoal.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(goal, settings);

		PropertiesConfigurator config = new PropertiesConfigurator();
		Mockito.doReturn(config).when(goal).getConfiguration();

		Mockito.doThrow(new MojoExecutionException("boom")).when(goal).applyActPrompt(Mockito.any(Configurator.class));

		// Act
		goal.execute();
	}

	@SuppressWarnings("unchecked")
	private static <T> T getPrivateField(Object target, String fieldName, Class<T> type) throws Exception {
		Class<?> c = target.getClass();
		while (c != null) {
			try {
				Field f = c.getDeclaredField(fieldName);
				f.setAccessible(true);
				return (T) f.get(target);
			} catch (NoSuchFieldException e) {
				c = c.getSuperclass();
			}
		}
		throw new NoSuchFieldException(fieldName);
	}
}
