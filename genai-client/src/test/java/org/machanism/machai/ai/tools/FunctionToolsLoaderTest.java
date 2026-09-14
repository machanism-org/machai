package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.Genai;

class FunctionToolsLoaderTest {

    @Test
    void applyTools_registersOnlyUnrestrictedAndAssignableTools() throws Exception {
        // Arrange
        FunctionToolsLoader loader = new FunctionToolsLoader();
        List<FunctionTools> tools = discoveredTools(loader);
        tools.clear();
        Unrestricted unrestricted = new Unrestricted();
        Supported supported = new Supported();
        Unsupported unsupported = new Unsupported();
        tools.add(unrestricted);
        tools.add(supported);
        tools.add(unsupported);
        Genai provider = mock(Genai.class);
        String[] requestedTools = { "read-file" };

        // Act
        loader.applyTools(provider, requestedTools, ChildApplication.class);

        // Assert
        verifyRegistered(provider, unrestricted, requestedTools);
        verifyRegistered(provider, supported, requestedTools);
        verify(provider, never()).addTools(unsupported, requestedTools);
        verify(provider, never()).addPrompts(unsupported);
        verify(provider, never()).addResources(unsupported);
    }

    @Test
    void applyTools_acceptsExactSupportedApplicationClass() throws Exception {
        // Arrange
        FunctionToolsLoader loader = new FunctionToolsLoader();
        List<FunctionTools> tools = discoveredTools(loader);
        tools.clear();
        Supported supported = new Supported();
        tools.add(supported);
        Genai provider = mock(Genai.class);

        // Act
        loader.applyTools(provider, null, ParentApplication.class);

        // Assert
        verifyRegistered(provider, supported, null);
    }

    @Test
    void applyTools_doesNotRegisterToolsWhenNoSupportedApplicationMatches() throws Exception {
        // Arrange
        FunctionToolsLoader loader = new FunctionToolsLoader();
        List<FunctionTools> tools = discoveredTools(loader);
        tools.clear();
        Unsupported unsupported = new Unsupported();
        tools.add(unsupported);
        Genai provider = mock(Genai.class);
        String[] requestedTools = { "ignored" };

        // Act
        loader.applyTools(provider, requestedTools, ParentApplication.class);

        // Assert
        verify(provider, never()).addTools(unsupported, requestedTools);
        verify(provider, never()).addPrompts(unsupported);
        verify(provider, never()).addResources(unsupported);
    }

    @Test
    void constructor_discoversFunctionToolsRegisteredThroughServiceLoader() throws Exception {
        // Arrange and Act
        FunctionToolsLoader loader = new FunctionToolsLoader();

        // Assert
        assertTrue(discoveredTools(loader).stream().anyMatch(DiscoveredFunctionTools.class::isInstance));
    }

    private static void verifyRegistered(Genai provider, FunctionTools functionTools, String[] requestedTools) {
        verify(provider).addTools(functionTools, requestedTools);
        verify(provider).addPrompts(functionTools);
        verify(provider).addResources(functionTools);
    }

    @SuppressWarnings("unchecked")
    private static List<FunctionTools> discoveredTools(FunctionToolsLoader loader) throws Exception {
        Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
        field.setAccessible(true);
        return (List<FunctionTools>) field.get(loader);
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
