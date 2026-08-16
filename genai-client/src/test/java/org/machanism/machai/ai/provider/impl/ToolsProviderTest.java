package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.tools.ToolFunction;

import com.fasterxml.jackson.databind.JsonNode;

/** Tests YAML dispatch and result handling of {@link ToolsProvider}. */
class ToolsProviderTest {
    private static final class ExposedToolsProvider extends ToolsProvider {
        void register(String name, ToolFunction function) {
            addTool(name, "test", function);
        }
        void selectYaml() {
            init("yaml", TestConfigurators.mapBacked());
        }
    }

    @Test
    void nonYamlModelDoesNotInvokeTools() {
        // Arrange
        ExposedToolsProvider provider = new ExposedToolsProvider();
        provider.init("other", TestConfigurators.mapBacked());
        provider.prompt("ignored");

        // Act
        String result = provider.perform();

        // Assert
        assertNull(result);
    }

    @Test
    void yamlPromptInvokesToolAndSerializesObjectResult() {
        // Arrange
        ExposedToolsProvider provider = new ExposedToolsProvider();
        provider.selectYaml();
        provider.register("sum", (JsonNode params, Object... context) ->
                Collections.singletonMap("value", params.get("a").asInt() + params.get("b").asInt()));
        provider.prompt("tool: sum\nparams:\n  a: 2\n  b: 3");

        // Act
        String result = provider.perform();

        // Assert
        assertEquals("{\"value\":5}", result);
    }

    @Test
    void yamlPromptReturnsStringResultUnchanged() {
        // Arrange
        ExposedToolsProvider provider = new ExposedToolsProvider();
        provider.selectYaml();
        provider.register("echo", (params, context) -> "done");
        provider.prompt("tool: echo\nparams: {}");

        // Act
        String result = provider.perform();

        // Assert
        assertEquals("done", result);
    }

    @Test
    void yamlPromptWithUnknownToolFailsClearly() {
        // Arrange
        ExposedToolsProvider provider = new ExposedToolsProvider();
        provider.selectYaml();
        provider.prompt("tool: missing\nparams: {}");

        // Act and assert
        assertThrows(IllegalArgumentException.class, provider::perform);
    }
}
