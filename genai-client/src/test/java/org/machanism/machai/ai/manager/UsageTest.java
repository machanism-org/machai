package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UsageTest {

	@Test
	void constructorAndGetters_returnProvidedValues() {
		// Arrange
		long input = 11;
		long cached = 22;
		long output = 33;

		// Act
		Usage usage = new Usage(input, cached, output);

		// Assert
		assertEquals(input, usage.getInputTokens());
		assertEquals(cached, usage.getInputCachedTokens());
		assertEquals(output, usage.getOutputTokens());
	}
}
