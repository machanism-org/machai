package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Language;

class PickerTest {

    @Test
    @Disabled
    void constructorThrowsWhenUriNullAndOpenAiApiKeyMissing() {
        // Arrange
        GenAIProvider provider = mock(GenAIProvider.class);

        // Act + Assert
        assertThrows(IllegalStateException.class, () -> new Picker(provider, null));
    }

    @Test
    void getNormalizedLanguageNameLowerCasesTrimsAndRemovesParentheses() {
        // Arrange
        Language lang = mock(Language.class);
        when(lang.getName()).thenReturn("  JavaScript (Node.js)  ");

        // Act
        String normalized = Picker.getNormalizedLanguageName(lang);

        // Assert
        assertEquals("javascript", normalized);
    }

    @Test
    @Disabled
    void addDependenciesDoesNotLoopWhenStartIdAlreadyPresent() throws Exception {
        // Arrange
        Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017");
        Set<String> deps = new HashSet<>();
        deps.add("a");

        // Act + Assert
        assertDoesNotThrow(() -> picker.addDependencies(deps, "a"));
        assertEquals(Set.of("a"), deps);
    }

    @Test
    void getScoreReturnsValueFromInternalScoreMap() throws Exception {
        // Arrange
        Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017");

        Field f = Picker.class.getDeclaredField("scoreMap");
        f.setAccessible(true);
        Map<String, Double> map = new HashMap<>();
        map.put("id", 0.91);
        f.set(picker, map);

        // Act
        Double score = picker.getScore("id");

        // Assert
        assertEquals(0.91, score);
    }

    @Test
    void getEmbeddingBsonThrowsWhenOpenAiApiKeyNotConfiguredEvenWithExplicitUri() throws Exception {
        // Arrange
        Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017");

        Field apiKeyField = Picker.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(picker, null);

        Method method = Picker.class.getDeclaredMethod("getEmbeddingBson",
                org.machanism.machai.schema.Classification.class, Integer.class);
        method.setAccessible(true);

        org.machanism.machai.schema.Classification classification = new org.machanism.machai.schema.Classification();
        classification.setLanguages(List.of());
        classification.setLayers(List.of());
        classification.setDomains(List.of());
        classification.setIntegrations(List.of());

        // Act + Assert
        assertThrows(Exception.class, () -> method.invoke(picker, classification, 3));
    }

    @Test
    @Disabled
    void createThrowsNullPointerWhenClassificationMissing() throws Exception {
        // Arrange
        Picker picker = new Picker(mock(GenAIProvider.class), "mongodb://localhost:27017");
        Bindex bindex = new Bindex();
        bindex.setId("id");
        bindex.setName("name");
        bindex.setVersion("1");
        bindex.setClassification(null);

        // Act + Assert
        assertThrows(NullPointerException.class, () -> picker.create(bindex));
    }
}
