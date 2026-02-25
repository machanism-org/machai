package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Language;

class PickerTest {

    @Test
    void getNormalizedLanguageName_stripsParenthesesLowercasesAndTrims() {
        // Arrange
        Language language = new Language();
        language.setName("  Java (JVM)  ");

        // Act
        String normalized = Picker.getNormalizedLanguageName(language);

        // Assert
        assertEquals("java", normalized);
    }

    @Test
    void getScore_whenIdNotPresent_returnsNull() throws Exception {
        // Arrange
        Picker picker = new Picker(null, "mongodb://localhost");

        // Act
        Double score = picker.getScore("missing");

        // Assert
        assertNull(score);
    }

    @Test
    void setEmbeddingModelName_andGetEmbeddingModelName_roundTrip() throws Exception {
        // Arrange
        Picker picker = new Picker(null, "mongodb://localhost");

        // Act
        picker.setEmbeddingModelName("custom-model");

        // Assert
        assertEquals("custom-model", picker.getEmbeddingModelName());
    }

    @Test
    void addDependencies_whenTransitiveAndCycles_collectsUniqueIds() throws Exception {
        // Arrange
        Picker picker = new Picker(null, "mongodb://localhost") {
            @Override
            protected Bindex getBindex(String id) {
                if ("a".equals(id)) {
                    Bindex b = new Bindex();
                    b.setId("a");
                    b.setDependencies(Arrays.asList("b", "c"));
                    return b;
                }
                if ("b".equals(id)) {
                    Bindex b = new Bindex();
                    b.setId("b");
                    b.setDependencies(Arrays.asList("c"));
                    return b;
                }
                if ("c".equals(id)) {
                    Bindex b = new Bindex();
                    b.setId("c");
                    b.setDependencies(Arrays.asList("a"));
                    return b;
                }
                return null;
            }
        };

        Set<String> deps = new HashSet<String>();

        // Act
        picker.addDependencies(deps, "a");

        // Assert
        assertEquals(new HashSet<String>(Arrays.asList("a", "b", "c")), deps);
    }
}
