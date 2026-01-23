package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.machanism.machai.schema.Language;

class PickerTest {

    @Test
    void getNormalizedLanguageName_stripsParenthesesAndLowercases() {
        // Arrange
        Language language = new Language();
        language.setName("Java (JVM) ");

        // Act
        String normalized = Picker.getNormalizedLanguageName(language);

        // Assert
        assertEquals("java", normalized);
    }

    @Test
    void getScore_returnsNullWhenNoScoreWasRecorded() {
        // Arrange
        Picker picker = new Picker(null, "mongodb://localhost:27017");
        try {
            // Act
            Double score = picker.getScore("any");

            // Assert
            assertNull(score);
        } finally {
            try {
                picker.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    void setEmbeddingModelName_roundTripsViaGetter() {
        // Arrange
        Picker picker = new Picker(null, "mongodb://localhost:27017");
        try {
            // Act
            picker.setEmbeddingModelName("model-x");

            // Assert
            assertEquals("model-x", picker.getEmbeddingModelName());
        } finally {
            try {
                picker.close();
            } catch (Exception ignored) {
            }
        }
    }
}
