package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class ActCommandTest {

	@Test
	void init_shouldNotThrow() {
		// Arrange
		ActCommand cmd = new ActCommand();

		// Act + Assert
		assertDoesNotThrow(cmd::init);
	}
}
