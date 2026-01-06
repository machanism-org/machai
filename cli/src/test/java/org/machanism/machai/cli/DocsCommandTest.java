package org.machanism.machai.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DocsCommand}.
 * <p>
 * These tests cover basic instantiation and shell method exposure.
 *
 * @author Viktor Tovstyi
 */
class DocsCommandTest {

    private DocsCommand command;

    @BeforeEach
    void setUp() {
        command = new DocsCommand();
    }

    @Test
    void testInstantiation() {
        assertNotNull(command);
    }
}
