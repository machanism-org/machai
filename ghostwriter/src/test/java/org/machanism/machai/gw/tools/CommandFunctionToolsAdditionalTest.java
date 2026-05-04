package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    void commandTerminationExceptionShouldExposeExitCode() {
        CommandFunctionTools.ProcessTerminationException ex = new CommandFunctionTools.ProcessTerminationException("stop", 7);
        assertEquals(7, ex.getExitCode());
    }
}
