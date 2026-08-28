package org.machanism.machai.ai.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Verifies logger level-specific payload handling without relying on a backend. */
class ToolLoggerTest {
    private final Logger original = ToolLogger.logger;

    @AfterEach
    void restoreLogger() {
        ToolLogger.logger = original;
    }

    @Test
    void logsDebugInputAndResultWithCompletePayloads() throws Exception {
        // Arrange
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        ToolLogger.logger = logger;
        ToolLogger subject = new ToolLogger(ToolLogger.Type.TOOL, null);
        File directory = new File("work");

        // Act
        subject.logInput("read", new ObjectMapper().readTree("{\"key\":\"value\"}"), directory);
        subject.logResult("read", directory, "complete-result");

        // Assert
        verify(logger).debug(ToolLogger.CALL_MSG, ToolLogger.Type.TOOL, "read", "{\"key\":\"value\"}", directory);
        verify(logger).debug(ToolLogger.RETURNS_MSG, ToolLogger.Type.TOOL, "read", 15, "complete-result", directory);
    }

    @Test
    void debugErrorIncludesThrowable() {
        // Arrange
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        ToolLogger.logger = logger;
        RuntimeException failure = new RuntimeException("bad");

        // Act
        new ToolLogger(ToolLogger.Type.RESOURCE, null).logError("file", new File("."), failure);

        // Assert
        verify(logger).error(ToolLogger.ERROR_MSG, ToolLogger.Type.RESOURCE, "file", "failed: bad", new File("."), failure);
    }

}
