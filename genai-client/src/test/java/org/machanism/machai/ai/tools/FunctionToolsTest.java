package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;

class FunctionToolsTest {

	private static final class RecordingFunctionTools implements FunctionTools {

		private Genai lastProvider;

		@Override
		public void applyTools(Genai provider) {
			lastProvider = provider;
		}
	}

	@Test
	void applyTools_whenImplemented_receivesProviderInstance() {
		// Arrange
		RecordingFunctionTools tools = new RecordingFunctionTools();
		Genai provider = null;

		// Act
		tools.applyTools(provider);

		// Assert
		assertSame(provider, tools.lastProvider);
	}

	@Test
	void setConfigurator_defaultImplementation_acceptsNullConfigurator() {
		// Arrange
		RecordingFunctionTools tools = new RecordingFunctionTools();
		Configurator configurator = null;

		// Act
		tools.setConfigurator(configurator);

		// Assert
		assertSame(null, configurator);
	}
}
