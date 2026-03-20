package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.lang.reflect.Method;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.mockito.Mockito;

class GWCommandAdditionalTest {

	@Test
	void resolveModel_shouldReturnConfiguredValue_whenOptionIsNull() throws Exception {
		// Arrange
		ConfigCommand.config.set(Ghostwriter.GW_GENAI_PROP_NAME, "Configured:Model");
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
		LineReader reader = Mockito.mock(LineReader.class);
		GWCommand cmd = new GWCommand(reader);
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
			LineReader reader = Mockito.mock(LineReader.class);
			GWCommand cmd = new GWCommand(reader);
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

}
