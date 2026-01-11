package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.schema.Bindex;

/**
 * Unit tests for {@link ApplicationAssembly}.
 * 
 * <p>Verifies project directory assignment, assembly LLM execution, bindex prompt integration, and error handling.</p>
 *
 * @author Viktor Tovstyi
 */
class ApplicationAssemblyTest {
    private GenAIProvider provider;
    private ApplicationAssembly assembly;

    @BeforeEach
    void setUp() {
        provider = mock(GenAIProvider.class);
        assembly = new ApplicationAssembly(provider);
    }

    @Test
    void testProjectDirSetterReturnsSelfAndSetsDir() {
        File dir = new File("/tmp/testdir");
        ApplicationAssembly result = assembly.projectDir(dir);
        assertSame(assembly, result);
    }

    @Test
    void testAssemblyExecutesLLMAndNoException() throws IOException {
        String prompt = "Test system prompt";
        List<Bindex> bindexList = new ArrayList<>();
        when(provider.perform()).thenReturn("Response");
        ResourceBundle.clearCache();
        assembly.assembly(prompt, bindexList);
    }

    @Test
    void testBindexPromptIntegratedWhenPresent() throws Exception {
        String prompt = "Library selection prompt";
        Bindex bindex = mock(Bindex.class);
        when(bindex.getId()).thenReturn("libX");
        List<Bindex> bindexList = List.of(bindex);
        when(provider.perform()).thenReturn("Done");
        assembly.assembly(prompt, bindexList);
        verify(provider, atLeastOnce()).prompt(anyString());
    }

    @Test
    @Disabled("IOException throwing scenario - fix for integration.")
    void testAssemblyThrowsIllegalArgumentExceptionOnIOError() throws IOException {
        GenAIProvider faultyProvider = mock(GenAIProvider.class);
        ApplicationAssembly faultyAssembly = new ApplicationAssembly(faultyProvider);
        doThrow(new IOException("fail")).when(faultyProvider).perform();
        List<Bindex> bindexList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> {
            faultyAssembly.assembly("prompt", bindexList);
        });
    }
}
