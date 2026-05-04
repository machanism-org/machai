package org.machanism.machai.ai.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.tools.ToolFunction;

class GenaiAdapterTest {

    @Test
    void setProviderShouldRejectNull() {
        GenaiAdapter adapter = new GenaiAdapter();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adapter.setProvider(null));

        assertEquals("provider must not be null", ex.getMessage());
    }

    @Test
    void methodsShouldDelegateToConfiguredProvider() {
        Genai delegate = mock(Genai.class);
        GenaiAdapter adapter = new GenaiAdapter();
        Configurator configurator = mock(Configurator.class);
        ToolFunction toolFunction = mock(ToolFunction.class);
        File inputsLog = new File("target/test-genai-adapter/inputs.txt");
        File workingDir = new File("target/test-genai-adapter/work");
        List<Double> embedding = Arrays.asList(1.0d, 2.0d);
        Usage usage = new Usage(1, 2, 3);

        when(delegate.embedding("hello", 5L)).thenReturn(embedding);
        when(delegate.perform()).thenReturn("response");
        when(delegate.usage()).thenReturn(usage);
        adapter.setProvider(delegate);

        adapter.init(configurator);
        adapter.prompt("hello");
        List<Double> actualEmbedding = adapter.embedding("hello", 5L);
        adapter.clear();
        adapter.addTool("name", "description", toolFunction, "required param", "optional");
        adapter.instructions("rules");
        String actualResponse = adapter.perform();
        adapter.inputsLog(inputsLog);
        adapter.setWorkingDir(workingDir);
        Usage actualUsage = adapter.usage();

        verify(delegate).init(configurator);
        verify(delegate).prompt("hello");
        verify(delegate).embedding("hello", 5L);
        verify(delegate).clear();
        verify(delegate).addTool("name", "description", toolFunction, "required param", "optional");
        verify(delegate).instructions("rules");
        verify(delegate).perform();
        verify(delegate).inputsLog(inputsLog);
        verify(delegate).setWorkingDir(workingDir);
        verify(delegate).usage();
        assertSame(embedding, actualEmbedding);
        assertEquals("response", actualResponse);
        assertSame(usage, actualUsage);
    }
}
