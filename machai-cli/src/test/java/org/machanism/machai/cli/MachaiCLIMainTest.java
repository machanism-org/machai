package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MachaiCLIMainTest {

	@TempDir
	File tempDir;

	private String previousConfig;
	private Properties previousSystemProperties;

	@AfterEach
	void restoreSystemState() {
		if (previousConfig == null) {
			System.clearProperty("config");
		} else {
			System.setProperty("config", previousConfig);
		}
		if (previousSystemProperties != null) {
			System.setProperties(previousSystemProperties);
		}
	}

	@Test
	void loadSystemProperties_whenConfigPropertyPointsToExistingFile_shouldMergeIntoSystemProperties() throws Exception {
		// Arrange
		previousConfig = System.getProperty("config");
		previousSystemProperties = (Properties) System.getProperties().clone();

		File propsFile = new File(tempDir, "machai.properties");
		Files.createDirectories(tempDir.toPath());
		Properties p = new Properties();
		p.setProperty("test.key", "test.value");
		try (FileOutputStream fos = new FileOutputStream(propsFile)) {
			p.store(fos, "test");
		}
		System.setProperty("config", propsFile.getAbsolutePath());

		Method m = MachaiCLI.class.getDeclaredMethod("loadSystemProperties");
		m.setAccessible(true);

		// Act
		m.invoke(null);

		// Assert
		assertEquals("test.value", System.getProperty("test.key"));
		assertTrue(new File(System.getProperty("config")).exists());
	}

	@Test
	void loadSystemProperties_whenConfigPropertyNotSet_shouldNotThrowAndShouldNotSetNewProperties() throws Exception {
		// Arrange
		previousConfig = System.getProperty("config");
		previousSystemProperties = (Properties) System.getProperties().clone();
		System.clearProperty("config");
		System.clearProperty("test.key");

		Method m = MachaiCLI.class.getDeclaredMethod("loadSystemProperties");
		m.setAccessible(true);

		// Act + Assert
		assertDoesNotThrow(() -> m.invoke(null));
		assertNull(System.getProperty("test.key"));
	}
}
