package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        tools.clear();
        Unrestricted unrestricted = new Unrestricted();
        Supported supported = new Supported();
        Unsupported unsupported = new Unsupported();
        tools.add(unrestricted);
        tools.add(supported);
        tools.add(unsupported);
        Genai provider = mock(Genai.class);

        loader.applyTools(provider, null, ChildApplication.class);

        verify(provider).addTools(unrestricted, null);
        verify(provider).addPrompts(unrestricted);
        verify(provider).addResources(unrestricted);
        verify(provider).addTools(supported, null);
        verify(provider).addPrompts(supported);
        verify(provider).addResources(supported);
        verifyNoInteractionsFor( provider, unsupported);
    }

    @Test
    void applyTools_acceptsExactSupportedApplicationClass() throws Exception {
        // Arrange
        FunctionToolsLoader loader = new FunctionToolsLoader();
        Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<FunctionTools> tools = (List<FunctionTools>) field.get(loader);
        tools.clear();
        Supported supported = new Supported();
        tools.add(supported);
        Genai provider = mock(Genai.class);

        // Act
        loader.applyTools(provider, null, ParentApplication.class);

        // Assert
        verify(provider).addTools(supported, null);
        verify(provider).addPrompts(supported);
        verify(provider).addResources(supported);
    }

    @Test
    void constructor_discoversFunctionToolsRegisteredThroughServiceLoader() throws Exception {
        // Arrange and Act
        FunctionToolsLoader loader = new FunctionToolsLoader();

        // Assert
        Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<FunctionTools> tools = (List<FunctionTools>) field.get(loader);
        assertTrue(tools.stream().anyMatch(DiscoveredFunctionTools.class::isInstance));
    }

    private void verifyNoInteractionsFor(Genai provider, FunctionTools unsupported) {
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.never()).addTools(unsupported, null);
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.never()).addPrompts(unsupported);
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.never()).addResources(unsupported);
    }

    static class ParentApplication { }
    static class ChildApplication extends ParentApplication { }

    static class Unrestricted implements FunctionTools { }

    @SupportedFor({String.class, ParentApplication.class})
    static class Supported implements FunctionTools { }

    @SupportedFor(String.class)
    static class Unsupported implements FunctionTools { }

    public static class DiscoveredFunctionTools implements FunctionTools { }
}
