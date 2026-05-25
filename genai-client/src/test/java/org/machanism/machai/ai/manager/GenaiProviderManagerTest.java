package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.ToolFunction;

class GenaiProviderManagerTest {

    @Test
    void getProviderReturnsNullWhenProviderNameIsBlank() {
        // Arrange
        Configurator configurator = null;

        // Act
        Genai provider = GenaiProviderManager.getProvider("   :model", configurator);

        // Assert
        assertNull(provider);
    }

    @Test
    void getProviderInitializesClassDerivedFromSimpleProviderName() {
        // Arrange
        Configurator configurator = null;

        // Act
        Genai provider = GenaiProviderManager.getProvider("Testgenai:test-model", configurator);

        // Assert
        assertNotNull(provider);
        assertEquals(TestgenaiProvider.class, provider.getClass());
        assertEquals("test-model", TestgenaiProvider.lastInitializedModel);
        assertSame(configurator, TestgenaiProvider.lastConfigurator);
    }

    @Test
    void getProviderInitializesProviderUsingFullyQualifiedClassName() {
        // Arrange
        Configurator configurator = null;
        String providerClassName = TestgenaiProvider.class.getName();

        // Act
        Genai provider = GenaiProviderManager.getProvider(providerClassName + ":fq-model", configurator);

        // Assert
        assertNotNull(provider);
        assertEquals(TestgenaiProvider.class, provider.getClass());
        assertEquals("fq-model", TestgenaiProvider.lastInitializedModel);
        assertSame(configurator, TestgenaiProvider.lastConfigurator);
    }

    @Test
    void getProviderShouldRejectUnknownProvider() {
        // Arrange
        Configurator configurator = null;

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getProvider("MissingProvider:model", configurator));

        // Assert
        assertEquals(
                "Failed to initialize GenAI provider 'MissingProvider': provider is not supported or an error occurred during initialization.",
                ex.getMessage());
    }

    @Test
    void getProviderWithoutColonShouldTreatWholeValueAsProviderName() {
        // Arrange
        Configurator configurator = null;

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getProvider("standalone-model", configurator));

        // Assert
        assertEquals(
                "Failed to initialize GenAI provider 'standalone-model': provider is not supported or an error occurred during initialization.",
                ex.getMessage());
    }

    @Test
    void getEmbeddingProviderReturnsNullWhenProviderNameIsBlank() {
        // Arrange
        Configurator configurator = null;

        // Act
        EmbeddingProvider provider = GenaiProviderManager.getEmbeddingProvider("   :embedding-model", configurator);

        // Assert
        assertNull(provider);
    }

    @Test
    void getEmbeddingProviderInitializesClassDerivedFromSimpleProviderName() {
        // Arrange
        Configurator configurator = null;

        // Act
        EmbeddingProvider provider = GenaiProviderManager.getEmbeddingProvider("Testembedding:vector-model", configurator);

        // Assert
        assertNotNull(provider);
        assertEquals(TestembeddingProvider.class, provider.getClass());
        assertEquals("vector-model", TestembeddingProvider.lastInitializedModel);
        assertSame(configurator, TestembeddingProvider.lastConfigurator);
    }

    @Test
    void getEmbeddingProviderInitializesProviderUsingFullyQualifiedClassName() {
        // Arrange
        Configurator configurator = null;
        String providerClassName = TestembeddingProvider.class.getName();

        // Act
        EmbeddingProvider provider = GenaiProviderManager.getEmbeddingProvider(providerClassName + ":embedding-fq",
                configurator);

        // Assert
        assertNotNull(provider);
        assertEquals(TestembeddingProvider.class, provider.getClass());
        assertEquals("embedding-fq", TestembeddingProvider.lastInitializedModel);
        assertSame(configurator, TestembeddingProvider.lastConfigurator);
    }

    @Test
    void getEmbeddingProviderRejectsClassesThatDoNotImplementEmbeddingProvider() {
        // Arrange
        Configurator configurator = null;

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getEmbeddingProvider(TestgenaiProvider.class.getName() + ":model",
                        configurator));

        // Assert
        assertEquals(
                "Class `" + TestgenaiProvider.class.getName()
                        + "` does not implement EmbeddingProvider. Please ensure the class is a valid provider implementation.",
                ex.getMessage());
    }

    @Test
    void getEmbeddingProviderRejectsUnknownProvider() {
        // Arrange
        Configurator configurator = null;

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getEmbeddingProvider("MissingEmbedding:model", configurator));

        // Assert
        assertEquals(
                "Failed to initialize EmbeddingProvider provider 'MissingEmbedding': provider is not supported or an error occurred during initialization.",
                ex.getMessage());
    }

    public static class TestgenaiProvider implements GenaiProviderManagerTestMarker, Genai {

        static String lastInitializedModel;
        static Configurator lastConfigurator;

        @Override
        public void init(String model, Configurator conf) {
            lastInitializedModel = model;
            lastConfigurator = conf;
        }

        @Override
        public void prompt(String text) {
        }

        @Override
        public void clear() {
        }

        @Override
        public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
        }

        @Override
        public void instructions(String instructions) {
        }

        @Override
        public String perform() {
            return null;
        }

        @Override
        public void inputsLog(File bindexTempDir) {
        }

        @Override
        public void setWorkingDir(File workingDir) {
        }

        @Override
        public Usage usage() {
            return new Usage(0, 0, 0);
        }
    }

    public static class TestembeddingProvider implements GenaiProviderManagerTestMarker, EmbeddingProvider {

        static String lastInitializedModel;
        static Configurator lastConfigurator;

        @Override
        public void init(String model, Configurator conf) {
            lastInitializedModel = model;
            lastConfigurator = conf;
        }

        @Override
        public List<Double> embedding(String text, long dimensions) {
            return Collections.emptyList();
        }
    }

    public interface GenaiProviderManagerTestMarker {
        // marker for reflective loading visibility in tests
    }
}
