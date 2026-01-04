package org.machanism.machai.ai.manager;

import com.openai.models.ChatModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GenAIProviderManagerTest {
    @Test
    void returnsOpenAIProviderForValidModel() {
        GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo");
        assertNotNull(provider);
        assertTrue(provider instanceof org.machanism.machai.ai.openAI.OpenAIProvider);
    }

    @Test
    void infersOpenAIProviderWhenOnlyModelIsGiven() {
        GenAIProvider provider = GenAIProviderManager.getProvider("gpt-3.5-turbo");
        assertNotNull(provider);
        assertTrue(provider instanceof org.machanism.machai.ai.openAI.OpenAIProvider);
    }

    @Test
    void throwsForUnsupportedProvider() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            GenAIProviderManager.getProvider("OtherAI:model-x");
        });
        assertTrue(exception.getMessage().contains("is not supported"));
    }
}
