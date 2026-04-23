package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;

public class ActConfigureAndScanTest {

	static class TestableAct extends Act {
		boolean applyActPromptCalled;
		boolean scanDocumentsCalled;

		@Override
		protected void applyActPrompt(org.machanism.macha.core.commons.configurator.Configurator conf)
				throws org.apache.maven.plugin.MojoExecutionException {
			applyActPromptCalled = true;
			super.applyActPrompt(conf);
		}

		@Override
		protected void scanDocuments(ActProcessor actProcessor) {
			scanDocumentsCalled = true;
		}
	}

	@Test
	public void configureAndScan_whenActPromptAlreadySet_usesItWithoutPrompting() throws Exception {
		TestableAct goal = new TestableAct();
		goal.actPrompt = "explicit-act";
		goal.session = mock(MavenSession.class);
		doReturn(new Properties()).when(goal.session).getUserProperties();

		ActProcessor processor = mock(ActProcessor.class);
		PropertiesConfigurator conf = new PropertiesConfigurator();
		doReturn(conf).when(processor).getConfigurator();

		goal.configureAndScan(processor);

		verify(processor).setAct("explicit-act");
		assertEquals(false, goal.applyActPromptCalled);
		assertEquals(true, goal.scanDocumentsCalled);
	}

	@Test
	public void configureAndScan_whenActPromptMissing_readsSavedActFromUserProperties() throws Exception {
		TestableAct goal = new TestableAct();
		Properties userProperties = new Properties();
		userProperties.setProperty(Ghostwriter.ACT_PROP_NAME, "saved-from-user-props");
		goal.session = mock(MavenSession.class);
		doReturn(userProperties).when(goal.session).getUserProperties();

		ActProcessor processor = mock(ActProcessor.class);
		PropertiesConfigurator conf = new PropertiesConfigurator();
		doReturn(conf).when(processor).getConfigurator();

		goal.configureAndScan(processor);

		verify(processor).setAct("saved-from-user-props");
		assertEquals(true, goal.applyActPromptCalled);
		assertEquals(true, goal.scanDocumentsCalled);
	}
}
