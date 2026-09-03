package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.gw.processor.GWConstants;
import org.mockito.Mockito;

public class ActApplyActPromptTest {

	private static ActMojo newAct(Prompter prompter) {
		ActMojo act = new ActMojo();
		act.setPrompter(prompter);
		return act;
	}

	@Test
	public void applyActPrompt_whenSavedActExists_doesNotPromptAndKeepsValue() throws Exception {
		ActMojo act = newAct(Mockito.mock(Prompter.class));
		Properties userProps = new Properties();
		userProps.setProperty(GWConstants.ACT_PROP_NAME, "saved");
		act.setSession(newSession(userProps));

		Configurator conf = Mockito.mock(Configurator.class);

		act.applyActPrompt(conf);

		Mockito.verifyNoInteractions(act.getPrompter());
		assertEquals("saved", act.session.getUserProperties().getProperty(GWConstants.ACT_PROP_NAME));
	}

	@Test
	public void applyActPrompt_whenNoSavedAct_andConfigProvidesAct_savesIt() throws Exception {
		ActMojo act = newAct(Mockito.mock(Prompter.class));
		act.setSession(newSession(new Properties()));

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(GWConstants.ACT_PROP_NAME, null)).thenReturn("fromConf");

		act.applyActPrompt(conf);

		assertEquals("fromConf", act.session.getUserProperties().getProperty(GWConstants.ACT_PROP_NAME));
		Mockito.verifyNoInteractions(act.getPrompter());
	}

	@Test
	public void applyActPrompt_whenNoSavedActAndNoConfiguredAct_promptsAndStoresInput() throws Exception {
		Prompter prompter = Mockito.mock(Prompter.class);
		ActMojo act = newAct(prompter);
		act.setSession(newSession(new Properties()));

		Mockito.when(prompter.prompt("Act")).thenReturn("prompted-act");

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(GWConstants.ACT_PROP_NAME, null)).thenReturn(null);

		act.applyActPrompt(conf);

		assertEquals("prompted-act", act.session.getUserProperties().getProperty(GWConstants.ACT_PROP_NAME));
		Mockito.verify(prompter).prompt("Act");
	}

	@Test
	public void applyActPrompt_whenPrompterFails_wrapsInMojoExecutionException() throws Exception {
		ActMojo act = newAct(Mockito.mock(Prompter.class));
		act.setSession(newSession(new Properties()));

		Mockito.when(act.getPrompter().prompt("Act")).thenThrow(new PrompterException("boom"));

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(GWConstants.ACT_PROP_NAME, null)).thenReturn(null);

		try {
			act.applyActPrompt(conf);
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			assertTrue(e.getMessage().contains(GWConstants.ACT_PROP_NAME));
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
