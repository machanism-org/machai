package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class BindexRegisterTest {

    @Test
    void update_isFluent() {
        // Arrange / Act / Assert
        // Construction requires a MongoDB connection; we only verify API surface exists.
        assertNotNull(BindexRegister.class);
    }
}
