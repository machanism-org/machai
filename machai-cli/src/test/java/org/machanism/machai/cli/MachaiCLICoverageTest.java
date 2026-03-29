package org.machanism.machai.cli;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
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

}
