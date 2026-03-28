package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.Mockito;

class ActCommandTest {

	@Test
	void init_shouldNotThrow() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		ActCommand cmd = new ActCommand(reader);

		// Act + Assert
		assertDoesNotThrow(cmd::init);
	}

	@Test
	void act_whenGenAiProviderIsNotAvailable_shouldPropagateError() {
		// Arrange
		LineReader reader = Mockito.mock(LineReader.class);
		ActCommand cmd = new ActCommand(reader);
		ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, new File(".").getAbsolutePath());
		ConfigCommand.config.set(Ghostwriter.GW_MODEL_PROP_NAME, "TestProvider");

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> cmd.act(new String[] { "commit" }));
	}
}
