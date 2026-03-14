package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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
	void setActDir_whenDirectoryNotExists_thenDoesNotThrow() {
		// Arrange
		PropertiesConfigurator config = new PropertiesConfigurator();
		config.set("gw.acts", tempDir.resolve("original").toString());
		ActProcessor processor = new ActProcessor(tempDir.toFile(), config, "Any:Model");

		// Act + Assert
		assertDoesNotThrow(() -> processor.setActsLocation(tempDir.resolve("missing").toString()));
	}

}
