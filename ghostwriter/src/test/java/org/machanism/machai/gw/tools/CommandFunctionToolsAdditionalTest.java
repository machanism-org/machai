package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CommandFunctionToolsAdditionalTest {

    @TempDir
    Path tempDir;

    @Test
    void resolveWorkingDirShouldHandleDotTraversalAbsoluteAndNullValues() throws Exception {
        CommandFunctionTools tools = new CommandFunctionTools();
        File projectDir = tempDir.toFile();

        File dotResult = tools.resolveWorkingDir(projectDir, ".");
        File childResult = tools.resolveWorkingDir(projectDir, "child");
        File traversalResult = tools.resolveWorkingDir(projectDir, "..");
        File absoluteResult = tools.resolveWorkingDir(projectDir, projectDir.getAbsolutePath());
        File nullResult = tools.resolveWorkingDir(null, ".");

        assertEquals(projectDir.getCanonicalFile(), dotResult);
        assertEquals(new File(projectDir, "child").getCanonicalFile(), childResult);
        assertEquals(null, traversalResult);
        assertEquals(null, absoluteResult);
        assertEquals(null, nullResult);
    }

    @Test
    void parseEnvShouldIgnoreInvalidAndKeepValidEntries() {
        CommandFunctionTools tools = new CommandFunctionTools();
        String env = "A=1\n#comment\nINVALID\n1BAD=x\nEMPTY=\nGOOD_NAME = spaced value \n";

        Map<String, String> result = tools.parseEnv(env);

        assertEquals(2, result.size());
        assertEquals("1", result.get("A"));
        assertEquals("spaced value", result.get("GOOD_NAME"));
    }

    @Test
    void parseEnvShouldReturnEmptyMapForNullAndEmptyInput() {
        CommandFunctionTools tools = new CommandFunctionTools();

        assertTrue(tools.parseEnv(null).isEmpty());
        assertTrue(tools.parseEnv("").isEmpty());
    }

    @Test
    void waitAndCollectShouldAppendExitCodeAndExposeReadStreamMethod() throws Exception {
        CommandFunctionTools tools = new CommandFunctionTools();
        LimitedStringBuilder output = new LimitedStringBuilder(512);
        Process quickProcess = new ProcessBuilder("cmd", "/c", "echo hello").start();
        quickProcess.waitFor();

        String quickResult = tools.waitAndCollect(quickProcess, CompletableFuture.completedFuture(null),
                CompletableFuture.completedFuture(null), output, "id-1");

        assertTrue(quickResult.contains("Command exited with code: 0"));

        Method method = CommandFunctionTools.class.getDeclaredMethod("readStream", java.io.InputStream.class, String.class,
                LimitedStringBuilder.class, Class.forName("org.machanism.machai.gw.tools.CommandFunctionTools$LineConsumer"),
                Class.forName("org.machanism.machai.gw.tools.CommandFunctionTools$ErrorConsumer"));
        assertNotNull(method);
    }

    @Test
    void waitAndCollectShouldRecordTimeoutForLongRunningProcess() throws Exception {
        CommandFunctionTools tools = new CommandFunctionTools();
        java.lang.reflect.Field timeout = CommandFunctionTools.class.getDeclaredField("processTimeoutSeconds");
        timeout.setAccessible(true);
        timeout.setInt(tools, 0);
        Process process = new ProcessBuilder("cmd", "/c", "ping -n 3 127.0.0.1 > nul").start();

        String result = tools.waitAndCollect(process, CompletableFuture.completedFuture(null),
                CompletableFuture.completedFuture(null), new LimitedStringBuilder(512), "timeout");

        assertTrue(result.contains("Command timed out after 0 seconds."));
        assertTrue(result.contains("Command exited with code:"));
    }

    @Test
    void terminateProcessShouldUseDefaultsAndCustomCause() {
        CommandFunctionTools tools = new CommandFunctionTools();
        ObjectMapper mapper = new ObjectMapper();

        CommandFunctionTools.ProcessTerminationException defaultException = assertThrows(
                CommandFunctionTools.ProcessTerminationException.class,
                () -> tools.terminateProcess(mapper.createObjectNode(), tempDir.toFile()));
        assertEquals("Process terminated by function tool.", defaultException.getMessage());
        assertEquals(1, defaultException.getExitCode());

        ObjectNode props = mapper.createObjectNode();
        props.put("message", "stop now");
        props.put("cause", "because");
        props.put("exitCode", 42);
        CommandFunctionTools.ProcessTerminationException customException = assertThrows(
                CommandFunctionTools.ProcessTerminationException.class,
                () -> tools.terminateProcess(props, tempDir.toFile()));
        assertEquals("stop now", customException.getMessage());
        assertEquals("because", customException.getCause().getMessage());
        assertEquals(42, customException.getExitCode());
    }

    @Test
    void commandTerminationExceptionShouldExposeExitCode() {
        CommandFunctionTools.ProcessTerminationException ex = new CommandFunctionTools.ProcessTerminationException("stop", 7);
        assertEquals(7, ex.getExitCode());
    }

    @Test
    void executorServiceAutoCloseableShouldShutdownExecutor() throws Exception {
        Class<?> clazz = Class.forName("org.machanism.machai.gw.tools.CommandFunctionTools$ExecutorServiceAutoCloseable");
        Constructor<?> constructor = clazz.getDeclaredConstructor(java.util.concurrent.ExecutorService.class);
        constructor.setAccessible(true);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        Object wrapper = constructor.newInstance(executor);

        clazz.getDeclaredMethod("close").invoke(wrapper);

        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
    }
}
