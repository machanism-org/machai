package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class GhostwriterTest {

	@TempDir
	File tempDir;

	@Test
	void constructor_shouldRejectBlankGenai() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new Ghostwriter(" ", new GuidanceProcessor(tempDir, "model", new PropertiesConfigurator())));
		assertTrue(ex.getMessage().contains(Ghostwriter.GW_GENAI_PROP_NAME));
	}

	@Test
	void resolveScanDirs_shouldPreferArgsThenConfigThenUserDir() throws Exception {
		PropertiesConfigurator config = new PropertiesConfigurator();

		Options options = new Options();
		CommandLine cmdWithArgs = new DefaultParser().parse(options, new String[] { "src" }, true);
		String[] scanDirs = Ghostwriter.resolveScanDirs(cmdWithArgs, config);
		assertArrayEquals(new String[] { "src" }, scanDirs);

		config.set(Ghostwriter.GW_SCAN_DIR_PROP_NAME, "fromConfig");
		CommandLine cmdNoArgs = new DefaultParser().parse(options, new String[] {}, true);
		String[] scanDirs2 = Ghostwriter.resolveScanDirs(cmdNoArgs, config);
		assertArrayEquals(new String[] { "fromConfig" }, scanDirs2);
	}

	@Test
	void logDefaultPrompt_shouldNotThrowOnNull() {
		Ghostwriter.logDefaultPrompt("Label", null);
		Ghostwriter.logDefaultPrompt("Label", "some prompt");
	}

	@Test
	void initializeConfiguration_shouldRespectProvidedGwHomeProperty() {
		String prev = System.getProperty(Ghostwriter.GW_HOME_PROP_NAME);
		try {
			System.setProperty(Ghostwriter.GW_HOME_PROP_NAME, tempDir.getAbsolutePath());
			Ghostwriter.initializeConfiguration(null);
			assertEquals(tempDir.getAbsolutePath(), System.getProperty(Ghostwriter.GW_HOME_PROP_NAME));
		} finally {
			if (prev != null) {
				System.setProperty(Ghostwriter.GW_HOME_PROP_NAME, prev);
			} else {
				System.clearProperty(Ghostwriter.GW_HOME_PROP_NAME);
			}
		}
	}
}
