package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.provider.GenaiAdapter;

/** Verifies every adapter operation is delegated to its provider. */
class GenaiAdapterTest {
    @Test
    void allOperationsDelegateAndNullProviderIsRejected() {
        // Arrange
        Genai delegate = mock(Genai.class);
        when(delegate.perform()).thenReturn("answer");
        GenaiAdapter adapter = new GenaiAdapter();
        File directory = new File(".");

        // Act
        adapter.setProvider(delegate);
        adapter.init("model", TestConfigurators.mapBacked());
        adapter.prompt("p");
        adapter.clear();
        adapter.instructions("i");
        adapter.setProjectDir(directory);
        adapter.addTools(null);
        adapter.addPrompts(null);
        adapter.addResources(null);
        adapter.setErrorHandling(true);
        adapter.setEnabledTools(new String[] { "x" });

        // Assert
        assertEquals("answer", adapter.perform());
        verify(delegate).init(eq("model"), any());
        verify(delegate).prompt("p");
        verify(delegate).clear();
        verify(delegate).instructions("i");
        verify(delegate).setProjectDir(directory);
        verify(delegate).addTools(null);
        verify(delegate, times(2)).addPrompts(null);
        // addResources delegates to addPrompts for legacy compatibility.
        verify(delegate).setErrorHandling(true);
        verify(delegate).setEnabledTools(new String[] { "x" });
        assertThrows(IllegalArgumentException.class, () -> adapter.setProvider(null));
    }
}
