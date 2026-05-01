package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

public class GWCoverageTest {

	static class CapturingGWMojo extends GWMojo {
		GuidanceProcessor captured;

		@Override
		protected PropertiesConfigurator getConfiguration() {
			return new PropertiesConfigurator();
		}

		@Override
		protected void scanDocuments(GuidanceProcessor processor) {
			this.captured = processor;
		}
	}

	/**
	 * FalsePositive MavenSession construction in tests requires the deprecated constructor to simulate Maven runtime state.
	 */
	@SuppressWarnings("java:S1874")
	private static MavenSession newSession(boolean projectPresent, boolean parallel, int concurrency,
			java.util.List<MavenProject> allProjects, String executionRoot) {
		DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setProjectPresent(projectPresent);
		request.setDegreeOfConcurrency(concurrency);
		request.setUserProperties(new Properties());
		return new MavenSession(null, null, request, null) {
			@Override
			public boolean isParallel() {
				return parallel;
			}

			@Override
			public java.util.List<MavenProject> getAllProjects() {
				return allProjects;
			}

			@Override
			public String getExecutionRootDirectory() {
				return executionRoot;
			}
		};
	}

	private static void setSettings(AbstractGWMojo mojo) throws Exception {
		Field settingsField = AbstractGWMojo.class.getDeclaredField("settings");
		settingsField.setAccessible(true);
		settingsField.set(mojo, new Settings());
	}

	@Test
	public void execute_createsProcessorAndSupportsProjectLayoutLookup() throws Exception {
		CapturingGWMojo mojo = new CapturingGWMojo();
		mojo.basedir = new File(".").getCanonicalFile();
		mojo.project = new MavenProject();
		mojo.project.setFile(new File(mojo.basedir, "pom.xml"));
		mojo.project.getModel().addModule("module");
		MavenProject matching = new MavenProject();
		matching.setArtifactId("gw-maven-plugin");
		Model matchingModel = new Model();
		matchingModel.setArtifactId("gw-maven-plugin");
		matching.setModel(matchingModel);
		mojo.session = newSession(true, true, 3, Collections.singletonList(matching), mojo.basedir.getAbsolutePath());
		setSettings(mojo);

		mojo.execute();

		assertNotNull(mojo.captured);
		ProjectLayout projectLayout = mojo.captured.getProjectLayout(new File("."));
		assertNotNull(projectLayout);
	}

	@Test
	public void execute_whenNoPomProjectStillCreatesProcessor() throws Exception {
		CapturingGWMojo mojo = new CapturingGWMojo();
		mojo.basedir = new File(".").getCanonicalFile();
		mojo.project = new MavenProject();
		mojo.session = newSession(false, false, 1, Collections.emptyList(), mojo.basedir.getAbsolutePath());
		setSettings(mojo);

		mojo.execute();

		assertNotNull(mojo.captured);
	}

	@Test
	public void gwPerModuleExecute_coversProcessorAnonymousMethods() throws Exception {
		GWPerModuleMojo mojo = new GWPerModuleMojo();
		mojo.basedir = new File(".").getCanonicalFile();
		mojo.project = new MavenProject();
		mojo.project.setFile(new File(mojo.basedir, "pom.xml"));
		mojo.project.setModel(new Model());
		mojo.session = newSession(false, false, 1, Collections.emptyList(), mojo.basedir.getAbsolutePath());
		setSettings(mojo);

		final GuidanceProcessor[] captured = new GuidanceProcessor[1];
		GWPerModuleMojo override = new GWPerModuleMojo() {
			@Override
			protected PropertiesConfigurator getConfiguration() {
				return new PropertiesConfigurator();
			}

			@Override
			protected void scanDocuments(GuidanceProcessor processor) {
				captured[0] = processor;
			}
		};
		override.basedir = mojo.basedir;
		override.project = mojo.project;
		override.session = mojo.session;
		setSettings(override);

		override.execute();

		assertNotNull(captured[0]);
		assertNotNull(captured[0].getProjectLayout(new File(".")));
		Method processModule = captured[0].getClass().getDeclaredMethod("processModule", File.class, String.class);
		processModule.setAccessible(true);
		processModule.invoke(captured[0], new File("."), "module");
	}

	@Test
	public void execute_whenAbstractScanThrowsProcessTermination_wrapsWithMessage() throws Exception {
		GWPerModuleMojo mojo = new GWPerModuleMojo() {
			@Override
			protected PropertiesConfigurator getConfiguration() {
				return new PropertiesConfigurator();
			}

			@Override
			protected void scanDocuments(GuidanceProcessor processor) throws MojoExecutionException {
				throw new org.machanism.machai.gw.tools.CommandFunctionTools.ProcessTerminationException("stop", 9);
			}
		};
		mojo.basedir = new File(".").getCanonicalFile();
		mojo.project = new MavenProject();
		mojo.session = newSession(false, false, 1, Collections.emptyList(), mojo.basedir.getAbsolutePath());
		setSettings(mojo);

		try {
			mojo.execute();
		} catch (MojoExecutionException e) {
			assertTrue(e.getMessage().contains("exit code: 9"));
			return;
		}
		throw new AssertionError("Expected MojoExecutionException");
	}

	@Test
	public void resolveProjectByArtifactId_whenNoMatch_returnsNull() throws Exception {
		Method resolve = GWMojo.class.getDeclaredMethod("resolveProjectByArtifactId", java.util.List.class, Model.class);
		resolve.setAccessible(true);
		Model model = new Model();
		model.setArtifactId("missing");
		Object result = resolve.invoke(null, Collections.singletonList(new MavenProject()), model);
		assertNull(result);
	}
}
