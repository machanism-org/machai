package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
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
		ActMojo act = new ActMojo();
		Properties userProps = new Properties();
		userProps.setProperty(Ghostwriter.ACT_PROP_NAME, "saved");
		act.session = newSession(userProps);

		act.prompter = Mockito.mock(Prompter.class);
		Configurator conf = Mockito.mock(Configurator.class);

		act.applyActPrompt(conf);

		Mockito.verifyNoInteractions(act.prompter);
		assertEquals("saved", act.session.getUserProperties().getProperty(Ghostwriter.ACT_PROP_NAME));
	}

	@Test
	public void applyActPrompt_whenNoSavedAct_andConfigProvidesAct_savesIt() throws Exception {
		ActMojo act = new ActMojo();
		act.session = newSession(new Properties());

		act.prompter = Mockito.mock(Prompter.class);
		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Ghostwriter.ACT_PROP_NAME, null)).thenReturn("fromConf");

		act.applyActPrompt(conf);

		assertEquals("fromConf", act.session.getUserProperties().getProperty(Ghostwriter.ACT_PROP_NAME));
		Mockito.verifyNoInteractions(act.prompter);
	}

	@Test
	public void applyActPrompt_whenNoSavedActAndNoConfiguredAct_promptsAndStoresInput() throws Exception {
		ActMojo act = new ActMojo();
		act.session = newSession(new Properties());

		Prompter prompter = Mockito.mock(Prompter.class);
		Mockito.when(prompter.prompt("Act")).thenReturn("prompted-act");
		act.prompter = prompter;

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Ghostwriter.ACT_PROP_NAME, null)).thenReturn(null);

		act.applyActPrompt(conf);

		assertEquals("prompted-act", act.session.getUserProperties().getProperty(Ghostwriter.ACT_PROP_NAME));
		Mockito.verify(prompter).prompt("Act");
	}

	@Test
	public void applyActPrompt_whenPrompterFails_wrapsInMojoExecutionException() throws Exception {
		ActMojo act = new ActMojo();
		act.session = newSession(new Properties());

		Prompter prompter = Mockito.mock(Prompter.class);
		Mockito.when(prompter.prompt("Act")).thenThrow(new PrompterException("boom"));
		act.prompter = prompter;

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Ghostwriter.ACT_PROP_NAME, null)).thenReturn(null);

		try {
			act.applyActPrompt(conf);
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			assertTrue(e.getMessage().contains(Ghostwriter.ACT_PROP_NAME));
			assertTrue(e.getCause() instanceof PrompterException);
		}
	}

	@SuppressWarnings("deprecation")
	private static MavenSession newSession(Properties userProperties) {
		DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setUserProperties(userProperties);
		return new MavenSession(null, null, request, null);
	}
}
