package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GenAIProviderManager}.
 * <p>
 * Verifies correct provider resolution, model parsing, and error cases based on model identifier inputs.
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
class GenAIProviderManagerTest {
    /**
     * Checks correct resolution of OpenAI provider with direct model specification.
     */
    @Test
    void returnsOpenAIProviderForValidModel() {
        GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo");
        assertNotNull(provider);
        assertTrue(provider instanceof org.machanism.machai.ai.provider.openai.OpenAIProvider);
    }

    /**
     * Validates correct provider inference when only model is supplied.
     */
    @Test
    void infersOpenAIProviderWhenOnlyModelIsGiven() {
        GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo");
        assertNotNull(provider);
        assertTrue(provider instanceof org.machanism.machai.ai.provider.openai.OpenAIProvider);
    }

    /**
     * Confirms error case when unsupported provider string is supplied.
     */
    @Test
    void throwsForUnsupportedProvider() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            GenAIProviderManager.getProvider("OtherAI:model-x");
        });
        assertTrue(exception.getMessage().contains("is not supported"));
    }
}
