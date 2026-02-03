package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BindexRegisterTest {

    @Test
    void update_setsFlagAndReturnsSameInstance() {
        // Arrange
        BindexRegister register = Mockito.mock(BindexRegister.class, Mockito.CALLS_REAL_METHODS);

        // Act
        BindexRegister returned = register.update(true);

        // Assert
        assertSame(register, returned);
    }
}
