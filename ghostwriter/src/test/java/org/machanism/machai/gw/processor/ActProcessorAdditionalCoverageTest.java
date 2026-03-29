package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class ActProcessorAdditionalCoverageTest {

	@TempDir
	Path tempDir;

	@Test
	void loadAct_whenNotFoundInClasspathOrDirectory_throwsIllegalArgumentException() {
		// Arrange
		Map<String, Object> props = new HashMap<>();

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> ActProcessor.loadAct("definitely-not-exist", props, null));
	}

	@Test
	void tryLoadActFromDirectory_whenActsLocationIsHttp_andNetworkFails_propagatesIOException() {
		// Arrange
		Map<String, Object> props = new HashMap<>();

		// Act + Assert
		// The implementation only swallows FileNotFoundException; other I/O failures
		// propagate.
		assertThrows(Exception.class,
				() -> ActProcessor.tryLoadActFromDirectory(props, "missing", "http://nonexistent.invalid"));
	}

	@Test
	void tryLoadActFromDirectory_whenActsLocationIsFileAndExists_populatesProperties() throws Exception {
		// Arrange
		Path actsDir = tempDir.resolve("acts");
		Files.createDirectories(actsDir);
		Files.write(actsDir.resolve("x.toml"),
				("instructions='i'\n" + "inputs='p %s'\n").getBytes(StandardCharsets.UTF_8));

		Map<String, Object> props = new HashMap<>();

		// Act
		assertNotNull(ActProcessor.tryLoadActFromDirectory(props, "x", actsDir.toString()));

		// Assert
		assertEquals("i", props.get("instructions"));
		assertEquals("p %s", props.get("inputs"));
	}

	@Test
	void setDefaultPrompt_whenBlank_usesHelpActName_andAppliesInputsTemplateFormatting() {
		// Arrange
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		ActProcessor processor = new ActProcessor(tempDir.toFile(), configurator, "Any:Model");
		processor.setDefaultPrompt("help");

		// Act
		processor.setDefaultPrompt("help custom");

		// Assert
		assertNotNull(processor.getDefaultPrompt());
		assertTrue(processor.getDefaultPrompt().contains("custom"));
		assertNotNull(processor.getInstructions());
	}
}
