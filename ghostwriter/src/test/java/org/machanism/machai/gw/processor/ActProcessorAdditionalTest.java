package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

}
