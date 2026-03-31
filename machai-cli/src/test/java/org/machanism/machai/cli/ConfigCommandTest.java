package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigCommandTest {

	@TempDir
	Path tempDir;

	@Test
	void set_shouldLogCurrentValueWhenValueIsNull() {
		// Arrange
		var config = ConfigCommand.getConfigurator();
		config.set("some.key", "some.value");
		ConfigCommand command = new ConfigCommand();

		// Act / Assert
		assertDoesNotThrow(() -> command.set("some.key", null));
		assertEquals("some.value", config.get("some.key"));
	}

	@Test
	void set_shouldStoreValueInSharedConfigurator_whenValueProvided_andSaveIsAttempted() {
		// Arrange
		String oldUserDir = System.getProperty("user.dir");
		System.setProperty("user.dir", tempDir.toAbsolutePath().toString());
		try {
			ConfigCommand command = new ConfigCommand();

			// Act / Assert
			assertDoesNotThrow(() -> command.set("k", "v"));

			// Assert
			assertEquals("v", ConfigCommand.getConfigurator().get("k"));
		} finally {
			if (oldUserDir != null) {
				System.setProperty("user.dir", oldUserDir);
			}
		}
	}
}
