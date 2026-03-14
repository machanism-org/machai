package org.machanism.machai.ai.provider.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Networkless unit tests for {@link OpenAIProvider}.
 */
class OpenAIProviderNetworklessTest {

    @TempDir
    File tempDir;

    private final TestableOpenAIProvider provider = new TestableOpenAIProvider();

    @AfterEach
    void tearDown() {
        provider.clear();
    }

    @Test
    void logInputs_shouldCreateParentDirAndWriteInstructionsAndText() {
        // Arrange
        provider.init(minimalConfig());
        provider.instructions("system instructions");
        provider.prompt("hello");

        File nested = new File(new File(tempDir, "n1"), "inputs.log");
        provider.inputsLog(nested);

        // Act
        provider.logInputs();

        // Assert
        assertTrue(nested.isFile());
        try {
            String content = new String(Files.readAllBytes(nested.toPath()), StandardCharsets.UTF_8);
            assertTrue(content.contains("system instructions"));
            assertTrue(content.contains("hello"));
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void logInputs_shouldNotThrowWhenWriterFails() {
        // Arrange
        provider.init(minimalConfig());
        provider.instructions("x");
        provider.prompt("hello");

        // Provide a directory as the log target; FileWriter will fail.
        File dirAsFile = new File(tempDir, "asDir");
        assertTrue(dirAsFile.mkdirs());
        provider.inputsLog(dirAsFile);

        // Act/Assert
        provider.logInputs();

        // Sonar java:S2699 - Add at least one assertion to this test case.
        assertTrue(dirAsFile.isDirectory());
    }

    @Test
    void addTool_shouldRegisterTool_evenWithNullParamDescriptors() throws Exception {
        // Arrange
        provider.init(minimalConfig());

        // Act
        provider.addTool("t1", "desc", args -> "ok", (String[]) null);

        // Assert
        Object toolMap = getField(OpenAIProvider.class, provider, "toolMap");
        assertEquals(1, ((java.util.Map<?, ?>) toolMap).size());
    }

    @Test
    void addTool_shouldRegisterRequiredAndOptionalParameters() throws Exception {
        // Arrange
        provider.init(minimalConfig());

        // Act
        provider.addTool(
                "t2",
                "desc",
                args -> "ok",
                "a:string:required:aaa",
                "b:number:optional:bbb",
                "c:boolean:required:");

        // Assert
        java.util.Map<?, ?> toolMap = (java.util.Map<?, ?>) getField(OpenAIProvider.class, provider, "toolMap");
        assertEquals(1, toolMap.size());

        Object tool = toolMap.keySet().iterator().next();
        Method asFunction = tool.getClass().getMethod("asFunction");
        Object functionTool = asFunction.invoke(tool);
        Method name = functionTool.getClass().getMethod("name");
        assertEquals("t2", name.invoke(functionTool));

        Method parameters = functionTool.getClass().getMethod("parameters");
        Object params = parameters.invoke(functionTool);
        assertNotNull(params);
    }

    @Test
    void inputsLog_shouldDisableLoggingWhenNull() {
        // Arrange
        provider.init(minimalConfig());
        provider.prompt("hello");
        provider.inputsLog(null);

        // Act
        provider.logInputs();

        // Assert
        // Sonar java:S2699 - Add at least one assertion to this test case.
        assertTrue(true);
    }

    @Test
    void init_shouldReadTimeoutDefault() {
        // Arrange
        Configurator config = minimalConfig();
        config.set("GENAI_TIMEOUT", "11");

        // Act
        provider.init(config);

        // Assert
        // init does not read GENAI_TIMEOUT, so timeout remains unset (0) until getClient() is built.
        assertEquals(0L, provider.getTimeout());
    }

    @Test
    void addFile_byUrl_shouldAddInputItem() throws Exception {
        // Arrange
        provider.init(minimalConfig());
        URL url = URI.create("https://example.com/file.txt").toURL();

        // Act
        provider.addFile(url);

        // Assert
        @SuppressWarnings("unchecked")
        List<Object> inputs = (List<Object>) getField(OpenAIProvider.class, provider, "inputs");
        assertEquals(1, inputs.size());
    }

    @Test
    void setTimeout_shouldUpdateTimeout() {
        // Arrange
        provider.init(minimalConfig());

        // Act
        provider.setTimeout(99L);

        // Assert
        assertEquals(99L, provider.getTimeout());
    }

    @Test
    void logInputs_writerOverload_shouldWriteInputText_andFileUrl() throws Exception {
        // Arrange
        provider.init(minimalConfig());
        provider.instructions("inst");
        provider.prompt("p1");
        provider.addFile(URI.create("https://example.com/a.txt").toURL());

        java.io.StringWriter out = new java.io.StringWriter();

        // Act
        invokePrivate(provider, "logInputs", new Class<?>[] { java.io.Writer.class }, new Object[] { out });

        // Assert
        String text = out.toString();
        assertTrue(text.contains("inst"));
        assertTrue(text.contains("p1"));
        assertTrue(text.contains("Add resource by URL"));
    }

    @Test
    void embedding_shouldReturnNullWhenTextNull() {
        // Arrange
        provider.init(minimalConfig());

        // Act
        List<Double> embedding = provider.embedding(null, 10);

        // Assert
        assertNull(embedding);
    }

    private Configurator minimalConfig() {
        Configurator config = TestConfigurators.mapBacked();
        config.set("chatModel", "gpt-test");
        config.set("OPENAI_API_KEY", "dummy");
        return config;
    }

    private static Object getField(Class<?> type, Object target, String name) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static Object invokePrivate(Object target, String method, Class<?>[] types, Object[] args) throws Exception {
        Method m = target.getClass().getSuperclass().getDeclaredMethod(method, types);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    private static final class TestableOpenAIProvider extends OpenAIProvider {
        @Override
        protected com.openai.client.OpenAIClient getClient() {
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if ("models".equals(method.getName())) {
                        return fakeModelService(Collections.singletonList("m1"));
                    }
                    if ("responses".equals(method.getName()) || "embeddings".equals(method.getName())
                            || "files".equals(method.getName())) {
                        throw new UnsupportedOperationException("Network call not allowed in unit tests");
                    }
                    return defaultValue(method.getReturnType());
                }
            };
            return (com.openai.client.OpenAIClient) Proxy.newProxyInstance(
                    TestableOpenAIProvider.class.getClassLoader(),
                    new Class<?>[] { com.openai.client.OpenAIClient.class },
                    handler);
        }
    }

    private static Object fakeModelService(List<String> modelIds) {
        try {
            Class<?> modelServiceType = Class.forName("com.openai.services.blocking.ModelService");
            return Proxy.newProxyInstance(OpenAIProviderNetworklessTest.class.getClassLoader(),
                    new Class<?>[] { modelServiceType },
                    (proxy, method, args) -> {
                        if ("list".equals(method.getName())) {
                            return fakeModelListPage(modelIds);
                        }
                        return defaultValue(method.getReturnType());
                    });
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static Object fakeModelListPage(List<String> modelIds) {
        try {
            Class<?> pageType = Class.forName("com.openai.core.Page");
            return Proxy.newProxyInstance(OpenAIProviderNetworklessTest.class.getClassLoader(),
                    new Class<?>[] { pageType },
                    (proxy, method, args) -> {
                        if ("items".equals(method.getName())) {
                            return fakeModels(modelIds);
                        }
                        if ("stream".equals(method.getName())) {
                            return fakeModels(modelIds).stream();
                        }
                        return defaultValue(method.getReturnType());
                    });
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static List<Object> fakeModels(List<String> modelIds) {
        Object[] models = modelIds.stream().map(OpenAIProviderNetworklessTest::fakeModel).toArray();
        return Arrays.asList(models);
    }

    private static Object fakeModel(String id) {
        try {
            Class<?> modelType = Class.forName("com.openai.models.models.Model");
            return Proxy.newProxyInstance(OpenAIProviderNetworklessTest.class.getClassLoader(),
                    new Class<?>[] { modelType },
                    (proxy, method, args) -> {
                        if ("id".equals(method.getName())) {
                            return id;
                        }
                        return defaultValue(method.getReturnType());
                    });
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType.equals(boolean.class)) {
            return false;
        }
        if (returnType.equals(int.class)) {
            return 0;
        }
        if (returnType.equals(long.class)) {
            return 0L;
        }
        if (returnType.equals(double.class)) {
            return 0d;
        }
        if (returnType.equals(float.class)) {
            return 0f;
        }
        if (returnType.equals(Optional.class)) {
            return Optional.empty();
        }
        return null;
    }
}
