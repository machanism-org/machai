package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.Genai;

/**
 * Unit tests for {@link AbstractGWGoal}.
 */
public class AbstractGWGoalTest {

	static class TestGoal extends AbstractGWGoal {
		@Override
		public void execute() throws MojoExecutionException {
			// not used
		}
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field f = AbstractGWGoal.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		f.set(target, value);
	}

	@Test
	public void getConfiguration_whenSettingsNull_throwsMojoExecutionException() throws Exception {
		// Arrange
		TestGoal goal = new TestGoal();
		setField(goal, "settings", null);

		// Act
		try {
			goal.getConfiguration();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			// Assert
			assertEquals("Maven settings are not available.", e.getMessage());
		}
	}

	@Test
	public void getConfiguration_whenServerIdProvidedButServerMissing_throwsMojoExecutionException() throws Exception {
		// Arrange
		TestGoal goal = new TestGoal();
		Settings settings = new Settings();
		setField(goal, "settings", settings);
		setField(goal, "serverId", "missing");

		// Act
		try {
			goal.getConfiguration();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			// Assert
			assertEquals("No <server> with id 'missing' found in Maven settings.xml.", e.getMessage());
		}
	}

	@Test
	public void getConfiguration_whenServerIdProvided_setsNonBlankUsernameAndPassword() throws Exception {
		// Arrange
		TestGoal goal = new TestGoal();
		Settings settings = new Settings();
		Server server = new Server();
		server.setId("s1");
		server.setUsername("user");
		server.setPassword("pass");
		settings.addServer(server);
		setField(goal, "settings", settings);
		setField(goal, "serverId", "s1");

		// Act
		PropertiesConfigurator cfg = goal.getConfiguration();

		// Assert
		assertEquals("user", cfg.get(Genai.USERNAME_PROP_NAME));
		assertEquals("pass", cfg.get(Genai.PASSWORD_PROP_NAME));
	}

	@Test
	public void getConfiguration_whenServerIdProvided_ignoresBlankUsernameAndPassword() throws Exception {
		// Arrange
		TestGoal goal = new TestGoal();
		Settings settings = new Settings();
		Server server = new Server();
		server.setId("s1");
		server.setUsername("   ");
		server.setPassword("");
		settings.addServer(server);
		setField(goal, "settings", settings);
		setField(goal, "serverId", "s1");

		// Act
		PropertiesConfigurator cfg = goal.getConfiguration();

		// Assert
		assertSame(null, cfg.get(Genai.USERNAME_PROP_NAME, null));
		assertSame(null, cfg.get(Genai.PASSWORD_PROP_NAME, null));
	}

}
