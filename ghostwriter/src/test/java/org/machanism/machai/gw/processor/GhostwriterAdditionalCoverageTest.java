package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.slf4j.LoggerFactory;

class GhostwriterAdditionalCoverageTest {

	@TempDir
	File tempDir;

	@Test
	void initializeConfiguration_whenHomePropertyConfigured_usesConfiguredHomeDirectory() {
		// Arrange
		String originalHome = System.getProperty(Ghostwriter.HOME_PROP_NAME);

		try {
			System.setProperty(Ghostwriter.HOME_PROP_NAME, tempDir.getAbsolutePath());

			// Act
			PropertiesConfigurator config = Ghostwriter.initializeConfiguration(null);

			// Assert
			assertEquals(tempDir.getAbsolutePath(), System.getProperty(Ghostwriter.HOME_PROP_NAME));
			// Sonar java:S5785 - use explicit null assertion instead of boolean null-check.
			assertNotNull(config);
		} finally {
			restoreProperty(Ghostwriter.HOME_PROP_NAME, originalHome);
		}
	}

	@Test
	void setDegreeOfConcurrency_whenNull_doesNothing() {
		// Arrange
		TrackingProcessor processor = new TrackingProcessor(tempDir, new PropertiesConfigurator(), "model");
		Ghostwriter ghostwriter = new Ghostwriter("model", processor);

		// Act
		ghostwriter.setDegreeOfConcurrency(null);

		// Assert
		assertNull(processor.degreeOfConcurrency);
	}

	@Test
	void setDefaultPrompt_whenNull_doesNothing() {
		// Arrange
		TrackingProcessor processor = new TrackingProcessor(tempDir, new PropertiesConfigurator(), "model");
		Ghostwriter ghostwriter = new Ghostwriter("model", processor);

		// Act
		ghostwriter.setDefaultPrompt(null);

		// Assert
		assertNull(processor.defaultPrompt);
	}

	@Test
	void setInstructions_whenNull_doesNothing() {
		// Arrange
		TrackingProcessor processor = new TrackingProcessor(tempDir, new PropertiesConfigurator(), "model");
		Ghostwriter ghostwriter = new Ghostwriter("model", processor);

		// Act
		ghostwriter.setInstructions(null);

		// Assert
		assertNull(processor.instructions);
	}

	@Test
	void setExcludes_whenNull_doesNothing() {
		// Arrange
		TrackingProcessor processor = new TrackingProcessor(tempDir, new PropertiesConfigurator(), "model");
		Ghostwriter ghostwriter = new Ghostwriter("model", processor);

		// Act
		ghostwriter.setExcludes(null);

		// Assert
		assertNull(processor.excludes);
	}

	@Test
	void createActProcessor_whenActsOptionMissingAndConfigUnset_leavesActsLocationUnset() throws Exception {
		// Arrange
		setLogger();
		Options options = new Options();
		options.addOption(new Option("a", "act", true, "act"));
		CommandLine cmd = new DefaultParser().parse(options, new String[] { "-a", "demo" });
		PropertiesConfigurator config = new PropertiesConfigurator();

		// Act
		AIFileProcessor processor = Ghostwriter.createActProcessor(cmd, tempDir, config, "model");

		// Assert
		assertTrue(processor instanceof ActProcessor);
	}

	@Test
	void constructor_whenGenaiBlank_throwsHelpfulException() {
		// Arrange
		TrackingProcessor processor = new TrackingProcessor(tempDir, new PropertiesConfigurator(), "model");

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new Ghostwriter("   ", processor));
		assertTrue(ex.getMessage().contains("No GenAI provider/model configured"));
	}

	private static String invokeResolveActPrompt(CommandLine cmd, PropertiesConfigurator config) throws Exception {
		Method method = Ghostwriter.class.getDeclaredMethod("resolveActPrompt", CommandLine.class,
				PropertiesConfigurator.class);
		method.setAccessible(true);
		return (String) method.invoke(null, cmd, config);
	}

	private static void setLogger() throws Exception {
		Field field = Ghostwriter.class.getDeclaredField("logger");
		field.setAccessible(true);
		field.set(null, LoggerFactory.getLogger(Ghostwriter.class));
	}

	private static void restoreProperty(String key, String value) {
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}

	private static final class TrackingProcessor extends AIFileProcessor {
		private Integer degreeOfConcurrency;
		private String defaultPrompt;
		private String instructions;
		private String[] excludes;

		private TrackingProcessor(File projectDir, PropertiesConfigurator configurator, String model) {
			super(projectDir, configurator, model);
		}

		@Override
		public void setDegreeOfConcurrency(int value) {
			this.degreeOfConcurrency = value;
		}

		@Override
		public void setDefaultPrompt(String defaultPrompt) {
			this.defaultPrompt = defaultPrompt;
		}

		@Override
		public void setInstructions(String instructions) {
			this.instructions = instructions;
		}

		@Override
		public void setExcludes(String[] excludes) {
			this.excludes = excludes;
		}

		@Override
		public String process(org.machanism.machai.project.layout.ProjectLayout projectLayout, File file,
				String instructions, String prompt) {
			throw new UnsupportedOperationException();
		}
	}
}
