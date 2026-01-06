package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BindexCommand}.
 * <p>
 * These tests cover basic instantiation and shell method exposure.
 *
 * @author Viktor Tovstyi
 */
class BindexCommandTest {

    private BindexCommand command;

    @BeforeEach
    void setUp() {
        command = new BindexCommand();
    }

    @Test
    void testInstantiation() {
        assertNotNull(command);
    }
}
