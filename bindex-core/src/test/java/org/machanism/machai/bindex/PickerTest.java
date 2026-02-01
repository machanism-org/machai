package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
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
}
