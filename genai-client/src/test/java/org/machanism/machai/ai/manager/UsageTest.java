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

	@Test
	void constructorAndGetters_preserveBoundaryAndNegativeValuesReportedByProviders() {
		// Arrange
		long input = Long.MAX_VALUE;
		long cached = Long.MIN_VALUE;
		long output = -1;

		// Act
		Usage usage = new Usage(input, cached, output);

		// Assert
		assertEquals(Long.MAX_VALUE, usage.getInputTokens());
		assertEquals(Long.MIN_VALUE, usage.getInputCachedTokens());
		assertEquals(-1, usage.getOutputTokens());
	}
}
