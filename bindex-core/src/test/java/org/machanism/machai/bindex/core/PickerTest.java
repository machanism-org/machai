package org.machanism.machai.bindex.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;
import org.mockito.MockedStatic;

/** Package-level tests for dependency traversal and language normalization. */
class PickerTest {

    @Test
    void normalizedLanguageNameIsLowercaseAndDropsParentheticalVersion() {
        // Arrange
        Language language = new Language();
        language.setName("  Java (JDK 21) ");

        // Act
        String normalized = Picker.getNormalizedLanguageName(language);

        // Assert
        assertEquals("java", normalized);
    }

    @Test
    void pickClassifiesEmbedsAndHydratesMissingDescriptions() throws Exception {
        // Arrange
        BindexRepository repository = mock(BindexRepository.class);
        Configurator configurator = mock(Configurator.class);
        Genai genai = mock(Genai.class);
        EmbeddingProvider embeddings = mock(EmbeddingProvider.class);
        BindexInfo withoutDescription = new BindexInfo();
        withoutDescription.setId("library-id");
        Bindex fullBindex = new Bindex();
        fullBindex.setDescription("hydrated description");
        when(configurator.get(org.mockito.ArgumentMatchers.eq("picker.classificationInstruction"), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("Schema: %s Request: %s");
        when(configurator.get("pick.model", null)).thenReturn("chat-model");
        when(configurator.get("embedding.model")).thenReturn("embedding-model");
        when(genai.perform()).thenReturn("```json\n[{\"type\":\"library\"}]\n```");
        when(embeddings.embedding(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(700)))
                .thenReturn(List.of(0.3));
        when(repository.find(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyDouble(),
                org.mockito.ArgumentMatchers.same(configurator))).thenReturn(List.of(withoutDescription));
        when(repository.getBindex("library-id")).thenReturn(fullBindex);
        Picker picker = new Picker(repository, configurator);

        try (MockedStatic<GenaiProviderManager> providers = org.mockito.Mockito.mockStatic(GenaiProviderManager.class)) {
            providers.when(() -> GenaiProviderManager.getProvider("chat-model", configurator)).thenReturn(genai);
            providers.when(() -> GenaiProviderManager.getEmbeddingProvider("embedding-model", configurator))
                    .thenReturn(embeddings);

            // Act
            var picked = picker.pick("Need a library", 3, 0.8, configurator);

            // Assert
            assertEquals(List.of(withoutDescription), List.copyOf(picked));
            assertEquals("hydrated description", withoutDescription.getDescription());
            org.mockito.Mockito.verify(genai).prompt(org.mockito.ArgumentMatchers.contains("Need a library"));
        }
    }

    @Test
    void pickRejectsMissingModelBeforeContactingProvider() {
        // Arrange
        Configurator configurator = mock(Configurator.class);
        when(configurator.get(org.mockito.ArgumentMatchers.eq("picker.classificationInstruction"),
                org.mockito.ArgumentMatchers.anyString())).thenReturn("Schema: %s Request: %s");
        when(configurator.get("pick.model", null)).thenReturn(null);
        when(configurator.get("gw.model")).thenReturn(null);
        Picker picker = new Picker(mock(BindexRepository.class), configurator);

        // Act / Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> picker.pick("request", 1, 0.5, configurator));
        assertEquals("genai must not be null", exception.getMessage());
    }

    @Test
    void addDependenciesTraversesGraphOnlyOnceAndIgnoresMissingNodes() {
        // Arrange
        BindexRepository repository = mock(BindexRepository.class);
        Bindex root = bindex("root", List.of("child", "missing"));
        Bindex child = bindex("child", List.of("root"));
        when(repository.getBindex("root")).thenReturn(root);
        when(repository.getBindex("child")).thenReturn(child);
        when(repository.getBindex("missing")).thenReturn(null);
        Picker picker = new Picker(repository, null);
        Set<String> dependencies = new HashSet<>();

        // Act
        picker.addDependencies(dependencies, "root");

        // Assert
        assertEquals(Set.of("root", "child"), dependencies);
    }

    @Test
    void addDependenciesLeavesSetUnchangedForMissingRoot() {
        // Arrange
        BindexRepository repository = mock(BindexRepository.class);
        when(repository.getBindex("unknown")).thenReturn(null);
        Picker picker = new Picker(repository, null);
        Set<String> dependencies = new HashSet<>();

        // Act
        picker.addDependencies(dependencies, "unknown");

        // Assert
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void saveGeneratesEmbeddingAndDelegatesToRepository() {
        // Arrange
        BindexRepository repository = mock(BindexRepository.class);
        Configurator configurator = mock(Configurator.class);
        EmbeddingProvider embeddingProvider = mock(EmbeddingProvider.class);
        Classification classification = new Classification();
        classification.setType("library");
        Bindex bindex = bindex("saved", List.of());
        bindex.setClassification(classification);
        when(configurator.get("embedding.model")).thenReturn("embedding-model");
        when(embeddingProvider.embedding("{\"type\":\"library\",\"domains\":[],\"languages\":[],\"layers\":[],\"usageContext\":[],\"targetEnvironment\":[],\"integrations\":[]}", 700))
                .thenReturn(List.of(0.1, 0.2));
        when(repository.save(bindex, List.of(0.1, 0.2))).thenReturn("database-id");
        Picker picker = new Picker(repository, configurator);

        try (MockedStatic<GenaiProviderManager> providers = org.mockito.Mockito.mockStatic(GenaiProviderManager.class)) {
            providers.when(() -> GenaiProviderManager.getEmbeddingProvider("embedding-model", configurator))
                    .thenReturn(embeddingProvider);

            // Act
            String result = picker.save(bindex);

            // Assert
            assertEquals("saved", result);
        }
    }

    private static Bindex bindex(String id, List<String> dependencies) {
        Bindex value = new Bindex();
        value.setId(id);
        value.setDependencies(dependencies);
        return value;
    }
}
