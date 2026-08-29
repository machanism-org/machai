package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;

/** Tests CodeMie model routing without making a remote authentication request. */
class CodeMieProviderDelegationTest {

    // SonarQube java:S5976: model-routing variants share one table-driven test.
    @Test
    void initRoutesModelsToExpectedExtension() {
        for (ModelRoutingExpectation expectation : modelRoutingCases()) {
            assertModelRouting(expectation);
        }
    }

    private static void assertModelRouting(ModelRoutingExpectation expectation) {
        // Arrange
        CodeMieProvider provider = new CodeMieProvider();

        // Act
        provider.init(expectation.model(), credentials());

        // Assert
        assertEquals(expectation.expectedDelegateName(), delegate(provider).getClass().getSimpleName());
    }

    private static List<ModelRoutingExpectation> modelRoutingCases() {
        return Arrays.asList(
                new ModelRoutingExpectation("gemini-2.0", "OpenAIProviderExtension"),
                new ModelRoutingExpectation("claude-3-7-sonnet", "ClaudeProviderExtension"),
                new ModelRoutingExpectation("", "OpenAIProviderExtension"));
    }

    @Test
    void embeddingDelegatesWhenConfiguredDelegateSupportsEmbeddings() {
        // Arrange
        CodeMieProvider provider = new CodeMieProvider();
        List<Double> expected = Arrays.asList(1.0, 2.0);
        EmbeddingGenai delegate = new EmbeddingGenai(expected);
        setDelegate(provider, delegate);

        // Act
        List<Double> result = provider.embedding("text", 2);

        // Assert
        assertEquals(expected, result);
        assertEquals("text", delegate.text);
        assertEquals(2L, delegate.dimensions);
    }

    @Test
    void embeddingRejectsDelegatesWithoutEmbeddingCapability() {
        // Arrange
        CodeMieProvider provider = new CodeMieProvider();
        setDelegate(provider, new NonEmbeddingGenai());

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> provider.embedding("text", 2));
    }

    private static Configurator credentials() {
        Configurator config = mock(Configurator.class);
        when(config.get(anyString())).thenReturn(null);
        when(config.get("GENAI_USERNAME")).thenReturn("client");
        when(config.get("GENAI_PASSWORD")).thenReturn("secret");
        when(config.get(CodeMieProvider.AUTH_URL_PROP_NAME)).thenReturn("http://localhost/token");
        when(config.getLong(anyString(), anyLong())).thenAnswer(invocation -> invocation.getArgument(1));
        return config;
    }

    private static Genai delegate(CodeMieProvider provider) {
        try {
            Field field = org.machanism.machai.ai.provider.GenaiAdapter.class.getDeclaredField("provider");
            field.setAccessible(true);
            return (Genai) field.get(provider);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void setDelegate(CodeMieProvider provider, Genai delegate) {
        provider.setProvider(delegate);
    }

    private static final class ModelRoutingExpectation {
        private final String model;
        private final String expectedDelegateName;

        private ModelRoutingExpectation(String model, String expectedDelegateName) {
            this.model = model;
            this.expectedDelegateName = expectedDelegateName;
        }

        private String model() {
            return model;
        }

        private String expectedDelegateName() {
            return expectedDelegateName;
        }
    }

    private static class NonEmbeddingGenai implements Genai {
        @Override public void init(String model, org.machanism.macha.core.commons.configurator.Configurator conf) {
            // SonarQube java:S1186: test double intentionally has no initialization behavior.
        }
        @Override public void prompt(String text) {
            // SonarQube java:S1186: test double does not retain prompts.
        }
        @Override public void clear() {
            // SonarQube java:S1186: test double has no state to clear.
        }
        @Override public void instructions(String instructions) {
            // SonarQube java:S1186: test double does not use instructions.
        }
        @Override public String perform() { return null; }
        @Override public void setProjectDir(java.io.File projectDir) {
            // SonarQube java:S1186: test double does not use project directories.
        }
        @Override public void addTools(org.machanism.machai.ai.tools.FunctionTools tools, String[] enabledTools) {
            // SonarQube java:S1186: test double does not register tools.
        }
        @Override public void addPrompts(org.machanism.machai.ai.tools.FunctionTools tools) {
            // SonarQube java:S1186: test double does not register prompts.
        }
        @Override public void addResources(org.machanism.machai.ai.tools.FunctionTools tools) {
            // SonarQube java:S1186: test double does not register resources.
        }
        @Override public void setErrorHandling(boolean errorHandling) {
            // SonarQube java:S1186: test double has no error-handling behavior.
        }
    }

    private static final class EmbeddingGenai extends NonEmbeddingGenai implements EmbeddingProvider {
        private final List<Double> response;
        private String text;
        private long dimensions;
        private EmbeddingGenai(List<Double> response) { this.response = response; }
        @Override public List<Double> embedding(String input, long size) { text = input; dimensions = size; return response; }
    }
}
