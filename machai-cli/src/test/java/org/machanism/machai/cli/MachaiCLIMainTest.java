package org.machanism.machai.cli;

import java.io.File;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
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

}
