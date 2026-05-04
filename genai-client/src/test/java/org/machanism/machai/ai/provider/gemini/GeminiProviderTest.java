package org.machanism.machai.ai.provider.gemini;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.ListResourceBundle;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;

class GeminiProviderTest {

    @Test
    void placeholderMethodsShouldEitherDoNothingOrReturnNull() {
        GeminiProvider provider = new GeminiProvider();

        assertDoesNotThrow(() -> provider.prompt("text"));
        assertDoesNotThrow(provider::clear);
        assertDoesNotThrow(() -> provider.addTool("name", "description", null, "required value"));
        assertDoesNotThrow(() -> provider.instructions("system"));
        assertDoesNotThrow(() -> provider.inputsLog(new File("target/gemini-inputs.txt")));
        assertDoesNotThrow(() -> provider.setWorkingDir(new File("target")));
        assertDoesNotThrow(() -> provider.promptBundle(new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] { { "key", "value" } };
            }
        }));
        assertNull(provider.usage());
    }

    @Test
    void notImplementedOperationsShouldThrow() {
        GeminiProvider provider = new GeminiProvider();

        assertThrows(NotImplementedException.class, () -> provider.init(null));
        assertThrows(NotImplementedException.class, () -> provider.embedding("text", 3));
        assertThrows(NotImplementedException.class, provider::perform);
    }
}
