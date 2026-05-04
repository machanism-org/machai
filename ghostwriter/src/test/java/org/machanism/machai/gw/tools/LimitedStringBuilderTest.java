package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LimitedStringBuilderTest {

	@Test
	void constructorShouldRejectNonPositiveMaxSize() {
		// Arrange
		int invalidSize = 0;

		// Act
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new LimitedStringBuilder(invalidSize));

		// Assert
		assertEquals("maxSize must be positive", exception.getMessage());
	}

	@Test
	void appendShouldIgnoreNullAndSupportFluentUsage() {
		// Arrange
		LimitedStringBuilder builder = new LimitedStringBuilder(5);

		// Act
		LimitedStringBuilder returned = builder.append(null).append("abc");

		// Assert
		assertSame(builder, returned);
		assertEquals(3, builder.length());
		assertEquals("abc", builder.getLastText());
	}

	@Test
	void appendShouldKeepOnlyLastCharactersWhenTextExceedsLimit() {
		// Arrange
		LimitedStringBuilder builder = new LimitedStringBuilder(5);

		// Act
		builder.append("ab").append("cdefgh");

		// Assert
		assertEquals(5, builder.length());
		assertEquals("(Previous content has been truncated)...defgh", builder.getLastText());
	}

	@Test
	void clearShouldRemoveContentAndResetTruncationPrefix() {
		// Arrange
		LimitedStringBuilder builder = new LimitedStringBuilder(4);
		builder.append("123456");

		// Act
		builder.clear();
		builder.append("xy");

		// Assert
		assertEquals(2, builder.length());
		assertEquals("xy", builder.getLastText());
	}
}
