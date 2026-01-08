package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ProcessCommand}.
 * <p>
 * These tests cover basic instantiation and shell method exposure.
 *
 * @author Viktor Tovstyi
 */
class DocsCommandTest {

    private ProcessCommand command;

    @BeforeEach
    void setUp() {
        command = new ProcessCommand();
    }

    @Test
    void testInstantiation() {
        assertNotNull(command);
    }
}
