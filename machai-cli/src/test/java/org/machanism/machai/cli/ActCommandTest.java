package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

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
	void act_whenGenAiProviderIsNotAvailable_shouldPropagateError() {
		// Arrange
		ActCommand cmd = new ActCommand();
		ConfigCommand.config.set("project.dir", new File(".").getAbsolutePath());
		ConfigCommand.config.set("gw.model", "TestProvider");

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> cmd.act(new String[] { "commit" }));
	}
}
