package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests persisted command-log access and process collection without starting OS commands. */
class CommandFunctionToolsLogOperationsTest {

    @Test
    void getPreviousLogChunkReturnsRequestedPrecedingCharacters() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();
        String id = "chunk-" + System.nanoTime();
        Path log = LogBuilder.getCommandLogPath("command", id);
        Files.write(log, "abcdefghij".getBytes(StandardCharsets.UTF_8));

        // Act
        String result = (String) tools.getPreviousLogChunk(id, 4, 8, "UTF-8");

        // Assert
        assertEquals("efgh", result);
    }

    @Test
    void getPreviousLogChunkReturnsEmptyWhenWindowHasNoWidth() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();
        String id = "empty-" + System.nanoTime();
        Files.write(LogBuilder.getCommandLogPath("command", id), "content".getBytes(StandardCharsets.UTF_8));

        // Act
        Object result = tools.getPreviousLogChunk(id, 10, 0, "UTF-8");

        // Assert
        assertEquals("", result);
    }

    @Test
    void logOperationsRejectUnknownLogId() {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();
        String missing = "missing-" + System.nanoTime();

        // Act / Assert
        assertThrows(FileNotFoundException.class, () -> tools.getPreviousLogChunk(missing, 1, 1, "UTF-8"));
        assertThrows(FileNotFoundException.class, () -> tools.getLogMatches(missing, ".*", "UTF-8"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getLogMatchesReturnsTextOffsetsAndOneBasedLineNumbers() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();
        String id = "matches-" + System.nanoTime();
        Files.write(LogBuilder.getCommandLogPath("command", id), "one 12\ntwo 345".getBytes(StandardCharsets.UTF_8));

        // Act
        List<Map<String, Object>> matches = (List<Map<String, Object>>) tools.getLogMatches(id, "\\d+", "UTF-8");

        // Assert
        assertEquals(2, matches.size());
        assertEquals("12", matches.get(0).get("text"));
        assertEquals(4, matches.get(0).get("start"));
        assertEquals(1, matches.get(0).get("line"));
        assertEquals("345", matches.get(1).get("text"));
        assertEquals(2, matches.get(1).get("line"));
    }

    @Test
    void waitAndCollectDestroysTimedOutProcessAndRecordsTimeout() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();
        java.lang.reflect.Field timeout = CommandFunctionTools.class.getDeclaredField("processTimeoutSeconds");
        timeout.setAccessible(true);
        timeout.setInt(tools, 0);
        Process process = mock(Process.class);
        when(process.waitFor(0, TimeUnit.SECONDS)).thenReturn(false);
        LogBuilder output = new LogBuilder("command", 200, null, null);

        // Act
        Map<String, Object> report = tools.waitAndCollect(process, output, "timeout-test");

        // Assert
        verify(process).destroyForcibly();
        assertTrue(((String) report.get("tail")).contains("Command timed out after 0 seconds."));
    }

    @Test
    void waitAndCollectLeavesCompletedProcessUntouched() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();
        Process process = mock(Process.class);
        when(process.waitFor(Mockito.anyLong(), Mockito.eq(TimeUnit.SECONDS))).thenReturn(true);
        LogBuilder output = new LogBuilder("command", 20, null, null);
        output.append("done");

        // Act
        Map<String, Object> report = tools.waitAndCollect(process, output, "done-test");

        // Assert
        assertEquals("done", report.get("tail"));
        verify(process, Mockito.never()).destroyForcibly();
    }
}
