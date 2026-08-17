package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.tools.ParamDescriptor;

import com.anthropic.models.beta.messages.BetaMessageParam;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseUsage;

/** Focused coverage of provider request construction and local state. */
class ProviderCoreCoverageTest {
    @Test
    void openAiBuildsFilteredRequestAndSupportsClear() throws Exception {
        ExposedOpenAI p = new ExposedOpenAI();
        p.init("gpt-5.5-test", TestConfigurators.mapBacked());
        p.instructions("be concise");
        p.setEnabledTools(new String[] { "allowed" });
        p.register("allowed", new ParamDescriptor("q", "string", true, "query", null));
        p.register("blocked");
        p.prompt("hello");
        ResponseCreateParams request = p.request();
        assertEquals("gpt-5.5-test", request.model().get().string().get());
        assertEquals("be concise", request.instructions().get());
        assertEquals(1, request.tools().get().size());
        assertNotNull(request.reasoning());
        p.clear();
        assertTrue(p.inputs.isEmpty());
        p.capture(Optional.empty());
        assertNotNull(UsageStatistics.getAllModelUsages());
    }

    @Test
    void openAiEmbeddingWithNullInputDoesNotCreateAClient() {
        // Arrange
        OpenAIProvider provider = new OpenAIProvider();

        // Act
        java.util.List<Double> result = provider.embedding(null, 3);

        // Assert
        assertNull(result);
    }

    @Test
    void anthropicBuildsRequestAndIgnoresBlankPrompts() throws Exception {
        ExposedAnthropic p = new ExposedAnthropic();
        p.init("claude-test", TestConfigurators.mapBacked());
        p.instructions("system");
        p.prompt(" ");
        p.prompt("question");
        p.register("lookup", new ParamDescriptor("q", "string", true, "query", null));
        MessageCreateParams request = p.request();
        assertTrue(request.model().toString().contains("claude-test"));
        assertTrue(request.system().get().toString().contains("system"));
        assertEquals(1, request.messages().size());
        assertEquals(1, request.tools().get().size());
        p.clear();
        assertTrue(p.inputs().isEmpty());
    }

    @Test
    void codeMieRejectsUnsupportedModelAndUninitializedEmbedding() {
        CodeMieProvider p = new CodeMieProvider();
        assertThrows(IllegalArgumentException.class, () -> p.init("unsupported", TestConfigurators.mapBacked()));
        assertThrows(NullPointerException.class, () -> p.embedding(null, 3));
    }

    private static final class ExposedOpenAI extends OpenAIProvider {
        void register(String name, ParamDescriptor... d) { addTool(name, "tool", (params, context) -> "ok", d); }
        void register(String name) { register(name, new ParamDescriptor[0]); }
        ResponseCreateParams request() throws Exception { return build(inputs); }
        ResponseCreateParams build(java.util.List<?> values) throws Exception {
            Method m = OpenAIProvider.class.getDeclaredMethod("createResponseBuilder", java.util.List.class);
            m.setAccessible(true);
            return (ResponseCreateParams) m.invoke(this, values);
        }
        void invoke(com.openai.models.responses.ResponseFunctionToolCall c) throws Exception {
            Method m = OpenAIProvider.class.getDeclaredMethod("callFunction", c.getClass());
            m.setAccessible(true);
            try { m.invoke(this, c); } catch (java.lang.reflect.InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
                throw e;
            }
        }
        void capture(Optional<ResponseUsage> usage) { captureUsage(usage); }

        Object invoke(Object call) throws Exception {
            Method m = OpenAIProvider.class.getDeclaredMethod("callFunction",
                    com.openai.models.responses.ResponseFunctionToolCall.class);
            m.setAccessible(true);
            try {
                return m.invoke(this, call);
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw e;
            }
        }
    }

    private static final class ExposedAnthropic extends AnthropicProvider {
        void register(String name, ParamDescriptor... d) { addTool(name, "tool", (p, c) -> "ok", d); }
        MessageCreateParams request() throws Exception {
            Method m = AnthropicProvider.class.getDeclaredMethod("createResponseBuilder", java.util.List.class);
            m.setAccessible(true);
            return (MessageCreateParams) m.invoke(this, inputs());
        }
        @SuppressWarnings("unchecked") java.util.List<BetaMessageParam> inputs() throws Exception {
            java.lang.reflect.Field f = AnthropicProvider.class.getDeclaredField("inputs");
            f.setAccessible(true);
            return (java.util.List<BetaMessageParam>) f.get(this);
        }
    }
}
