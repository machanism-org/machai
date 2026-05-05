package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

public class ActCoverageTest {

	static class CapturingActMojo extends ActMojo {
		ActProcessor capturedProcessor;

		@Override
		protected PropertiesConfigurator getConfiguration() {
			return new PropertiesConfigurator();
		}

		@Override
		protected void process(ActProcessor actProcessor) {
			this.capturedProcessor = actProcessor;
		}
	}

	static class CapturingActPerModuleMojo extends ActPerModuleMojo {
		ActProcessor capturedProcessor;

		@Override
		protected PropertiesConfigurator getConfiguration() {
			return new PropertiesConfigurator();
		}

		@Override
		protected void applyActPrompt(org.machanism.macha.core.commons.configurator.Configurator conf) {
			// no-op
		}

		@Override
		protected void process(ActProcessor actProcessor) {
			this.capturedProcessor = actProcessor;
		}
	}

	static class RecordingActProcessor extends ActProcessor {
		String act;
		boolean nonRecursive;
		File scannedBasedir;
		String scannedDir;

		RecordingActProcessor() {
			super(new File("."), new PropertiesConfigurator(), null);
		}

		@Override
		public void setAct(String act) {
			this.act = act;
		}

		@Override
		public boolean isNonRecursive() {
			return nonRecursive;
		}

		@Override
		public void setNonRecursive(boolean nonRecursive) {
			this.nonRecursive = nonRecursive;
		}

		@Override
		public void scanDocuments(File basedir, String scanDir) {
			this.scannedBasedir = basedir;
			this.scannedDir = scanDir;
		}
	}

	/**
	 * FalsePositive MavenSession construction in tests requires the deprecated constructor to simulate Maven runtime state.
	 */
	@SuppressWarnings("java:S1874")
	private static MavenSession newSession(Properties userProperties, boolean projectPresent, String executionRoot,
			java.util.List<MavenProject> allProjects) {
		DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setUserProperties(userProperties);
		request.setProjectPresent(projectPresent);
		request.setDegreeOfConcurrency(1);
		return new MavenSession(null, null, request, null) {
			@Override
			public String getExecutionRootDirectory() {
				return executionRoot;
			}

			@Override
			public java.util.List<MavenProject> getAllProjects() {
				return allProjects;
			}
		};
	}

	private static void setSettings(AbstractGWMojo mojo) throws Exception {
		Field settingsField = AbstractGWMojo.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(mojo, new Settings());
	}

	@Test
	public void configureAndScan_whenPromptReturnsExit_skipsScanning() throws Exception {
		ActMojo mojo = new ActMojo();
		Properties props = new Properties();
		mojo.session = newSession(props, false, new File(".").getAbsolutePath(), Collections.emptyList());
		mojo.prompter = mock(Prompter.class);
		org.mockito.Mockito.when(mojo.prompter.prompt("Act")).thenReturn("exit");
		RecordingActProcessor processor = new RecordingActProcessor();

		mojo.configureAndScan(processor);

		assertNull(processor.act);
		assertNull(processor.scannedDir);
	}

	@Test
	public void applyActPrompt_whenUserEntersExit_doesNotStoreValue() throws Exception {
		ActMojo mojo = new ActMojo();
		Properties props = new Properties();
		mojo.session = newSession(props, false, new File(".").getAbsolutePath(), Collections.emptyList());
		Prompter prompter = mock(Prompter.class);
		org.mockito.Mockito.when(prompter.prompt("Act")).thenReturn("exit");
		mojo.prompter = prompter;

		mojo.applyActPrompt(mock(org.machanism.macha.core.commons.configurator.Configurator.class));

		assertNull(props.getProperty(GWConstants.ACT_PROP_NAME));
	}

	@Test
	public void updateMavenProjectLayout_whenMatchingArtifactFound_updatesLayoutModel() throws Exception {
		ActMojo mojo = new ActMojo();
		mojo.project = new MavenProject();
		MavenProject first = new MavenProject();
		first.setArtifactId("other");
		MavenProject matching = new MavenProject();
		matching.setArtifactId("target-artifact");
		Model matchingModel = new Model();
		matchingModel.setArtifactId("target-artifact");
		matching.setModel(matchingModel);
		mojo.session = newSession(new Properties(), false, new File(".").getAbsolutePath(), Arrays.asList(first, matching));
		MavenProjectLayout layout = new MavenProjectLayout();
		Model model = new Model();
		model.setArtifactId("target-artifact");

		Method method = ActMojo.class.getDeclaredMethod("updateMavenProjectLayout", MavenProjectLayout.class, Model.class);
		method.setAccessible(true);
		method.invoke(mojo, layout, model);

		assertEquals(matchingModel, layout.getModel());
	}

	@Test
	public void execute_onActPerModule_createsProcessorWithLayoutAndNoOpModuleProcessing() throws Exception {
		CapturingActPerModuleMojo mojo = new CapturingActPerModuleMojo();
		mojo.basedir = new File(".").getCanonicalFile();
		mojo.project = new MavenProject();
		mojo.project.setFile(new File(mojo.basedir, "pom.xml"));
		mojo.project.setModel(new Model());
		mojo.session = newSession(new Properties(), false, mojo.basedir.getAbsolutePath(), Collections.singletonList(mojo.project));
		setSettings(mojo);

		mojo.execute();

		assertNotNull(mojo.capturedProcessor);
		ProjectLayout layout = mojo.capturedProcessor.getProjectLayout(new File("."));
		assertNotNull(layout);
		Method processModule = mojo.capturedProcessor.getClass().getDeclaredMethod("processModule", File.class, String.class);
		processModule.setAccessible(true);
		processModule.invoke(mojo.capturedProcessor, new File("."), "module");
	}

	@Test
	public void scanDocuments_onActPerModule_whenEligible_scansAndForcesNonRecursive() throws Exception {
		ActPerModuleMojo mojo = new ActPerModuleMojo();
		mojo.basedir = new File(".").getCanonicalFile();
		mojo.project = new MavenProject();
		mojo.project.setFile(new File(mojo.basedir, "pom.xml"));
		mojo.session = newSession(new Properties(), false, mojo.basedir.getAbsolutePath(), Collections.singletonList(new MavenProject()));
		RecordingActProcessor processor = new RecordingActProcessor();
		processor.nonRecursive = false;

		mojo.scanDocuments(processor);

		assertTrue(processor.nonRecursive);
		assertEquals(mojo.basedir, processor.scannedBasedir);
		assertEquals(mojo.basedir.getAbsolutePath(), processor.scannedDir);
	}

	@Test
	public void getProjectLayout_onActPerModuleProcessor_acceptsMissingProjectFile() throws Exception {
		CapturingActPerModuleMojo mojo = new CapturingActPerModuleMojo();
		mojo.basedir = new File(".").getCanonicalFile();
		mojo.project = new MavenProject();
		mojo.project.setModel(new Model());
		mojo.session = newSession(new Properties(), false, mojo.basedir.getAbsolutePath(), Collections.singletonList(new MavenProject()));
		setSettings(mojo);

		mojo.execute();

		try {
			ProjectLayout layout = mojo.capturedProcessor.getProjectLayout(new File("."));
			assertNotNull(layout);
		} catch (FileNotFoundException e) {
			assertFalse(true);
		}
	}
}
