package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CodeMieProviderInitTest {

	@Test
	void unsupportedModelIsRejectedWithoutCallingRemoteAuthorization() {
		CodeMieProvider provider = new CodeMieProvider();
		TestConfigurators.MapBackedConfigurator config = TestConfigurators.mapBacked();
		config.put("GENAI_USERNAME", "client");
		config.put("GENAI_PASSWORD", "secret");

		assertThrows(IllegalArgumentException.class, () -> provider.init("x-model", config));
	}
}
