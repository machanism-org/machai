package org.machanism.machai.bindex.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.Genai;

class AbstractBindexMojoCoverageTest {

	private static class TestMojo extends AbstractBindexMojo {
		@Override
		public void execute() {
			// not used
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
	void getConfigurator_whenSettingsNull_throws() {
		// Arrange
		TestMojo mojo = new TestMojo();

		// Act / Assert
		assertThrows(MojoExecutionException.class, mojo::getConfigurator);
	}

	@Test
	void getConfigurator_whenNoServerId_doesNotPopulateCredentials() throws Exception {
		// Arrange
		TestMojo mojo = new TestMojo();
		Settings settings = new Settings();
		TestSupport.setPrivateField(mojo, "settings", settings);
		TestSupport.setPrivateField(mojo, "serverId", null);

		// Act
		PropertiesConfigurator config = mojo.getConfigurator();

		// Assert
		assertNotNull(config);
		assertNull(configValue(config, Genai.USERNAME_PROP_NAME));
		assertNull(configValue(config, Genai.PASSWORD_PROP_NAME));
	}

	@Test
	void getConfigurator_whenServerIdMissing_throws() throws Exception {
		// Arrange
		TestMojo mojo = new TestMojo();
		Settings settings = new Settings();
		TestSupport.setPrivateField(mojo, "settings", settings);
		TestSupport.setPrivateField(mojo, "serverId", "missing");

		// Act / Assert
		MojoExecutionException ex = assertThrows(MojoExecutionException.class, mojo::getConfigurator);
		assertEquals("No <server> with id 'missing' found in Maven settings.xml.", ex.getMessage());
	}

	@Test
	void getConfigurator_whenServerHasUsernamePasswordAndXpp3Dom_setsAllProperties() throws Exception {
		// Arrange
		TestMojo mojo = new TestMojo();
		mojo.project = new MavenProject(new Model());
		mojo.basedir = new File(".");

		Settings settings = new Settings();
		Server server = new Server();
		server.setId("genai");
		server.setUsername("user");
		server.setPassword("pass");

		Xpp3Dom conf = new Xpp3Dom("configuration");
		Xpp3Dom child1 = new Xpp3Dom("custom1");
		child1.setValue("v1");
		conf.addChild(child1);
		Xpp3Dom child2 = new Xpp3Dom("custom2");
		child2.setValue("v2");
		conf.addChild(child2);
		server.setConfiguration(conf);

		settings.addServer(server);
		TestSupport.setPrivateField(mojo, "settings", settings);
		TestSupport.setPrivateField(mojo, "serverId", "genai");

		// Act
		PropertiesConfigurator config = mojo.getConfigurator();

		// Assert
		assertEquals("user", configValue(config, Genai.USERNAME_PROP_NAME));
		assertEquals("pass", configValue(config, Genai.PASSWORD_PROP_NAME));
		assertEquals("v1", configValue(config, "custom1"));
		assertEquals("v2", configValue(config, "custom2"));
		assertNull(configValue(config, "missing"));
	}
}
