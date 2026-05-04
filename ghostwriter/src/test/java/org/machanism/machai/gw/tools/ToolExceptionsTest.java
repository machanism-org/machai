package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ToolExceptionsTest {

	@Test
	void moveToEpisodeExceptionShouldExposeNullEpisodeAsNextEpisodeMessage() {
		// Arrange
		MoveToEpisodeException exception = new MoveToEpisodeException(null);

		// Act
		String message = exception.getMessage();

		// Assert
		assertEquals("Move to next episode", message);
		assertNull(exception.getEpisodeId());
	}

	@Test
	void moveToEpisodeExceptionShouldExposeSpecificEpisodeId() {
		// Arrange
		MoveToEpisodeException exception = new MoveToEpisodeException("ep-2");

		// Act
		String message = exception.getMessage();

		// Assert
		assertEquals("Move to episode: ep-2", message);
		assertEquals("ep-2", exception.getEpisodeId());
	}

	@Test
	void repeatEpisodeExceptionShouldUseFixedMessage() {
		// Arrange
		RepeatEpisodeException exception = new RepeatEpisodeException();

		// Act
		String message = exception.getMessage();

		// Assert
		assertEquals("Repeat current episode requested.", message);
	}

	@Test
	void denyExceptionShouldPreserveProvidedMessage() {
		// Arrange
		DenyException exception = new DenyException("blocked command");

		// Act
		String message = exception.getMessage();

		// Assert
		assertEquals("blocked command", message);
	}
}
