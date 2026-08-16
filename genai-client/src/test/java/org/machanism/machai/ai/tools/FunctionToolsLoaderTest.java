package org.machanism.machai.ai.tools;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.Genai;

class FunctionToolsLoaderTest {

    @Test
    void applyTools_registersUnrestrictedAndAssignableSupportedTools_only() throws Exception {
        FunctionToolsLoader loader = new FunctionToolsLoader();
        Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<FunctionTools> tools = (List<FunctionTools>) field.get(loader);
        Unrestricted unrestricted = new Unrestricted();
        Supported supported = new Supported();
        Unsupported unsupported = new Unsupported();
        tools.add(unrestricted);
        tools.add(supported);
        tools.add(unsupported);
        Genai provider = mock(Genai.class);

        loader.applyTools(provider, ChildApplication.class);

        verify(provider).addTools(unrestricted);
        verify(provider).addPrompts(unrestricted);
        verify(provider).addResources(unrestricted);
        verify(provider).addTools(supported);
        verify(provider).addPrompts(supported);
        verify(provider).addResources(supported);
        verifyNoInteractionsFor( provider, unsupported);
    }

    private void verifyNoInteractionsFor(Genai provider, FunctionTools unsupported) {
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.never()).addTools(unsupported);
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.never()).addPrompts(unsupported);
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.never()).addResources(unsupported);
    }

    static class ParentApplication { }
    static class ChildApplication extends ParentApplication { }

    static class Unrestricted implements FunctionTools { }

    @SupportedFor(ParentApplication.class)
    static class Supported implements FunctionTools { }

    @SupportedFor(String.class)
    static class Unsupported implements FunctionTools { }
}
