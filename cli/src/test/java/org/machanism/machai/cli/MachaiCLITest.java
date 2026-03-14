package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MachaiCLITest {

	private final String originalConfigProperty = System.getProperty("config");

	@AfterEach
	void tearDown() {
		if (originalConfigProperty == null) {
			System.clearProperty("config");
		} else {
			System.setProperty("config", originalConfigProperty);
		}
	}

	@Test
	void loadSystemProperties_shouldNotThrow_whenDefaultConfigFileDoesNotExist() {
		// Arrange
		System.clearProperty("config");

		// Act + Assert
		assertDoesNotThrow(() -> invokeLoadSystemProperties());
	}

	@Test
	void loadSystemProperties_shouldLoadFromSystemPropertyConfigFile_whenFileExists() throws Exception {
		// Arrange
		File tempProps = File.createTempFile("machai-cli-test", ".properties");
		tempProps.deleteOnExit();
		Files.writeString(tempProps.toPath(), "test.sys.prop=abc\n");
		System.setProperty("config", tempProps.getAbsolutePath());

		// Act
		invokeLoadSystemProperties();

		// Assert
		assertEquals("abc", System.getProperty("test.sys.prop"));
	}

	private static void invokeLoadSystemProperties() throws Exception {
		var m = MachaiCLI.class.getDeclaredMethod("loadSystemProperties");
		m.setAccessible(true);
		m.invoke(null);
	}
}
