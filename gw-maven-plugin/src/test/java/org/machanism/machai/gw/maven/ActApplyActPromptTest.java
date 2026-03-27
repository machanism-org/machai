package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.mockito.Mockito;

public class ActApplyActPromptTest {

	@Test
	public void applyActPrompt_whenSavedActExists_doesNotPromptAndKeepsValue() throws Exception {
		// Arrange
		Act act = new Act();
		Properties userProps = new Properties();
		userProps.setProperty(Ghostwriter.GW_ACT_PROP_NAME, "saved");

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;

		act.prompter = Mockito.mock(Prompter.class);
		Configurator conf = Mockito.mock(Configurator.class);

		// Act
		act.applyActPrompt(conf);

		// Assert
		Mockito.verifyNoInteractions(act.prompter);
		assertEquals("saved", userProps.getProperty(Ghostwriter.GW_ACT_PROP_NAME));
	}

	@Test
	public void applyActPrompt_whenNoSavedAct_andConfigProvidesAct_savesIt() throws Exception {
		// Arrange
		Act act = new Act();
		Properties userProps = new Properties();

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;

		act.prompter = Mockito.mock(Prompter.class);
		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.GW_ACT_PROP_NAME), Mockito.isNull())).thenReturn("fromConf");

		// Act
		act.applyActPrompt(conf);

		// Assert
		assertEquals("fromConf", userProps.getProperty(Ghostwriter.GW_ACT_PROP_NAME));
		Mockito.verifyNoInteractions(act.prompter);
	}

	@Test
	public void applyActPrompt_whenNoSavedAct_andConfigMissing_promptsUserAndSaves() throws Exception {
		// Arrange
		Act act = Mockito.spy(new Act());
		Properties userProps = new Properties();

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.GW_ACT_PROP_NAME), Mockito.isNull())).thenReturn(null);

		Mockito.doReturn("typed").when(act).readText("Act");

		// Act
		act.applyActPrompt(conf);

		// Assert
		assertEquals("typed", userProps.getProperty(Ghostwriter.GW_ACT_PROP_NAME));
	}

	@Test(expected = MojoExecutionException.class)
	public void applyActPrompt_whenPrompterThrows_wrapsIntoMojoExecutionException() throws Exception {
		// Arrange
		Act act = Mockito.spy(new Act());
		Properties userProps = new Properties();

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.GW_ACT_PROP_NAME), Mockito.isNull())).thenReturn(null);

		Mockito.doThrow(new PrompterException("fail")).when(act).readText("Act");

		// Act
		act.applyActPrompt(conf);
	}

	@Test
	public void applyActPrompt_isSynchronizedAcrossThreads_usingSameInstance_doesNotLoseValue() throws Exception {
		// Arrange
		Act act = Mockito.spy(new Act());
		Properties userProps = new Properties();
		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;
		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.GW_ACT_PROP_NAME), Mockito.isNull())).thenReturn(null);

		// Return different values if called multiple times; due to synchronization and userProps caching,
		// readText should be called at most once.
		Mockito.doReturn("v1").doReturn("v2").when(act).readText("Act");

		Runnable r = () -> {
			try {
				act.applyActPrompt(conf);
			} catch (MojoExecutionException e) {
				throw new RuntimeException(e);
			}
		};

		Thread t1 = new Thread(r);
		Thread t2 = new Thread(r);

		// Act
		t1.start();
		t2.start();
		t1.join();
		t2.join();

		// Assert
		Mockito.verify(act, Mockito.atMost(1)).readText("Act");
		assertEquals("v1", userProps.getProperty(Ghostwriter.GW_ACT_PROP_NAME));
	}
}
