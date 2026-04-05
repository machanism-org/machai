package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class FunctionToolsSetConfiguratorTest {

	private static final class TestTools implements FunctionTools {
		@Override
		public void applyTools(org.machanism.machai.ai.provider.Genai provider) {
			// not used
		}
	}

	@Test
	void setConfigurator_defaultImplementation_isNoOp() {
		// Arrange
		TestTools tools = new TestTools();
		Configurator configurator = null;

		// Act + Assert
		assertDoesNotThrow(() -> tools.setConfigurator(configurator));
	}
}
