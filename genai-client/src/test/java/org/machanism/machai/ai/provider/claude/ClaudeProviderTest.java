package org.machanism.machai.ai.provider.claude;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Collections;
import java.util.ListResourceBundle;

import org.junit.jupiter.api.Test;

class ClaudeProviderTest {

    @Test
    void unsupportedOperationsShouldThrowConsistentException() {
        ClaudeProvider provider = new ClaudeProvider();
        String expectedMessage = "ClaudeProvider is not implemented yet.";

        assertEquals(expectedMessage, assertThrows(UnsupportedOperationException.class, () -> provider.init(null)).getMessage());
        assertEquals(expectedMessage, assertThrows(UnsupportedOperationException.class, () -> provider.prompt("text")).getMessage());
        assertEquals(expectedMessage, assertThrows(UnsupportedOperationException.class, provider::perform).getMessage());
        assertEquals(expectedMessage, assertThrows(UnsupportedOperationException.class, provider::clear).getMessage());
        assertEquals(expectedMessage,
                assertThrows(UnsupportedOperationException.class, () -> provider.addTool("name", "description", null, "param")).getMessage());
        assertEquals(expectedMessage, assertThrows(UnsupportedOperationException.class, () -> provider.instructions("system")).getMessage());
        assertEquals(expectedMessage,
                assertThrows(UnsupportedOperationException.class, () -> provider.promptBundle(new ListResourceBundle() {
                    @Override
                    protected Object[][] getContents() {
                        return new Object[][] { { "key", "value" } };
                    }
                })).getMessage());
        assertEquals(expectedMessage,
                assertThrows(UnsupportedOperationException.class, () -> provider.inputsLog(new File("target/claude-inputs.txt"))).getMessage());
        assertEquals(expectedMessage,
                assertThrows(UnsupportedOperationException.class, () -> provider.setWorkingDir(new File("target"))).getMessage());
        assertEquals(expectedMessage, assertThrows(UnsupportedOperationException.class, provider::usage).getMessage());
    }

    @Test
    void embeddingShouldReturnEmptyList() {
        ClaudeProvider provider = new ClaudeProvider();

        assertEquals(Collections.emptyList(), provider.embedding("text", 4));
    }
}
