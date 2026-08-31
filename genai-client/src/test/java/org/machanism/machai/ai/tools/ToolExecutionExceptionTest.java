package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ToolExecutionExceptionTest {

    @Test
    void messageConstructor_preservesSuppliedMessage() {
        // Arrange
        String message = "Tool invocation failed";

        // Act
        ToolExecutionException exception = new ToolExecutionException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void causeConstructor_preservesSuppliedCauseAndItsMessage() {
        // Arrange
        IllegalStateException cause = new IllegalStateException("Remote tool is unavailable");

        // Act
        ToolExecutionException exception = new ToolExecutionException(cause);

        // Assert
        assertSame(cause, exception.getCause());
        assertEquals(cause.toString(), exception.getMessage());
    }
}
