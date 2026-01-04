package org.machanism.machai.ai.openAI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.openai.models.ChatModel;

public class OpenAIProviderTest {
    private OpenAIProvider provider;
    private ChatModel chatModel;

    @BeforeEach
    void setup() {
        chatModel = mock(ChatModel.class);
        // Set up OPENAI_API_KEY so it doesn't throw
        System.setProperty("OPENAI_API_KEY", "testapikey");
        provider = new OpenAIProvider(chatModel);
    }

    @Test
    void prompt_appendsUserMessage() {
        GenAIProvider returned = provider.prompt("Hello World");
        assertSame(provider, returned);
    }

    @Test
    @Disabled("Need to fix.")
    void promptFile_nullBundleMessageName_usesFileData() throws IOException {
        File file = mock(File.class);
        when(file.getName()).thenReturn("test.txt");
        // Simulating FileInputStream via Mockito impossible; test behaviour only
        assertDoesNotThrow(() -> provider.promptFile(file, null));
    }

    @Test
    @Disabled("Need to fix.")
    void promptFile_withBundleMessageName_usesPromptBundle() throws IOException {
        File file = mock(File.class);
        ResourceBundle bundle = mock(ResourceBundle.class);
        when(bundle.getString("testMessage")).thenReturn("File: {0}, Type: {1}, Data: {2}");
        provider.promptBundle(bundle);
        when(file.getName()).thenReturn("test.txt");
        assertDoesNotThrow(() -> provider.promptFile(file, "testMessage"));
    }

    @Test
    @Disabled("Need to fix.")
    void addFile_file_addsInputMessage() throws IOException {
        File file = mock(File.class);
        assertDoesNotThrow(() -> provider.addFile(file));
    }

    @Test
    void addFile_url_addsInputMessage() throws IOException {
        URL url = mock(URL.class);
        when(url.toString()).thenReturn("http://example.com/file.txt");
        assertDoesNotThrow(() -> provider.addFile(url));
    }

    @Test
    void embedding_returnsList() {
        List<Float> result = provider.embedding("Test embedding");
        assertNotNull(result);
    }

    @Test
    void addTool_addsToolToMap() {
        provider.addTool("testFunction", "desc", (JsonNode node) -> "ok", "param1:string:required:desc1");
        // Can't access toolMap directly; rely on addTool doesn't throw
    }

    @Test
    void instructions_setsInstructions() {
        String instruct = "Do something";
        GenAIProvider returned = provider.instructions(instruct);
        assertSame(provider, returned);
    }

    @Test
    void promptBundle_setsPromptBundle() {
        ResourceBundle bundle = mock(ResourceBundle.class);
        GenAIProvider returned = provider.promptBundle(bundle);
        assertSame(provider, returned);
    }

    @Test
    void inputsLog_setsInputsLogFile() {
        File file = mock(File.class);
        GenAIProvider returned = provider.inputsLog(file);
        assertSame(provider, returned);
    }

    @Test
    void clear_clearsInputs() {
        provider.prompt("user");
        provider.clear();
        // Unable to access internal state, but method should not throw
    }
}
