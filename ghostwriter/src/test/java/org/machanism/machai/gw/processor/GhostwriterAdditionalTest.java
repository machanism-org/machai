package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class GhostwriterAdditionalTest {

	@TempDir
	File tempDir;

	private InputStream originalIn;

	@AfterEach
	void tearDown() {
		if (originalIn != null) {
			System.setIn(originalIn);
		}
	}

	@Test
	void resolveScanDirs_whenArgsProvided_returnsArgs() throws Exception {
		Options options = new Options();
		CommandLine cmd = new DefaultParser().parse(options, new String[] { "src" });
		PropertiesConfigurator config = new PropertiesConfigurator();

		String[] result = Ghostwriter.resolveScanDirs(cmd, config);

		assertArrayEquals(new String[] { "src" }, result);
	}

	@Test
	void resolveScanDirs_whenNoArgsAndConfigHasGwScanDir_usesConfiguredScanDir() throws Exception {
		Options options = new Options();
		CommandLine cmd = new DefaultParser().parse(options, new String[] {});
		PropertiesConfigurator config = new PropertiesConfigurator();
		config.set(Ghostwriter.SCAN_DIR_PROP_NAME, "glob:**/*.java");

		String[] result = Ghostwriter.resolveScanDirs(cmd, config);

		assertArrayEquals(new String[] { "glob:**/*.java" }, result);
	}

	@Test
	void setDegreeOfConcurrency_whenProvided_parsesAndDelegatesToProcessor() {
		RecordingAIFileProcessor p = new RecordingAIFileProcessor(tempDir, new PropertiesConfigurator(), "Any:Model");
		Ghostwriter.initializeConfiguration(tempDir);
		Ghostwriter gw = new Ghostwriter("Any:Model", p);

		gw.setDegreeOfConcurrency("3");

		assertEquals(3, p.lastDegreeOfConcurrency);
	}

	private static final class RecordingAIFileProcessor extends AIFileProcessor {
		private int lastDegreeOfConcurrency = -1;

		private RecordingAIFileProcessor(File rootDir, PropertiesConfigurator configurator, String genai) {
			super(rootDir, configurator, genai);
		}

		@Override
		public void setDegreeOfConcurrency(int data) {
			this.lastDegreeOfConcurrency = data;
			super.setDegreeOfConcurrency(data);
		}
	}
}
