

package org.machanism.machai.ai.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import org.machanism.machai.ai.manager.GenAIProvider;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;


class FunctionToolsTest {

    @Mock
    private Configurator configurator;
    @InjectMocks
    private FunctionTools functionTools = new FunctionTools() {
        @Override
        public void applyTools(GenAIProvider provider) {
            // no-op for test
        }
    };

    @BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this);
}

    @Test
    void replace_shouldResolveNestedPlaceholders() {
        // TestMate-55a2b88fb4ed6efca07756159e2832e0
        // Arrange
        String input = "${base_url}/api";
        when(configurator.get("base_url")).thenReturn("https://${host}");
        when(configurator.get("host")).thenReturn("localhost");
        // Act
        String result = functionTools.replace(input, configurator);
        // Assert
        assertEquals("https://localhost/api", result);
    }
    @Test
    void replace_shouldStopAtIterationLimitToPreventInfiniteRecursion() {
        // TestMate-60242c09ed9bc5aec3927f0ea5ef9ba0
        // Arrange
        String input = "${A}";
        when(configurator.get("A")).thenReturn("${B}");
        when(configurator.get("B")).thenReturn("${A}");
        // Act
        String result = functionTools.replace(input, configurator);
        // Assert
        assertEquals("${A}", result);
        verify(configurator, times(5)).get("A");
        verify(configurator, times(5)).get("B");
    }
    @Test
    void replace_shouldHandleSpecialCharactersInPropertyNames() {
        // TestMate-d723baee6dfaf6613b74cd5e3b5b7e97
        // Arrange
        String input = "${app.version} - ${service_name} (${env-id})";
        when(configurator.get("app.version")).thenReturn("1.0.0");
        when(configurator.get("service_name")).thenReturn("auth-service");
        when(configurator.get("env-id")).thenReturn("prod-01");
        // Act
        String result = functionTools.replace(input, configurator);
        // Assert
        assertEquals("1.0.0 - auth-service (prod-01)", result);
    }
    @Test
    void replace_shouldReturnUnchangedValue_whenNoPlaceholdersPresent() {
        // TestMate-eee981617e30dc4651501a7413700578
        // Arrange
        String input = "Hello World without placeholders";
        // Act
        String result = functionTools.replace(input, configurator);
        // Assert
        assertEquals(input, result);
        verifyNoInteractions(configurator);
    }

    @Test
void replace_shouldHandleMalformedOrEmptyPlaceholdersGracefully() {
    // TestMate-26f80aae9520c7d71bab527b3aead8a2
    // Arrange
    String emptyPlaceholder = "Value is ${}";
    String unclosedPlaceholder = "Value is ${unclosed";
    String missingEnd = "${missing_end";
    // Act
    String resultEmpty = functionTools.replace(emptyPlaceholder, configurator);
    String resultUnclosed = functionTools.replace(unclosedPlaceholder, configurator);
    String resultMissingEnd = functionTools.replace(missingEnd, configurator);
    // Assert
    assertEquals(emptyPlaceholder, resultEmpty);
    assertEquals(unclosedPlaceholder, resultUnclosed);
    assertEquals(missingEnd, resultMissingEnd);
    verifyNoInteractions(configurator);
}
}
