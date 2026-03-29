package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
	void readText_whenMultiLineInput_joinsLinesAndStripsTrailingBreaker() {
		// Arrange
		Ghostwriter.initializeConfiguration(tempDir);

		originalIn = System.in;
		System.setIn(new ByteArrayInputStream(("a\\\n" + "b\\\n" + "c\n").getBytes(StandardCharsets.UTF_8)));

		// Act
		String text = Ghostwriter.readText("Prompt");

		// Assert
		assertEquals("a\nb\nc", text);
	}

	@Test
	void resolveScanDirs_whenArgsProvided_returnsArgs() throws Exception {
		// Arrange
		Options options = new Options();
		CommandLine cmd = new DefaultParser().parse(options, new String[] { "src" });
		PropertiesConfigurator config = new PropertiesConfigurator();

		// Act
		String[] result = Ghostwriter.resolveScanDirs(cmd, config);

		// Assert
		assertArrayEquals(new String[] { "src" }, result);
	}

	@Test
	void resolveScanDirs_whenNoArgsAndConfigHasGwScanDir_usesConfiguredScanDir() throws Exception {
		// Arrange
		Options options = new Options();
		CommandLine cmd = new DefaultParser().parse(options, new String[] {});
		PropertiesConfigurator config = new PropertiesConfigurator();
		config.set(Ghostwriter.SCAN_DIR_PROP_NAME, "glob:**/*.java");

		// Act
		String[] result = Ghostwriter.resolveScanDirs(cmd, config);

		// Assert
		assertArrayEquals(new String[] { "glob:**/*.java" }, result);
	}

	@Test
	void setDegreeOfConcurrency_whenProvided_parsesAndDelegatesToProcessor() {
		// Arrange
		RecordingAIFileProcessor p = new RecordingAIFileProcessor(tempDir, new PropertiesConfigurator(), "Any:Model");
		Ghostwriter.initializeConfiguration(tempDir);
		Ghostwriter gw = new Ghostwriter("Any:Model", p);

		// Act
		gw.setDegreeOfConcurrency("3");

		// Assert
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
