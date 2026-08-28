package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CommandSpecFunctionToolsTest {

    @Test
    void terminateExecutionShouldAlwaysThrowWithMessageAndExitCode() {
        // Arrange
        CommandSpecFunctionTools tools = new CommandSpecFunctionTools();

        // Act
        ProcessTerminationException exception = assertThrows(ProcessTerminationException.class,
                () -> tools.terminateExecution("stop now", 17, null));

        // Assert
        assertEquals("stop now", exception.getMessage());
        assertEquals(17, exception.getExitCode());
    }

    @Test
    void endTaskShouldAlwaysThrowWithProvidedMessage() {
        // Arrange
        CommandSpecFunctionTools tools = new CommandSpecFunctionTools();

        // Act
        EndTaskException exception = assertThrows(EndTaskException.class,
                () -> tools.endTask("finished"));

        // Assert
        assertEquals("finished", exception.getMessage());
    }

    @Test
    void terminationExceptionCauseConstructorShouldPreserveCauseAndCode() {
        // Arrange
        Throwable cause = new IllegalStateException("underlying");

        // Act
        ProcessTerminationException exception = new ProcessTerminationException("failed", cause, 3);

        // Assert
        assertEquals("failed", exception.getMessage());
        assertSame(cause, exception.getCause());
        assertEquals(3, exception.getExitCode());
    }

    @Test
    void terminationExceptionCodeOnlyConstructorShouldHaveNoMessageOrCause() {
        // Arrange and Act
        ProcessTerminationException exception = new ProcessTerminationException(9);

        // Assert
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
        assertEquals(9, exception.getExitCode());
    }
}
