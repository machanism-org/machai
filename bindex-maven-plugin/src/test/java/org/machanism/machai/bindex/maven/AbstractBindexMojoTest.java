package org.machanism.machai.bindex.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.Genai;

class AbstractBindexMojoTest {

	private static class TestMojo extends AbstractBindexMojo {
		@Override
		public void execute() {
			// not used
		}
	}

	private static void setField(Object target, String fieldName, Object value) {
		try {
			java.lang.reflect.Field f = AbstractBindexMojo.class.getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String configValue(PropertiesConfigurator config, String key) {
		try {
			java.lang.reflect.Method getProperty = config.getClass().getMethod("getProperty", String.class);
			Object v = getProperty.invoke(config, key);
			return v == null ? null : String.valueOf(v);
		} catch (NoSuchMethodException e) {
			// fall through
		} catch (java.lang.reflect.InvocationTargetException e) {
			// Some Machai versions throw if property is missing. Treat as null.
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			java.lang.reflect.Method get = config.getClass().getMethod("get", String.class);
			Object v = get.invoke(config, key);
			return v == null ? null : String.valueOf(v);
		} catch (NoSuchMethodException e) {
			// fall through
		} catch (java.lang.reflect.InvocationTargetException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			java.lang.reflect.Method getString = config.getClass().getMethod("getString", String.class);
			Object v = getString.invoke(config, key);
			return v == null ? null : String.valueOf(v);
		} catch (NoSuchMethodException e) {
			// fall through
		} catch (java.lang.reflect.InvocationTargetException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		throw new IllegalStateException("Could not access PropertiesConfigurator getter method");
	}

	@Test
	void isBindexed_returnsFalseForPomPackaging() {
		// Arrange
		TestMojo mojo = new TestMojo();
		MavenProject project = new MavenProject();
		project.setPackaging("pom");
		mojo.project = project;

		// Act
		boolean result = mojo.isBindexed();

		// Assert
		assertFalse(result);
	}

	@Test
	void isBindexed_returnsTrueForNonPomPackaging() {
		// Arrange
		TestMojo mojo = new TestMojo();
		MavenProject project = new MavenProject();
		project.setPackaging("jar");
		mojo.project = project;

		// Act
		boolean result = mojo.isBindexed();

		// Assert
		assertTrue(result);
	}

	@Test
	void getConfigurator_throwsWhenSettingsNotAvailable() {
		// Arrange
		TestMojo mojo = new TestMojo();
		setField(mojo, "settings", null);

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::getConfigurator);
		assertEquals("Maven settings are not available.", ex.getMessage());
	}

	@Test
	void getConfigurator_returnsConfiguratorWhenServerIdNotSet() throws Exception {
		// Arrange
		TestMojo mojo = new TestMojo();
		Settings settings = new Settings();
		setField(mojo, "settings", settings);
		setField(mojo, "serverId", null);

		// Act
		PropertiesConfigurator config = mojo.getConfigurator();

		// Assert
		assertNotNull(config);
		assertNull(configValue(config, Genai.USERNAME_PROP_NAME));
		assertNull(configValue(config, Genai.PASSWORD_PROP_NAME));
	}

	@Test
	void getConfigurator_throwsWhenServerIdProvidedButServerNotFound() {
		// Arrange
		TestMojo mojo = new TestMojo();
		Settings settings = new Settings();
		setField(mojo, "settings", settings);
		setField(mojo, "serverId", "missing");

		// Act + Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::getConfigurator);
		assertEquals("No <server> with id 'missing' found in Maven settings.xml.", ex.getMessage());
	}

	@Test
	void getConfigurator_setsUsernameAndPasswordWhenNonBlank() throws Exception {
		// Arrange
		TestMojo mojo = new TestMojo();
		Settings settings = new Settings();
		Server server = new Server();
		server.setId("genai");
		server.setUsername("user");
		server.setPassword("pass");
		settings.addServer(server);
		setField(mojo, "settings", settings);
		setField(mojo, "serverId", "genai");

		// Act
		PropertiesConfigurator config = mojo.getConfigurator();

		// Assert
		assertEquals("user", configValue(config, Genai.USERNAME_PROP_NAME));
		assertEquals("pass", configValue(config, Genai.PASSWORD_PROP_NAME));
	}

	@Test
	void getConfigurator_doesNotSetBlankUsernameOrPassword() throws Exception {
		// Arrange
		TestMojo mojo = new TestMojo();
		Settings settings = new Settings();
		Server server = new Server();
		server.setId("genai");
		server.setUsername("   ");
		server.setPassword("");
		settings.addServer(server);
		setField(mojo, "settings", settings);
		setField(mojo, "serverId", "genai");

		// Act
		PropertiesConfigurator config = mojo.getConfigurator();

		// Assert
		assertNull(configValue(config, Genai.USERNAME_PROP_NAME));
		assertNull(configValue(config, Genai.PASSWORD_PROP_NAME));
	}
}
