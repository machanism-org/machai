package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.Test;

/** Unit tests for the deliberately inert provider. */
class NoneProviderTest {
    @Test
    void nonLoggingProviderAcceptsEveryOperationAndReturnsNull() {
        // Arrange
        NoneProvider provider = new NoneProvider();

        // Act and assert
        assertDoesNotThrow(() -> {
            provider.init("disabled", TestConfigurators.mapBacked());
            provider.prompt("prompt");
            provider.instructions("instructions");
            provider.clear();
            provider.addTools(null, null);
            provider.addPrompts(null);
            provider.addResources(null);
            provider.setProjectDir(new File("."));
            provider.setErrorHandling(true);
        });
        assertNull(provider.perform());
    }

    @Test
    void loggingModeAlsoReturnsNullAfterLoggingCalls() {
        // Arrange
        NoneProvider provider = new NoneProvider();

        // Act
        provider.init("log", TestConfigurators.mapBacked());

        // Assert
        assertDoesNotThrow(() -> {
            provider.prompt("prompt");
            provider.instructions("instructions");
            provider.clear();
            provider.perform();
            provider.addTools(null, null);
            provider.setProjectDir(null);
            provider.setErrorHandling(false);
        });
        assertNull(provider.perform());
    }
}
