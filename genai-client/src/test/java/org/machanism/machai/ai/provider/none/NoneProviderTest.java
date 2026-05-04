package org.machanism.machai.ai.provider.none;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.provider.Genai;

class NoneProviderTest {

    @TempDir
    File tempDir;

    @Test
    void promptClearAndGetPromptsShouldManagePromptBuffer() {
        NoneProvider provider = new NoneProvider();

        provider.prompt("first");
        provider.prompt("second");

        assertEquals("first" + Genai.PARAGRAPH_SEPARATOR + "second" + Genai.PARAGRAPH_SEPARATOR, provider.getPrompts());

        provider.clear();

        assertEquals("", provider.getPrompts());
    }

    @Test
    void embeddingShouldThrowUnsupportedOperationException() {
        NoneProvider provider = new NoneProvider();

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> provider.embedding("text", 5));

        assertEquals("NoneProvider doesn't support embedding generation.", ex.getMessage());
    }

    @Test
    void performShouldWriteInstructionsAndPromptsAndThenClearBuffer() throws IOException {
        NoneProvider provider = new NoneProvider();
        File logFile = new File(new File(tempDir, "nested/dir"), "inputs.txt");

        provider.inputsLog(logFile);
        provider.instructions("system instructions");
        provider.prompt("prompt body");

        String result = provider.perform();

        assertNull(result);
        assertEquals("", provider.getPrompts());
        assertEquals("system instructions",
                new String(Files.readAllBytes(new File(logFile.getParentFile(), "instructions.txt").toPath()), StandardCharsets.UTF_8));
        assertEquals("prompt body" + Genai.PARAGRAPH_SEPARATOR,
                new String(Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    void performShouldUseUserDirectoryWhenInputsLogHasNoParent() throws IOException {
        NoneProvider provider = new NoneProvider();
        File originalUserDir = new File(System.getProperty("user.dir"));
        File testUserDir = new File(tempDir, "user-home-simulated");
        File logFile = new File("standalone-inputs.txt");
        File instructionsFile = new File(testUserDir, "instructions.txt");
        File promptsFile = new File(logFile.getPath());

        Files.createDirectories(testUserDir.toPath());
        System.setProperty("user.dir", testUserDir.getAbsolutePath());
        try {
            provider.inputsLog(logFile);
            provider.instructions("standalone instructions");
            provider.prompt("standalone prompt");

            provider.perform();

            assertEquals("standalone instructions",
                    new String(Files.readAllBytes(instructionsFile.toPath()), StandardCharsets.UTF_8));
            assertEquals("standalone prompt" + Genai.PARAGRAPH_SEPARATOR,
                    new String(Files.readAllBytes(promptsFile.toPath()), StandardCharsets.UTF_8));
        } finally {
            System.setProperty("user.dir", originalUserDir.getAbsolutePath());
            Files.deleteIfExists(promptsFile.toPath());
            Files.deleteIfExists(instructionsFile.toPath());
        }
    }

    @Test
    void performWithoutInputsLogShouldOnlyClearPrompts() {
        NoneProvider provider = new NoneProvider();

        provider.instructions("ignored");
        provider.prompt("text");

        String result = provider.perform();

        assertNull(result);
        assertEquals("", provider.getPrompts());
    }

    @Test
    void noOpMethodsAndUsageShouldRemainStable() {
        NoneProvider provider = new NoneProvider();

        provider.addTool("name", "description", null, "param");
        provider.setWorkingDir(tempDir);
        provider.init((Configurator) null);
        Usage usage = provider.usage();

        assertNotNull(usage);
        assertEquals(0L, usage.getInputTokens());
        assertEquals(0L, usage.getInputCachedTokens());
        assertEquals(0L, usage.getOutputTokens());
    }
}
