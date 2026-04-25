package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.gw.processor.GuidanceProcessor;

public class AbstractGWGoalTest {

	static class TestGoal extends AbstractGWMojo {
		@Override
		public void execute() throws MojoExecutionException {
			// not used
		}
	}

	static class RecordingGuidanceProcessor extends GuidanceProcessor {
		String[] excludes;
		String instructions;
		boolean logInputs;
		Object tool;
		File scannedBasedir;
		String scannedDir;
		RuntimeException failure;

		RecordingGuidanceProcessor() {
			super(new File("."), null, new PropertiesConfigurator());
		}

		@Override
		public void setExcludes(String... excludes) {
			this.excludes = excludes;
		}

		@Override
		public void setInstructions(String instructions) {
			this.instructions = instructions;
		}

		@Override
		public void setLogInputs(boolean logInputs) {
			this.logInputs = logInputs;
		}

		public void addTool(Object tool) {
			this.tool = tool;
		}

		@Override
		public void scanDocuments(File basedir, String scanDir) {
			if (failure != null) {
				throw failure;
			}
			this.scannedBasedir = basedir;
			this.scannedDir = scanDir;
		}
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field f = AbstractGWMojo.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		f.set(target, value);
	}

	@SuppressWarnings("deprecation")
	private static MavenSession newSession(boolean projectPresent, String executionRoot) {
		MavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setProjectPresent(projectPresent);
		return new MavenSession(null, null, request, null) {
			@Override
			public String getExecutionRootDirectory() {
				return executionRoot;
			}
		};
	}

	@Test
	public void getConfiguration_whenSettingsNull_throwsMojoExecutionException() throws Exception {
		TestGoal goal = new TestGoal();
		setField(goal, "settings", null);

		try {
			goal.getConfiguration();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			org.junit.Assert.assertEquals("Maven settings are not available.", e.getMessage());
		}
	}

	@Test
	public void getConfiguration_whenServerIdProvidedButServerMissing_throwsMojoExecutionException() throws Exception {
		TestGoal goal = new TestGoal();
		Settings settings = new Settings();
		setField(goal, "settings", settings);
		setField(goal, "serverId", "missing");

		try {
			goal.getConfiguration();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			org.junit.Assert.assertEquals("No <server> with id 'missing' found in Maven settings.xml.", e.getMessage());
		}
	}

	@Test
	public void getConfiguration_whenServerIdProvided_setsNonBlankUsernameAndPassword() throws Exception {
		TestGoal goal = new TestGoal();
		Settings settings = new Settings();
		Server server = new Server();
		server.setId("s1");
		server.setUsername("user");
		server.setPassword("pass");
		settings.addServer(server);
		setField(goal, "settings", settings);
		setField(goal, "serverId", "s1");

		PropertiesConfigurator cfg = goal.getConfiguration();

		org.junit.Assert.assertEquals("user", cfg.get(Genai.USERNAME_PROP_NAME));
		org.junit.Assert.assertEquals("pass", cfg.get(Genai.PASSWORD_PROP_NAME));
	}

	@Test
	public void getConfiguration_whenServerIdProvided_ignoresBlankUsernameAndPassword() throws Exception {
		TestGoal goal = new TestGoal();
		Settings settings = new Settings();
		Server server = new Server();
		server.setId("s1");
		server.setUsername("   ");
		server.setPassword("");
		settings.addServer(server);
		setField(goal, "settings", settings);
		setField(goal, "serverId", "s1");

		PropertiesConfigurator cfg = goal.getConfiguration();

		assertNull(cfg.get(Genai.USERNAME_PROP_NAME, null));
		assertNull(cfg.get(Genai.PASSWORD_PROP_NAME, null));
	}

	@Test
	public void getConfiguration_whenServerContainsXmlConfiguration_addsChildProperties() throws Exception {
		TestGoal goal = new TestGoal();
		Settings settings = new Settings();
		Server server = new Server();
		server.setId("s1");
		Xpp3Dom configuration = new Xpp3Dom("configuration");
		Xpp3Dom endpoint = new Xpp3Dom("endpoint");
		endpoint.setValue("http://localhost");
		configuration.addChild(endpoint);
		server.setConfiguration(configuration);
		settings.addServer(server);
		setField(goal, "settings", settings);
		setField(goal, "serverId", "s1");

		PropertiesConfigurator cfg = goal.getConfiguration();

		org.junit.Assert.assertEquals("http://localhost", cfg.get("endpoint"));
	}

	@Test
	public void scanDocuments_whenInstructionsAndProjectPresent_configuresProcessorAndScans() throws Exception {
		TestGoal goal = new TestGoal();
		goal.instructions = "instruction text";
		goal.excludes = new String[] { "target", "logs" };
		goal.logInputs = true;
		goal.project = new MavenProject();
		goal.project.setFile(new File("pom.xml"));
		goal.session = newSession(true, new File(".").getAbsolutePath());
		RecordingGuidanceProcessor processor = new RecordingGuidanceProcessor();

		goal.scanDocuments(processor);

		org.junit.Assert.assertArrayEquals(goal.excludes, processor.excludes);
		org.junit.Assert.assertEquals("instruction text", processor.instructions);
		assertTrue(processor.logInputs);
		org.junit.Assert.assertEquals(new File(System.getProperty("user.dir")), processor.scannedBasedir);
		org.junit.Assert.assertEquals(new File(".").getAbsolutePath(), processor.scannedDir);
	}

	@Test
	public void scanDocuments_whenProjectBasedirMissing_usesUserDirAndExistingScanDir() throws Exception {
		TestGoal goal = new TestGoal();
		goal.scanDir = "custom-dir";
		goal.project = new MavenProject();
		goal.session = newSession(false, new File(".").getAbsolutePath());
		RecordingGuidanceProcessor processor = new RecordingGuidanceProcessor();

		goal.scanDocuments(processor);

		assertSame(null, processor.excludes);
		assertFalse(processor.logInputs);
		org.junit.Assert.assertEquals(new File(System.getProperty("user.dir")), processor.scannedBasedir);
		org.junit.Assert.assertEquals("custom-dir", processor.scannedDir);
		assertSame(null, processor.tool);
	}

	@Test
	public void scanDocuments_whenProcessorFails_wrapsExceptionInMojoExecutionException() throws Exception {
		TestGoal goal = new TestGoal();
		goal.project = new MavenProject();
		goal.project.setFile(new File("pom.xml"));
		goal.session = newSession(false, new File(".").getAbsolutePath());
		RecordingGuidanceProcessor processor = new RecordingGuidanceProcessor();
		processor.failure = new IllegalStateException("boom");

		try {
			goal.scanDocuments(processor);
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			org.junit.Assert.assertEquals("File processing failed.", e.getMessage());
			assertTrue(e.getCause() instanceof IllegalStateException);
		}
	}

	@Test
	public void scanDocuments_whenProjectNotPresent_doesNotRegisterClassTool() throws Exception {
		TestGoal goal = new TestGoal();
		goal.project = new MavenProject();
		goal.session = newSession(false, new File(".").getAbsolutePath());
		RecordingGuidanceProcessor processor = new RecordingGuidanceProcessor();

		goal.scanDocuments(processor);

		assertSame(null, processor.tool);
		org.junit.Assert.assertEquals(new File(System.getProperty("user.dir")), processor.scannedBasedir);
		org.junit.Assert.assertEquals(new File(".").getAbsolutePath(), processor.scannedDir);
	}
}
