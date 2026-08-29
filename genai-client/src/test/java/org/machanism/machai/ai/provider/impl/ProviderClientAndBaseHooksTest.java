package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;

/** Exercises SDK client configuration without issuing requests and base optional hooks. */
class ProviderClientAndBaseHooksTest {

    @Test
    void openAiClientCanBeBuiltWithConfiguredBaseUrlAndTimeout() {
        // Arrange
        TestConfigurators.MapBackedConfigurator config = TestConfigurators.mapBacked();
        config.put(OpenAIProvider.OPENAI_API_KEY, "test-key");
        config.put(OpenAIProvider.OPENAI_BASE_URL_NAME, "http://localhost:8080");
        config.put("GENAI_TIMEOUT", "2");
        OpenAIProvider provider = new OpenAIProvider();
        provider.init("gpt-test", config);

        // Act / Assert
        assertDoesNotThrow(provider::getClient);
        assertEquals(2L, provider.getTimeout());
    }

    @Test
    void anthropicClientSupportsApiKeyAndAuthTokenConfigurationBranches() {
        // Arrange
        TestConfigurators.MapBackedConfigurator apiKeyConfig = TestConfigurators.mapBacked();
        apiKeyConfig.put(AnthropicProvider.ANTHROPIC_API_KEY, "sk-test-key");
        apiKeyConfig.put(AnthropicProvider.ANTHROPIC_BASE_URL, "http://localhost:8081");
        apiKeyConfig.put("GENAI_TIMEOUT", "1");
        AnthropicProvider apiKeyProvider = new AnthropicProvider();
        apiKeyProvider.init("claude-test", apiKeyConfig);

        TestConfigurators.MapBackedConfigurator tokenConfig = TestConfigurators.mapBacked();
        tokenConfig.put(AnthropicProvider.ANTHROPIC_API_KEY, "bearer-token");
        AnthropicProvider tokenProvider = new AnthropicProvider();
        tokenProvider.init("claude-test", tokenConfig);

        // Act / Assert
        assertDoesNotThrow(apiKeyProvider::getClient);
        assertDoesNotThrow(tokenProvider::getClient);
    }

    @Test
    void baseOptionalHooksAndStateMutatorsAreSafeDefaults() {
        // Arrange
        HookProvider provider = new HookProvider();
        provider.init("model", TestConfigurators.mapBacked());

        // Act
        provider.callBaseHooks();
        provider.prompt("ignored");
        provider.clear();
        provider.instructions("system");
        provider.setProjectDir(new java.io.File("."));
        provider.setTimeout(5L, null);
        provider.setErrorHandling(false);

        // Assert
        assertEquals("", provider.normalized(null));
        assertEquals("mixed", provider.normalized("MiXeD"));
        assertEquals(5L, provider.getTimeout());
        assertFalse(provider.isErrorHandling());
        assertTrue(provider.getProjectDir().isDirectory());
    }

    private static final class HookProvider extends AbstractAIProvider {
        @Override
        protected void addTool(String name, String description, ToolFunction function, ParamDescriptor... params) {
            // Required abstract hook; irrelevant to this test.
        }

        @Override
        public String perform() {
            return null;
        }

        void callBaseHooks() {
            super.addMcpServer("name", "url", null, null);
            super.addWebSearch("type", null, null, null);
        }

        String normalized(String value) {
            return normalize(value);
        }
    }
}
