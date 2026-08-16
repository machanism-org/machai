package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.tools.SpecialException;
import org.machanism.machai.ai.tools.ToolFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Tests common provider state and protected tool error semantics. */
class AbstractAIProviderTest {
    private static final class ExposedProvider extends AbstractAIProvider {
        @Override protected void addTool(String n, String d, ToolFunction f, org.machanism.machai.ai.tools.ParamDescriptor... p) { }
        void initialize() { init("model", TestConfigurators.mapBacked()); }
        @Override public String perform() { return null; }
        Object invoke(String name, ToolFunction tool, com.fasterxml.jackson.databind.JsonNode node) { return safelyInvokeTool(name, tool, node, null); }
    }

    @Test
    void toolErrorsBecomeModelMessageWhenHandlingEnabled() throws Exception {
        // Arrange
        ExposedProvider provider = new ExposedProvider();
        provider.initialize();
        ObjectNode params = new ObjectMapper().createObjectNode();

        // Act
        Object result = provider.invoke("bad", (p, context) -> { throw new Exception("boom"); }, params);

        // Assert
        assertTrue(result.toString().contains(AbstractAIProvider.ERROR_TOOL_RESULT_PREFIX));
        assertTrue(result.toString().contains("boom"));
    }

    @Test
    void disabledErrorHandlingWrapsFailureInSpecialException() {
        // Arrange
        ExposedProvider provider = new ExposedProvider();
        provider.initialize();
        provider.setErrorHandling(false);

        // Act and assert
        assertThrows(SpecialException.class, () -> provider.invoke("bad", (p, context) -> {
            throw new Exception("boom");
        }, new ObjectMapper().createObjectNode()));
    }
}
