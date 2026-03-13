package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class ActProcessorAdditionalTest {

	@TempDir
	Path tempDir;

	@Test
	void loadAct_whenMissingEverywhere_thenThrowsIllegalArgumentException() {
		// Arrange
		Map<String, Object> props = new HashMap<String, Object>();

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ActProcessor.loadAct("__missing_act__", props, null));

		// Assert
		assertTrue(ex.getMessage().contains("not found"));
	}

	@Test
	void loadAct_whenCustomOverridesClasspath_thenCustomTakesPrecedence() throws IOException {
		// Arrange
		Path actsDir = tempDir.resolve("acts");
		Files.createDirectories(actsDir);
		Files.write(actsDir.resolve("help.toml"), "description='CUSTOM HELP'\n".getBytes(StandardCharsets.UTF_8));

		Map<String, Object> props = new HashMap<String, Object>();

		// Act
		ActProcessor.loadAct("help", props, actsDir.toString());

		// Assert
		assertEquals("CUSTOM HELP", props.get("description"));
	}

	@Test
	void setDefaultPrompt_whenBlank_thenDefaultsToHelpAndLoadsAct() {
		// Arrange
		PropertiesConfigurator config = new PropertiesConfigurator();
		ActProcessor processor = new ActProcessor(tempDir.toFile(), config, "Any:Model");

		// Act
		processor.setDefaultPrompt(" ");

		// Assert
		assertNotNull(processor.getDefaultPrompt());
		assertFalse(processor.getDefaultPrompt().trim().isEmpty());
	}

	@Test
	void setActDir_whenDirectoryNotExists_thenDoesNotThrow() {
		// Arrange
		PropertiesConfigurator config = new PropertiesConfigurator();
		config.set("gw.acts", tempDir.resolve("original").toString());
		ActProcessor processor = new ActProcessor(tempDir.toFile(), config, "Any:Model");

		// Act + Assert
		assertDoesNotThrow(() -> processor.setActDir(tempDir.resolve("missing").toString()));
	}

	@Test
	void runRelatedActs_whenListProvided_thenClearsListBeforeRunToAvoidInfiniteLoop() throws Exception {
		// Arrange
		PropertiesConfigurator config = new PropertiesConfigurator();

		class TestActProcessor extends ActProcessor {
			int runs = 0;

			TestActProcessor(File rootDir, PropertiesConfigurator configurator, String genai) {
				super(rootDir, configurator, genai);
			}

			@Override
			public void scanDocuments(File projectDir, String scanDir) {
				runs++;
			}
		}

		TestActProcessor processor = new TestActProcessor(tempDir.toFile(), config, "Any:Model");
		List<String> related = new java.util.ArrayList<String>();
		related.add("help");

		// Act
		processor.runRelatedActs(related);

		// Assert
		assertEquals(1, processor.runs);
		assertTrue(related.isEmpty(), "Expected list to be cleared by runRelatedActs");
	}
}
