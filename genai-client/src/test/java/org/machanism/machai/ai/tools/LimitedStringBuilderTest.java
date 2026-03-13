package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LimitedStringBuilderTest {

	@Test
	void constructor_whenMaxSizeNonPositive_throwsIllegalArgumentException() {
		// Arrange
		int maxSize = 0;

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new LimitedStringBuilder(maxSize));
	}

	@Test
	void append_whenNull_doesNothingAndKeepsLength() {
		// Arrange
		LimitedStringBuilder sb = new LimitedStringBuilder(10);
		sb.append("abc");

		// Act
		sb.append(null);

		// Assert
		assertEquals(3, sb.length());
		assertEquals("abc", sb.getLastText());
	}

	@Test
	void append_whenWithinLimit_doesNotTruncateAndNoPrefix() {
		// Arrange
		LimitedStringBuilder sb = new LimitedStringBuilder(10);

		// Act
		sb.append("hello");

		// Assert
		assertEquals(5, sb.length());
		assertEquals("hello", sb.getLastText());
	}

	@Test
	void append_whenExceedsLimit_truncatesFromStartAndAddsPrefix() {
		// Arrange
		LimitedStringBuilder sb = new LimitedStringBuilder(5);

		// Act
		sb.append("hello").append("world");

		// Assert
		assertEquals(5, sb.length());
		assertEquals("(Previous content has been truncated)...world", sb.getLastText());
	}

	@Test
	void getLastText_whenExactlyMaxSize_doesNotAddPrefix() {
		// Arrange
		LimitedStringBuilder sb = new LimitedStringBuilder(5);

		// Act
		sb.append("hello");

		// Assert
		assertEquals("hello", sb.getLastText());
	}

	@Test
	void clear_resetsRetainedContent() {
		// Arrange
		LimitedStringBuilder sb = new LimitedStringBuilder(5);
		sb.append("hello");

		// Act
		sb.clear();

		// Assert
		assertEquals(0, sb.length());
		assertEquals("", sb.getLastText());
	}
}
