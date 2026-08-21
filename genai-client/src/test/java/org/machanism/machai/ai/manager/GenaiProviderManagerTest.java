package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;

class GenaiProviderManagerTest {

    @Test
    void getProviderReturnsNullWhenProviderNameIsMissing() {
        // Arrange
        Configurator configuration = null;

        // Act
        Genai result = GenaiProviderManager.getProvider(":model", configuration);

        // Assert
        assertNull(result);
    }

    @Test
    void getProviderReturnsNullWhenModelSpecificationIsNull() {
        // Arrange
        Configurator configuration = null;

        // Act
        Genai result = GenaiProviderManager.getProvider(null, configuration);

        // Assert
        assertNull(result);
    }

    @Test
    void getEmbeddingProviderReturnsNullForNullModelSpecification() {
        // Arrange

        // Act
        EmbeddingProvider result = GenaiProviderManager.getEmbeddingProvider(null, null);

        // Assert
        assertNull(result);
    }

    @Test
    void getProviderReturnsNullWhenProviderNameContainsOnlyWhitespace() {
        // Arrange
        Configurator configuration = null;

        // Act
        Genai result = GenaiProviderManager.getProvider("   :model", configuration);

        // Assert
        assertNull(result);
    }

    @Test
    void getProviderSupportsSpecificationWithoutExplicitModel() {
        // Arrange
        Configurator configuration = null;

        // Act
        Genai result = GenaiProviderManager.getProvider("None", configuration);

        // Assert
        assertNotNull(result);
        assertNull(result.perform());
    }


    @Test
    void getProviderInitializesConventionalNoneProvider() {
        // Arrange
        Configurator configuration = null;

        // Act
        Genai result = GenaiProviderManager.getProvider("None:log", configuration);

        // Assert
        assertEquals("org.machanism.machai.ai.provider.impl.NoneProvider", result.getClass().getName());
        assertNull(result.perform());
    }

    @Test
    void getProviderRejectsProviderNamesThatAreNotJavaIdentifiers() {
        // Arrange
        Configurator configuration = null;

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getProvider("bad-name:model", configuration));

        // Assert
        assertEquals("Invalid provider name: `bad-name`. Expected format is `Provider:Model` (e.g., `OpenAI:gpt-4`). Please specify both provider and model separated by a colon.",
                exception.getMessage());
    }

    @Test
    void getProviderRejectsFullyQualifiedNamesBecauseChatProvidersRequireSimpleNames() {
        // Arrange
        Configurator configuration = null;

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getProvider("some.package.Provider:model", configuration));

        // Assert
        assertEquals("Invalid provider name: `some.package.Provider`. Expected format is `Provider:Model` (e.g., `OpenAI:gpt-4`). Please specify both provider and model separated by a colon.",
                exception.getMessage());
    }

    @Test
    void resolveClassNameUsesNestedFallbackForUnavailableConventionalClass() throws Exception {
        // Arrange
        Method resolver = GenaiProviderManager.class.getDeclaredMethod("resolveClassName", String.class);
        resolver.setAccessible(true);

        // Act
        String result = (String) resolver.invoke(null, "UnavailableProvider");

        // Assert
        assertEquals(GenaiProviderManager.class.getName() + "$UnavailableProviderProvider", result);
    }

    @Test
    void resolveClassNamePreservesFullyQualifiedProviderNames() throws Exception {
        // Arrange
        Method resolver = GenaiProviderManager.class.getDeclaredMethod("resolveClassName", String.class);
        resolver.setAccessible(true);
        String fullyQualifiedName = FullyQualifiedEmbeddingProvider.class.getName();

        // Act
        String result = (String) resolver.invoke(null, fullyQualifiedName);

        // Assert
        assertEquals(fullyQualifiedName, result);
    }

    @Test
    void getProviderReportsUnsupportedConventionalProvider() {
        // Arrange
        Configurator configuration = null;

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getProvider("DoesNotExist:model", configuration));

        // Assert
        assertEquals("Failed to initialize GenAI provider 'DoesNotExist': provider is not supported or an error occurred during initialization.",
                exception.getMessage());
    }

    @Test
    void getEmbeddingProviderReturnsNullWhenProviderNameIsBlank() {
        // Arrange

        // Act
        EmbeddingProvider result = GenaiProviderManager.getEmbeddingProvider(":embedding", null);

        // Assert
        assertNull(result);
    }

    @Test
    void getEmbeddingProviderInitializesFullyQualifiedImplementation() {
        // Arrange
        String providerName = FullyQualifiedEmbeddingProvider.class.getName();
        Configurator configuration = null;

        // Act
        FullyQualifiedEmbeddingProvider result = (FullyQualifiedEmbeddingProvider)
                GenaiProviderManager.getEmbeddingProvider(providerName + ":vector-model", configuration);

        // Assert
        assertEquals("vector-model", result.model);
        assertNull(result.configuration);
        assertNotNull(result);
    }

    @Test
    void getEmbeddingProviderRejectsClassThatDoesNotImplementEmbeddingProvider() {
        // Arrange

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getEmbeddingProvider(
                        "org.machanism.machai.ai.provider.impl.NoneProvider:model", null));

        // Assert
        assertEquals("Class `org.machanism.machai.ai.provider.impl.NoneProvider` does not implement EmbeddingProvider. Please ensure the class is a valid provider implementation.",
                exception.getMessage());
    }

    @Test
    void getEmbeddingProviderReportsMissingProviderClass() {
        // Arrange

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getEmbeddingProvider("missing.provider.Class:model", null));

        // Assert
        assertEquals("Failed to initialize EmbeddingProvider provider 'missing.provider.Class': provider is not supported or an error occurred during initialization.",
                exception.getMessage());
    }

    /** Minimal fixture used to exercise fully qualified reflective loading. */
    public static class FullyQualifiedEmbeddingProvider implements EmbeddingProvider {
        private String model;
        private Configurator configuration;

        @Override
        public void init(String model, Configurator conf) {
            this.model = model;
            this.configuration = conf;
        }

        @Override
        public List<Double> embedding(String text, long dimensions) {
            return Collections.emptyList();
        }
    }

    /** Fixture used for reflective provider tests. */
    public static class FallbackProvider implements Genai {
        private String model;
        private Configurator configuration;

        @Override
        public void init(String model, Configurator conf) {
            this.model = model;
            this.configuration = conf;
        }

        @Override
        public void prompt(String text) {
        }

        @Override
        public void clear() {
        }

        @Override
        public void instructions(String instructions) {
        }

        @Override
        public String perform() {
            return null;
        }

        @Override
        public void addTools(FunctionTools tools, String[] enabledTools) {
        }

        @Override
        public void addPrompts(FunctionTools tools) {
        }

        @Override
        public void addResources(FunctionTools tools) {
        }

        @Override
        public void setProjectDir(File projectDir) {
        }

        @Override
        public void setErrorHandling(boolean errorHandling) {
        }
    }

}
