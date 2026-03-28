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

}
