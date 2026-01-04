package org.machanism.machai.ai.promt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Function;
import org.machanism.machai.ai.manager.GenAIProvider;
import com.fasterxml.jackson.databind.JsonNode;
import static org.junit.jupiter.api.Assertions.*;

class PromptProviderTest {

    private PromptProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PromptProvider();
    }

    @Test
    @DisplayName("prompt(String): returns null")
    void prompt_ReturnsNull() {
        assertNull(provider.prompt("test"));
    }

    @Test
    @DisplayName("promptFile(File, String): returns null")
    void promptFile_ReturnsNull() throws IOException {
        File file = File.createTempFile("temp", "txt");
        assertNull(provider.promptFile(file, "bundleName"));
        file.delete();
    }

    @Test
    @DisplayName("addFile(File): does not throw IOException")
    void addFile_File_DoesNotThrow() {
        File file = new File("doesnotexist.txt");
        assertDoesNotThrow(() -> provider.addFile(file));
    }

    @Test
    @DisplayName("addFile(URL): does not throw IOException")
    void addFile_URL_DoesNotThrow() throws IOException {
        URL url = new URL("file:///temp.txt");
        assertDoesNotThrow(() -> provider.addFile(url));
    }

    @Test
    @DisplayName("embedding(String): throws NotImplementedException")
    void embedding_ThrowsNotImplemented() {
        assertThrows(org.apache.commons.lang.NotImplementedException.class,
                () -> provider.embedding("text"));
    }

    @Test
    @DisplayName("clear(): does not throw")
    void clear_DoesNotThrow() {
        assertDoesNotThrow(() -> provider.clear());
    }

    @Test
    @DisplayName("addTool(String, String, Function, String...): does not throw")
    void addTool_DoesNotThrow() {
        Function<JsonNode, Object> fn = node -> "output";
        assertDoesNotThrow(() -> provider.addTool("name", "desc", fn, "p1", "p2"));
    }

    @Test
    @DisplayName("instructions(String): returns null")
    void instructions_ReturnsNull() {
        assertNull(provider.instructions("instructions"));
    }

    @Test
    @DisplayName("promptBundle(ResourceBundle): returns null")
    @Disabled("Need to fix.")
    void promptBundle_ReturnsNull() {
        ResourceBundle rb = ResourceBundle.getBundle("testBundle");
        assertNull(provider.promptBundle(rb));
    }

    @Test
    @DisplayName("perform(boolean): returns null")
    void perform_ReturnsNull() {
        assertNull(provider.perform(false));
    }

    @Test
    @DisplayName("inputsLog(File): returns null")
    void inputsLog_ReturnsNull() {
        File tempDir = new File("testTmpDir");
        assertNull(provider.inputsLog(tempDir));
    }
}
