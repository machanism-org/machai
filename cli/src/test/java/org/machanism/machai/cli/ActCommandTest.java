package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class ActCommandTest {

	@Test
	void init_shouldNotThrow() {
		// Arrange
		ActCommand cmd = new ActCommand();

		// Act + Assert
		assertDoesNotThrow(cmd::init);
	}

	@Test
	void act_whenGenAiProviderIsNotAvailable_shouldPropagateError() throws IOException {
		// Arrange
		ActCommand cmd = new ActCommand();
		ConfigCommand.config.set("gw.rootDir", new File(".").getAbsolutePath());
		ConfigCommand.config.set("gw.model", "TestProvider");

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> cmd.act(new String[] { "commit" }));
	}
}
