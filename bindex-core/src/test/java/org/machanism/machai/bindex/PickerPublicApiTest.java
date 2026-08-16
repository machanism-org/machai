package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.machanism.machai.bindex.core.BindexRepository;
import org.machanism.machai.bindex.core.Picker;
import org.machanism.machai.schema.Bindex;

/** Tests the public behaviour of the core picker without contacting external services. */
class PickerPublicApiTest {

    @Test
    void saveRejectsBindexWithoutClassificationBeforeRepositoryAccess() {
        // Arrange
        BindexRepository repository = mock(BindexRepository.class);
        Picker picker = new Picker(repository, null);
        Bindex bindex = new Bindex();
        bindex.setId("example:library");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> picker.save(bindex));

        // Assert
        assertEquals("classification must not be null", exception.getMessage());
    }
}
