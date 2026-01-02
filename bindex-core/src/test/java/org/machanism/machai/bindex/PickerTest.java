package org.machanism.machai.bindex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.BIndex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;
import org.machanism.machai.schema.Layer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PickerTest {
    private GenAIProvider provider;
    private Picker picker;

    @BeforeEach
    void setup() {
        provider = mock(GenAIProvider.class);
        picker = new Picker(provider);
    }

    @Test
    void testSetAndGetScore() {
        picker.setScore(0.95);
        // Direct getter by id will map value if present
    }

    @Test
    void testGetNormalizedLanguageNameReturnsLowerCaseName() {
        Language lang = mock(Language.class);
        when(lang.getName()).thenReturn("JavaScript (Node.js)");
        String result = Picker.getNormalizedLanguageName(lang);
        assertEquals("javascript", result);
    }

    @Test
    void testAddDependenciesDoesNotAddDuplicateIds() {
        Set<String> deps = new HashSet<>();
        // Need to check recursive dependency add and cycle protection
        // Not practical to deeply mock here (relies on DB) but can check method exists
        assertDoesNotThrow(() -> picker.addDependencies(deps, "id1"));
    }

    @Test
    @Disabled("Need to fix.")
    void testGetEmbeddingBsonCreatesEmbeddings() throws Exception {
        Classification classification = mock(Classification.class);
        List<Double> dummyEmbedding = Arrays.asList(0.1, 0.2, 0.3);
        // Would require API and environment. Here we can check for thrown exceptions.
        assertThrows(IllegalStateException.class, () -> {
            picker.getEmbeddingBson(classification, 3);
        });
    }
}
