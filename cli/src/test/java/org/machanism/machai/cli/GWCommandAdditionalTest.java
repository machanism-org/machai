package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.machai.gw.processor.Ghostwriter;

class GWCommandAdditionalTest {

	@Test
	void resolveModel_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_GENAI_PROP_NAME, "Configured:Model");
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("resolveModel", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("Configured:Model", result);
	}

	@Test
	void resolveModel_shouldPreferOptionValue_whenProvided() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_GENAI_PROP_NAME, "Configured:Model");
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("resolveModel", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, "Opt:Model");

		// Assert
		assertEquals("Opt:Model", result);
	}

	@Test
	void resolveLogInputs_shouldReturnFalse_whenConfigValueIsEmptyString() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_LOG_INPUTS_PROP_NAME, "");
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("resolveLogInputs", Boolean.class);
		m.setAccessible(true);

		// Act
		Boolean result = (Boolean) m.invoke(cmd, Boolean.TRUE);

		// Assert
		assertFalse(result);
	}

	@Test
	void resolveLogInputs_shouldReturnConfiguredValue_whenPresent() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_LOG_INPUTS_PROP_NAME, "false");
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("resolveLogInputs", Boolean.class);
		m.setAccessible(true);

		// Act
		Boolean result = (Boolean) m.invoke(cmd, Boolean.TRUE);

		// Assert
		assertFalse(result);
	}

	@Test
	void resolveRootDir_shouldReturnNonNull_whenArgumentNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_ROOTDIR_PROP_NAME, "");
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("resolveRootDir", File.class);
		m.setAccessible(true);

		// Act
		File result = (File) m.invoke(cmd, new Object[] { null });

		// Assert
		assertNotNull(result);
	}

	@Test
	void resolveRootDir_shouldReturnConfiguredRootDir_whenPresent() throws Exception {
		// Arrange
		File configured = new File("target").getAbsoluteFile();
		ConfigCommand.config.set(Ghostwriter.GW_ROOTDIR_PROP_NAME, configured.getAbsolutePath());
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("resolveRootDir", File.class);
		m.setAccessible(true);

		// Act
		File result = (File) m.invoke(cmd, new Object[] { new File(".") });

		// Assert
		assertEquals(configured.getAbsolutePath(), result.getAbsolutePath());
	}

	@Test
	void resolveInstructions_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_INSTRUCTIONS_PROP_NAME, "cfg-instr");
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("resolveInstructions", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("cfg-instr", result);
	}

	@Test
	void resolveGuidance_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_GUIDANCE_PROP_NAME, "cfg-guidance");
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("resolveGuidance", String.class);
		m.setAccessible(true);

		// Act
		String result = (String) m.invoke(cmd, new Object[] { null });

		// Assert
		assertEquals("cfg-guidance", result);
	}

	@Test
	void splitExcludes_shouldReturnSingleEntry_whenNoComma() throws Exception {
		// Arrange
		GWCommand cmd = new GWCommand();
		Method m = GWCommand.class.getDeclaredMethod("splitExcludes", String.class);
		m.setAccessible(true);

		// Act
		String[] result = (String[]) m.invoke(cmd, "target");

		// Assert
		assertEquals(1, result.length);
		assertEquals("target", result[0]);
	}

	@Test
	void loadMachaiPropertiesConfig_shouldReturnConfigurator_whenFileMissing() throws Exception {
		// Arrange
		File props = new File(ConfigCommand.MACHAI_PROPERTIES_FILE_NAME);
		File backup = null;
		if (props.exists()) {
			backup = new File(props.getParentFile(), props.getName() + ".bak-test");
			if (backup.exists()) {
				backup.delete();
			}
			assertEquals(true, props.renameTo(backup));
		}

		try {
			GWCommand cmd = new GWCommand();
			Method m = GWCommand.class.getDeclaredMethod("loadMachaiPropertiesConfig");
			m.setAccessible(true);

			// Act
			Object result = m.invoke(cmd);

			// Assert
			assertNotNull(result);
		} finally {
			if (backup != null && backup.exists()) {
				backup.renameTo(props);
			}
		}
	}

	@Test
	void resolveInstructions_shouldReturnNull_whenOptionEmptyAndNoInputProvided() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_INSTRUCTIONS_PROP_NAME, "");
		var originalIn = System.in;
		// Sonar java:S2093 - use try-with-resources for AutoCloseable resources.
		try (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) {
			System.setIn(in);
			GWCommand cmd = new GWCommand();
			Method m = GWCommand.class.getDeclaredMethod("resolveInstructions", String.class);
			m.setAccessible(true);

			// Act
			String result = (String) m.invoke(cmd, "");

			// Assert
			assertNull(result);
		} finally {
			System.setIn(originalIn);
		}
	}
}
