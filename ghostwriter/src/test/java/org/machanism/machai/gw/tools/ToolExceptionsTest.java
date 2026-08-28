package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ToolExceptionsTest {

	@Test
	void moveToEpisodeExceptionShouldExposeNullEpisodeAsNextEpisodeMessage() {
		// Arrange
		MoveToEpisodeException exception = new MoveToEpisodeException(null, null);

		// Act
		String message = exception.getMessage();

		// Assert
		assertEquals("Move to next episode", message);
		assertNull(exception.getEpisodeId());
	}

	@Test
	void moveToEpisodeExceptionShouldExposeSpecificEpisodeId() {
		// Arrange
		MoveToEpisodeException exception = new MoveToEpisodeException(2, null);

		// Act
		String message = exception.getMessage();

		// Assert
		assertEquals("Move to episode: 2", message);
		assertEquals(2, exception.getEpisodeId());
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
	void actSpecToolsShouldSignalRequestedNavigationAndRepeat() {
		// Arrange
		ActSpecFunctionTools tools = new ActSpecFunctionTools();

		// Act
		MoveToEpisodeException navigation = org.junit.jupiter.api.Assertions
				.assertThrows(MoveToEpisodeException.class, () -> tools.moveToEpisode(4, "Review"));
		RepeatEpisodeException repeat = org.junit.jupiter.api.Assertions
				.assertThrows(RepeatEpisodeException.class, () -> tools.repeateEpisode(""));

		// Assert
		assertEquals(Integer.valueOf(4), navigation.getEpisodeId());
		assertEquals("Review", navigation.getName());
		assertEquals("Move to episode: 4", navigation.getMessage());
		assertEquals("Repeat current episode requested.", repeat.getMessage());
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
