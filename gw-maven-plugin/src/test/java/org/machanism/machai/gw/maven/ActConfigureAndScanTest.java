package org.machanism.machai.gw.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.MutableConfigurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.mockito.Mockito;

public class ActConfigureAndScanTest {

	static class TestableAct extends ActMojo {
		public TestableAct() {
			setPrompter(Mockito.mock(Prompter.class));
		}

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

	static class RecordingActProcessor extends ActProcessor {
		String act;
		private final PropertiesConfigurator configurator = new PropertiesConfigurator();

		RecordingActProcessor() {
			super(new java.io.File("."), null, new PropertiesConfigurator());
		}

		@Override
		public MutableConfigurator getConfigurator() {
			return configurator;
		}

		@Override
		public void setAct(String act) {
			this.act = act;
		}
	}

	@Test
	public void configureAndScan_whenActPromptAlreadySet_usesItWithoutPrompting() throws Exception {
		TestableAct goal = new TestableAct();
		goal.actPrompt = "explicit-act";
		Properties userProperties = new Properties();
		goal.setSession(newSession(userProperties));

		RecordingActProcessor processor = new RecordingActProcessor();

		goal.configureAndScan(processor, goal.actPrompt);

		assertEquals("explicit-act", processor.act);
		assertEquals(false, goal.applyActPromptCalled);
		assertEquals(true, goal.scanDocumentsCalled);
	}

	@Test
	public void configureAndScan_whenActPromptMissing_readsSavedActFromUserProperties() throws Exception {
		TestableAct goal = new TestableAct();
		Properties userProperties = new Properties();
		userProperties.setProperty(GWConstants.ACT_PROP_NAME, "saved-from-user-params");
		goal.setSession(newSession(userProperties));

		RecordingActProcessor processor = new RecordingActProcessor();

		goal.configureAndScan(processor, null);

		assertEquals("saved-from-user-params", processor.act);
		assertEquals(true, goal.applyActPromptCalled);
		assertEquals(true, goal.scanDocumentsCalled);
	}

	@SuppressWarnings("deprecation")
	private static MavenSession newSession(Properties userProperties) {
		DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setUserProperties(userProperties);
		return new MavenSession(null, null, request, null);
	}
}
