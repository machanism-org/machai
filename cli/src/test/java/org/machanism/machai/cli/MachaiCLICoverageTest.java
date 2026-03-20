package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MachaiCLICoverageTest {

	@TempDir
	File tempDir;

	private final String originalConfigProp = System.getProperty("config");
	private final String originalCustomProp = System.getProperty("custom.prop");

	@AfterEach
	void tearDown() {
		if (originalConfigProp == null) {
			System.clearProperty("config");
		} else {
			System.setProperty("config", originalConfigProp);
		}

		if (originalCustomProp == null) {
			System.clearProperty("custom.prop");
		} else {
			System.setProperty("custom.prop", originalCustomProp);
		}
	}

	@Test
	void loadSystemProperties_whenConfigPropertySetAndFileExists_loadsIntoSystemProperties() throws Exception {
		// Arrange
		File configFile = new File(tempDir, "machai.properties");
		try (FileOutputStream out = new FileOutputStream(configFile)) {
			out.write("custom.prop=from-file\n".getBytes(StandardCharsets.UTF_8));
		}
		System.setProperty("config", configFile.getAbsolutePath());
		System.clearProperty("custom.prop");

		Method m = MachaiCLI.class.getDeclaredMethod("loadSystemProperties");
		m.setAccessible(true);

		// Act
		m.invoke(null);

		// Assert
		assertEquals("from-file", System.getProperty("custom.prop"));
	}

	@Test
	void loadSystemProperties_whenConfigPropertyPointsToMissingFile_doesNothing() throws Exception {
		// Arrange
		File missing = new File(tempDir, "missing.properties");
		System.setProperty("config", missing.getAbsolutePath());
		System.setProperty("custom.prop", "original");

		Method m = MachaiCLI.class.getDeclaredMethod("loadSystemProperties");
		m.setAccessible(true);

		// Act
		m.invoke(null);

		// Assert
		assertEquals("original", System.getProperty("custom.prop"));
	}
}
