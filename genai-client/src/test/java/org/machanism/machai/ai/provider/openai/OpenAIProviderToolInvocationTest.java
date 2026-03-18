package org.machanism.machai.ai.provider.openai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.openai.models.responses.ResponseFunctionToolCall;

/**
 * Verifies that the OpenAI SDK's generated model objects are constructible in tests.
 */
class OpenAIProviderToolInvocationTest {

    @Test
    void responseFunctionToolCall_builderIsAvailable() throws Exception {
        // Arrange/Act
        Object builder = ResponseFunctionToolCall.class.getMethod("builder").invoke(null);

        // Assert
        assertNotNull(builder);
    }
}
