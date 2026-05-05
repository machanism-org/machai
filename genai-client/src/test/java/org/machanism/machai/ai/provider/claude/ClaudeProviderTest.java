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

        assertEquals(expectedMessage, unsupportedMessage(() -> provider.init(null)));
        assertEquals(expectedMessage, unsupportedMessage(() -> provider.prompt("text")));
        assertEquals(expectedMessage, unsupportedMessage(provider::perform));
        assertEquals(expectedMessage, unsupportedMessage(provider::clear));
        assertEquals(expectedMessage,
                unsupportedMessage(() -> provider.addTool("name", "description", null, "param")));
        assertEquals(expectedMessage, unsupportedMessage(() -> provider.instructions("system")));
        assertEquals(expectedMessage, unsupportedMessage(() -> provider.promptBundle(new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] { { "key", "value" } };
            }
        })));
        assertEquals(expectedMessage,
                unsupportedMessage(() -> provider.inputsLog(new File("target/claude-inputs.txt"))));
        assertEquals(expectedMessage, unsupportedMessage(() -> provider.setWorkingDir(new File("target"))));
        assertEquals(expectedMessage, unsupportedMessage(provider::usage));
    }

    @Test
    void embeddingShouldReturnEmptyList() {
        ClaudeProvider provider = new ClaudeProvider();

        assertEquals(Collections.emptyList(), provider.embedding("text", 4));
    }

    private static String unsupportedMessage(ThrowingRunnable action) {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, action::run);
        return exception.getMessage();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }
}
