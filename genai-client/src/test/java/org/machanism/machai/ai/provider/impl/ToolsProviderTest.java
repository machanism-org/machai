package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Tool;

/** Tests the local YAML tool execution path without contacting an AI service. */
class ToolsProviderTest {

    @Test
    void returnsNullWhenModelIsNotYaml() {
        ToolsProvider provider = initialized("other");

        provider.prompt("tool: echo");

        assertNull(provider.perform());
    }

    @Test
    void invokesRegisteredToolAndSerializesNonStringResult() {
        ToolsProvider provider = initialized("yaml");
        provider.addTools(new LocalTools(), null);
        provider.setProjectDir(new File("work"));

        provider.prompt("tool: details\nparams:\n  name: Ada");

        assertEquals("{\"name\":\"Ada\",\"kind\":\"local\"}", provider.perform());
    }

    @Test
    void invokesRegisteredToolAndReturnsStringResult() {
        ToolsProvider provider = initialized("yaml");
        provider.addTools(new LocalTools(), null);

        provider.prompt("tool: echo\nparams:\n  name: Ada");

        assertEquals("hello Ada", provider.perform());
    }

    @Test
    void throwsHelpfulErrorForUnknownYamlTool() {
        ToolsProvider provider = initialized("yaml");

        provider.prompt("tool: absent\nparams: {}");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, provider::perform);
        assertEquals("Functional tool: `absent` not found.", exception.getMessage());
    }

    @Test
    void propagatesToolFailuresBecauseToolsProviderIsFailFast() {
        ToolsProvider provider = initialized("yaml");
        provider.addTools(new LocalTools(), null);

        provider.prompt("tool: fail\nparams: {}");

        assertThrows(RuntimeException.class, provider::perform);
    }

    private static ToolsProvider initialized(String model) {
        Configurator config = mock(Configurator.class);
        when(config.getLong("MAX_OUTPUT_TOKENS", AbstractAIProvider.MAX_OUTPUT_TOKENS)).thenReturn(10L);
        when(config.getLong("MAX_TOOL_CALLS", 0L)).thenReturn(0L);
        ToolsProvider provider = new ToolsProvider();
        provider.init(model, config);
        return provider;
    }

    public static final class LocalTools implements FunctionTools {
        @Tool(description = "echo")
        public String echo(@Param(name = "name", description = "person") String name) {
            return "hello " + name;
        }

        @Tool(description = "details")
        public Map<String, String> details(@Param(name = "name", description = "person") String name) {
            Map<String, String> result = new LinkedHashMap<>();
            result.put("name", name);
            result.put("kind", "local");
            return result;
        }

        @Tool(description = "failing tool")
        public String fail() {
            throw new IllegalStateException("broken");
        }
    }
}
