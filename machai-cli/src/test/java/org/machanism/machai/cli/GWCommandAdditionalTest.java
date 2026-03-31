package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.lang.reflect.Method;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.Mockito;

class GWCommandAdditionalTest {

	@Test
	void resolveModel_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		var config = ConfigCommand.getConfigurator();
		config.set(Ghostwriter.MODEL_PROP_NAME, "Configured:Model");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveModel", String.class,
				org.machanism.macha.core.commons.configurator.AbstractConfigurator.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, null, config);

		// Assert
		assertEquals("Configured:Model", result);
	}

	@Test
	void resolveModel_shouldPreferOptionValue_whenProvided() throws Exception {
		// Arrange
		var config = ConfigCommand.getConfigurator();
		config.set(Ghostwriter.MODEL_PROP_NAME, "Configured:Model");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveModel", String.class,
				org.machanism.macha.core.commons.configurator.AbstractConfigurator.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, "Opt:Model", config);

		// Assert
		assertEquals("Opt:Model", result);
	}

	@Test
	void resolveLogInputs_shouldReturnFalse_whenConfigValueIsEmptyString() throws Exception {
		// Arrange
		var config = ConfigCommand.getConfigurator();
		config.set(Genai.LOG_INPUTS_PROP_NAME, "");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveLogInputs", Boolean.class,
				org.machanism.macha.core.commons.configurator.AbstractConfigurator.class);
		m.setAccessible(true);

		// Act
		Boolean result = (Boolean) m.invoke(cmd, Boolean.TRUE, config);

		// Assert
		assertFalse(result);
	}

	@Test
	void resolveLogInputs_shouldReturnConfiguredValue_whenPresent() throws Exception {
		// Arrange
		var config = ConfigCommand.getConfigurator();
		config.set(Genai.LOG_INPUTS_PROP_NAME, "false");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveLogInputs", Boolean.class,
				org.machanism.macha.core.commons.configurator.AbstractConfigurator.class);
		m.setAccessible(true);

		// Act
		Boolean result = (Boolean) m.invoke(cmd, Boolean.TRUE, config);

		// Assert
		assertFalse(result);
	}

	@Test
	void resolveRootDir_shouldReturnNonNull_whenArgumentNull() throws Exception {
		// Arrange
		var config = ConfigCommand.getConfigurator();
		config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, "");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveProjectDir", File.class,
				org.machanism.macha.core.commons.configurator.AbstractConfigurator.class);
		m.setAccessible(true);

		// Act
		File result = (File) m.invoke(cmd, null, config);

		// Assert
		assertNotNull(result);
	}

	@Test
	void resolveRootDir_shouldReturnConfiguredRootDir_whenPresent() throws Exception {
		// Arrange
		File configured = new File("target").getAbsoluteFile();
		var config = ConfigCommand.getConfigurator();
		config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, configured.getAbsolutePath());
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveProjectDir", File.class,
				org.machanism.macha.core.commons.configurator.AbstractConfigurator.class);
		m.setAccessible(true);

		// Act
		File result = (File) m.invoke(cmd, new File("."), config);

		// Assert
		assertEquals(configured.getAbsolutePath(), result.getAbsolutePath());
	}

	@Test
	void resolveInstructions_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		var config = ConfigCommand.getConfigurator();
		config.set(Ghostwriter.INSTRUCTIONS_PROP_NAME, "cfg-instr");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
		Method m = GWCommand.class.getDeclaredMethod("resolveInstructions", String.class,
				org.machanism.macha.core.commons.configurator.AbstractConfigurator.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, null, config);

		// Assert
		assertEquals("cfg-instr", result);
	}

}
