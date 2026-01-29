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
    void getScore_returnsNullWhenNoScoreWasRecorded() throws Exception {
        // Arrange
        Picker picker = TestPickers.newPickerWithoutMongo();

        // Act
        Double score = picker.getScore("any");

        // Assert
        assertNull(score);
    }

    @Test
    void setEmbeddingModelName_roundTripsViaGetter() throws Exception {
        // Arrange
        Picker picker = TestPickers.newPickerWithoutMongo();

        // Act
        picker.setEmbeddingModelName("model-x");

        // Assert
        assertEquals("model-x", picker.getEmbeddingModelName());
    }
}
