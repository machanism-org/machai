package org.machanism.machai.ai.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Resource;
import org.machanism.machai.ai.tools.Role;
import org.machanism.machai.ai.tools.SpecialException;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.ToolFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Exercises reflection-based registration and invocation shared by providers. */
class AbstractAIProviderRegistrationTest {
    @Test
    void registersAnnotatedMembersInvokesThemAndHonorsToolFilter() throws Exception {
        CapturingProvider provider = new CapturingProvider();
        provider.init("model", configuration());
        provider.setProjectDir(new File("project"));
        SampleTools tools = new SampleTools();
        ObjectNode input = new ObjectMapper().createObjectNode().put("value", "actual");

        provider.addTools(tools, new String[] { ".*:renamed" });
        provider.addPrompts(tools);
        provider.addResources(tools);
        Object toolResult = provider.tools.get(0).function.apply(input, new File("project"));
        Object promptResult = provider.prompts.get(0).function.apply(input, new File("project"));
        Object resourceResult = provider.resources.get(0).function.apply(input, new File("project"));

        assertEquals(1, provider.tools.size());
        assertEquals("renamed", provider.tools.get(0).name);
        assertEquals("actual-actual", toolResult);
        assertEquals("actual", promptResult);
        assertEquals("resource:actual", resourceResult);
        assertEquals("promptMethod", provider.prompts.get(0).name);
        assertEquals(Role.ASSISTANT, provider.prompts.get(0).role);
        assertEquals(URI.create("file:///example.txt"), provider.resources.get(0).uri);
        assertEquals(1, provider.tools.get(0).params.length);
        assertFalse(provider.tools.get(0).params[0].isRequired());
    }

    @Test
    void exposesParameterFallbacksAndStrictToolFailureBehavior() {
        CapturingProvider provider = new CapturingProvider();
        provider.init("model", configuration());
        ObjectNode input = new ObjectMapper().createObjectNode();
        input.putObject("object");

        assertEquals("fallback", provider.value(input, "missing", "fallback"));
        assertEquals("", provider.value(input, "object", "fallback"));
        assertTrue(provider.safely("bad", (props, context) -> { throw new IllegalStateException("nope"); }).toString().contains("nope"));
        assertThrows(SpecialException.class, () -> provider.safely("special", (props, context) -> { throw new SpecialException("stop"); }));
    }

    private static Configurator configuration() {
        Configurator config = mock(Configurator.class);
        when(config.getLong("MAX_OUTPUT_TOKENS", AbstractAIProvider.MAX_OUTPUT_TOKENS)).thenReturn(12L);
        when(config.getLong("MAX_TOOL_CALLS", 0L)).thenReturn(2L);
        return config;
    }

    static final class SampleTools implements FunctionTools {
        @Tool(name = "renamed", description = "tool ${OS_NAME}")
        public String tool(@Param(name = "value", description = "value", defaultValue = "default") String value) { return value + "-${value}"; }
        @Prompt(description = "prompt", role = Role.ASSISTANT)
        public String promptMethod(@Param(name = "value", description = "value") String value) { return value; }
        @Resource(uri = { "file:///example.txt" }, description = "resource", mimeType = "text/plain")
        public String resource(@Param(name = "value", description = "value") String value) { return "resource:" + value; }
    }

    static final class CapturingProvider extends AbstractAIProvider {
        final List<Entry> tools = new ArrayList<>(); final List<PromptEntry> prompts = new ArrayList<>(); final List<ResourceEntry> resources = new ArrayList<>();
        @Override protected void addTool(String n, String d, ToolFunction f, ParamDescriptor... p) { tools.add(new Entry(n, f, p)); }
        @Override protected void addPrompt(String n, String d, ToolFunction f, Role r, ParamDescriptor... p) { prompts.add(new PromptEntry(n, f, r)); }
        @Override protected void addResource(URI u, String d, String m, ToolFunction f, ParamDescriptor... p) { resources.add(new ResourceEntry(u, f)); }
        @Override public String perform() { return null; }
        String value(ObjectNode n, String key, String fallback) { return getParamValue(n, key, fallback); }
        Object safely(String name, ToolFunction function) { return safelyInvokeTool(name, function, new ObjectMapper().createObjectNode(), null); }
    }
    static final class Entry { final String name; final ToolFunction function; final ParamDescriptor[] params; Entry(String n, ToolFunction f, ParamDescriptor[] p) { name=n; function=f; params=p; } }
    static final class PromptEntry { final String name; final ToolFunction function; final Role role; PromptEntry(String n, ToolFunction f, Role r) { name=n; function=f; role=r; } }
    static final class ResourceEntry { final URI uri; final ToolFunction function; ResourceEntry(URI u, ToolFunction f) { uri=u; function=f; } }
}
