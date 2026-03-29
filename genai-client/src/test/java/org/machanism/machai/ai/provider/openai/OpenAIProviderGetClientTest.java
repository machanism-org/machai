package org.machanism.machai.ai.provider.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Tests for {@link OpenAIProvider#getClient()} that avoid any network access.
 */
class OpenAIProviderGetClientTest {

	@Test
	void getClient_setsTimeoutFromConfigurator_whenPositive() {
		// Arrange
		OpenAIProvider provider = new OpenAIProvider();
		Configurator config = TestConfigurators.mapBacked();
		config.set("chatModel", "gpt-test");
		config.set("OPENAI_API_KEY", "dummy");
		config.set("OPENAI_BASE_URL", "http://localhost");
		config.set("GENAI_TIMEOUT", "5");
		provider.init(config);

		// Act
		com.openai.client.OpenAIClient client = provider.getClient();

		// Assert
		assertNotNull(client);
		assertEquals(5L, provider.getTimeout());
	}

	@Test
	void getClient_setsTimeoutZeroWhenNonPositiveOrMissing() {
		// Arrange
		OpenAIProvider provider = new OpenAIProvider();
		Configurator config = TestConfigurators.mapBacked();
		config.set("chatModel", "gpt-test");
		config.set("OPENAI_API_KEY", "dummy");
		config.set("OPENAI_BASE_URL", "http://localhost");
		config.set("GENAI_TIMEOUT", "0");
		provider.init(config);

		// Act
		provider.getClient();

		// Assert
		assertEquals(0L, provider.getTimeout());
	}
}
