package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.machanism.machai.bindex.core.BindexInfo;

/** Unit tests for the mutable Bindex summary value object. */
class BindexInfoTest {

    @Test
    void newInfoHasNullTextFieldsAndZeroScore() {
        // Arrange / Act
        BindexInfo info = new BindexInfo();

        // Assert
        assertNull(info.getId());
        assertNull(info.getVersion());
        assertNull(info.getDescription());
        assertEquals(0.0, info.getScore());
    }

    @Test
    void settersExposeAllAssignedValues() {
        // Arrange
        BindexInfo info = new BindexInfo();

        // Act
        info.setId("group:artifact");
        info.setVersion("2.1.0");
        info.setDescription("A useful library");
        info.setScore(0.875);

        // Assert
        assertEquals("group:artifact", info.getId());
        assertEquals("2.1.0", info.getVersion());
        assertEquals("A useful library", info.getDescription());
        assertEquals(0.875, info.getScore());
    }

    @Test
    void settersPermitNullAndSpecialNumericValues() {
        // Arrange
        BindexInfo info = new BindexInfo();

        // Act
        info.setId(null);
        info.setVersion(null);
        info.setDescription(null);
        info.setScore(Double.NaN);

        // Assert
        assertNull(info.getId());
        assertNull(info.getVersion());
        assertNull(info.getDescription());
        assertEquals(Double.NaN, info.getScore());
    }
}
