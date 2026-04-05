package org.machanism.machai.ai.provider.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.tools.ToolFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Unit tests for OpenAIProvider private/package-private helper methods.
 *
 * <p>
 * These tests deliberately avoid network access by using reflection and by
 * exercising only pure logic and error handling paths.
 * </p>
 */
class OpenAIProviderPrivateMethodsTest {

    private final OpenAIProvider provider = new OpenAIProvider();

    @AfterEach
    void tearDown() {
        provider.clear();
    }

    @Test
    void safelyInvokeTool_whenToolThrowsIOException_returnsErrorMessageInsteadOfThrowing() throws Exception {
        // Arrange
        provider.init(minimalConfig());

        ToolFunction tool = args -> {
            throw new IOException("boom");
        };

        Method safelyInvokeTool = OpenAIProvider.class.getDeclaredMethod(
                "safelyInvokeTool",
                String.class,
                ToolFunction.class,
                Object[].class);
        safelyInvokeTool.setAccessible(true);

        // Act
        Object result = safelyInvokeTool.invoke(provider, "t1", tool, new Object[] { new Object[0] });

        // Assert
        assertTrue(result instanceof String);
        assertTrue(((String) result).contains("functional tool call failed"));
        assertTrue(((String) result).contains("t1"));
        assertTrue(((String) result).contains("boom"));
    }

    @Test
    void firstNonBlankReasoning_whenAllBlank_returnsNull() throws Exception {
        // Arrange
        provider.init(minimalConfig());

        com.openai.models.responses.ResponseReasoningItem.Content content =
                com.openai.models.responses.ResponseReasoningItem.Content.builder().text("   ").build();

        Method m = OpenAIProvider.class.getDeclaredMethod("firstNonBlankReasoning", List.class);
        m.setAccessible(true);

        // Act
        Object result = m.invoke(provider, Collections.singletonList(content));

        // Assert
        assertNull(result);
    }

    @Test
    void captureUsage_whenUsageMissing_setsZeroUsage() throws Exception {
        // Arrange
        provider.init(minimalConfig());

        Method captureUsage = OpenAIProvider.class.getDeclaredMethod("captureUsage", java.util.Optional.class);
        captureUsage.setAccessible(true);

        // Act
        captureUsage.invoke(provider, java.util.Optional.empty());

        // Assert
        Usage usage = provider.usage();
        assertEquals(0, usage.getInputTokens());
        assertEquals(0, usage.getInputCachedTokens());
        assertEquals(0, usage.getOutputTokens());
    }

    @Test
    void callFunction_whenArgumentsInvalidJson_throwsIllegalArgumentException() throws Exception {
        // Arrange
        provider.init(minimalConfig());
        provider.addTool("t1", "d", args -> "ok", "a:string:required:x");

        com.openai.models.responses.ResponseFunctionToolCall call = com.openai.models.responses.ResponseFunctionToolCall.builder()
                .callId("c1")
                .name("t1")
                .arguments("{not-json")
                .build();

        Method callFunction = OpenAIProvider.class.getDeclaredMethod("callFunction",
                com.openai.models.responses.ResponseFunctionToolCall.class);
        callFunction.setAccessible(true);

        // Act/Assert
        Exception ex = assertThrows(Exception.class, () -> callFunction.invoke(provider, call));
        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof IllegalArgumentException);
    }

    @Test
    void callFunction_whenToolMatches_passesParsedJsonAndWorkingDirIntoHandler() throws Exception {
        // Arrange
        provider.init(minimalConfig());
        File wd = new File("target");
        provider.setWorkingDir(wd);

        final Object[] captured = new Object[2];
        provider.addTool("t1", "d", args -> {
            captured[0] = args[0];
            captured[1] = args[1];
            return "done";
        }, "a:string:required:x");

        com.openai.models.responses.ResponseFunctionToolCall call = com.openai.models.responses.ResponseFunctionToolCall.builder()
                .callId("c1")
                .name("t1")
                .arguments("{\"a\":\"v\"}")
                .build();

        Method callFunction = OpenAIProvider.class.getDeclaredMethod("callFunction",
                com.openai.models.responses.ResponseFunctionToolCall.class);
        callFunction.setAccessible(true);

        // Act
        Object result = callFunction.invoke(provider, call);

        // Assert
        assertEquals("done", result);
        assertNotNull(captured[0]);
        assertEquals(wd, captured[1]);
        assertEquals("v", ((TextNode) ((JsonNode) captured[0]).get("a")).asText());
    }

    @Test
    void callFunction_whenNoMatchingTool_returnsNull() throws Exception {
        // Arrange
        provider.init(minimalConfig());
        provider.addTool("t1", "d", args -> "ok", "a:string:required:x");

        com.openai.models.responses.ResponseFunctionToolCall call = com.openai.models.responses.ResponseFunctionToolCall.builder()
                .callId("c1")
                .name("missing")
                .arguments("{\"a\":\"v\"}")
                .build();

        Method callFunction = OpenAIProvider.class.getDeclaredMethod("callFunction",
                com.openai.models.responses.ResponseFunctionToolCall.class);
        callFunction.setAccessible(true);

        // Act
        Object result = callFunction.invoke(provider, call);

        // Assert
        assertNull(result);
    }

    private Configurator minimalConfig() {
        Configurator config = TestConfigurators.mapBacked();
        config.set("chatModel", "gpt-test");
        config.set("OPENAI_API_KEY", "dummy");
        return config;
    }
}
