package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GenAIProviderTest {
    private GenAIProvider provider;

    @BeforeEach
    void setUp() {
        provider = mock(GenAIProvider.class);
    }

    @Test
    void testPrompt() {
        doNothing().when(provider).prompt("Hello!");
        provider.prompt("Hello!");
        verify(provider, times(1)).prompt("Hello!");
    }

    @Test
    void testPromptFile() throws IOException {
        File file = mock(File.class);
        doNothing().when(provider).promptFile(file, "msg");
        provider.promptFile(file, "msg");
        verify(provider, times(1)).promptFile(file, "msg");
    }

    @Test
    void testAddFile_File() throws IOException {
        File file = mock(File.class);
        doNothing().when(provider).addFile(file);
        provider.addFile(file);
        verify(provider, times(1)).addFile(file);
    }

    @Test
    void testAddFile_Url() throws IOException {
        URL url = mock(URL.class);
        doNothing().when(provider).addFile(url);
        provider.addFile(url);
        verify(provider, times(1)).addFile(url);
    }

    @Test
    void testEmbedding() {
        String text = "sample text";
        List<Float> expected = List.of(1.0f, 2.0f);
        when(provider.embedding(text)).thenReturn(expected);
        List<Float> result = provider.embedding(text);
        assertEquals(expected, result);
        verify(provider, times(1)).embedding(text);
    }

    @Test
    void testClear() {
        doNothing().when(provider).clear();
        provider.clear();
        verify(provider, times(1)).clear();
    }

    @Test
    void testAddTool() {
        String name = "toolName";
        String desc = "desc";
        Function<Object[], Object> function = args -> "result";
        doNothing().when(provider).addTool(name, desc, function, "param1");
        provider.addTool(name, desc, function, "param1");
        verify(provider, times(1)).addTool(name, desc, function, "param1");
    }

    @Test
    void testInstructions() {
        String instructions = "Do this";
        doNothing().when(provider).instructions(instructions);
        provider.instructions(instructions);
        verify(provider, times(1)).instructions(instructions);
    }

    @Test
    void testPerform() {
        when(provider.perform()).thenReturn("output");
        String result = provider.perform();
        assertEquals("output", result);
        verify(provider, times(1)).perform();
    }

    @Test
    void testInputsLog() {
        File tempDir = mock(File.class);
        doNothing().when(provider).inputsLog(tempDir);
        provider.inputsLog(tempDir);
        verify(provider, times(1)).inputsLog(tempDir);
    }

    @Test
    void testModel() {
        String modelName = "modelX";
        doNothing().when(provider).model(modelName);
        provider.model(modelName);
        verify(provider, times(1)).model(modelName);
    }

    @Test
    void testSetWorkingDir() {
        File workingDir = mock(File.class);
        doNothing().when(provider).setWorkingDir(workingDir);
        provider.setWorkingDir(workingDir);
        verify(provider, times(1)).setWorkingDir(workingDir);
    }
}
