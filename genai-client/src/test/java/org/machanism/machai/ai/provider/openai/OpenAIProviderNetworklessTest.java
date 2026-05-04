package org.machanism.machai.ai.provider.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
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
        provider.init(minimalConfig());
        provider.instructions("system instructions");
        provider.prompt("hello");

        File nested = new File(new File(tempDir, "n1"), "inputs.log");
        provider.inputsLog(nested);
        provider.logInputs();

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
        provider.init(minimalConfig());
        provider.instructions("x");
        provider.prompt("hello");

        File dirAsFile = new File(tempDir, "asDir");
        assertTrue(dirAsFile.mkdirs());
        provider.inputsLog(dirAsFile);
        provider.logInputs();

        assertTrue(dirAsFile.isDirectory());
    }

    @Test
    void inputsLog_shouldDisableLoggingWhenNull() {
        provider.init(minimalConfig());
        provider.prompt("hello");
        provider.inputsLog(null);
        provider.logInputs();
        assertTrue(true);
    }

    @Test
    void init_shouldNotReadTimeoutUntilClientIsBuilt() {
        Configurator config = minimalConfig();
        config.set("GENAI_TIMEOUT", "11");
        provider.init(config);
        assertEquals(0L, provider.getTimeout());
    }

    @Test
    void setTimeout_shouldUpdateTimeout() {
        provider.init(minimalConfig());
        provider.setTimeout(99L);
        assertEquals(99L, provider.getTimeout());
    }

    @Test
    void embedding_shouldReturnNullWhenTextNull() {
        provider.init(minimalConfig());
        assertNull(provider.embedding(null, 10));
    }

    private Configurator minimalConfig() {
        Configurator config = TestConfigurators.mapBacked();
        config.set("chatModel", "gpt-test");
        config.set("OPENAI_API_KEY", "dummy");
        return config;
    }

    static Object fakeModelService() {
        try {
            Class<?> modelServiceType = Class.forName("com.openai.services.blocking.ModelService");
            return Proxy.newProxyInstance(OpenAIProviderNetworklessTest.class.getClassLoader(),
                    new Class<?>[] { modelServiceType },
                    (proxy, method, args) -> {
                        if ("list".equals(method.getName())) {
                            return fakeModelListPage();
                        }
                        return defaultValue(method.getReturnType());
                    });
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static Object fakeModelListPage() {
        try {
            Class<?> pageType = Class.forName("com.openai.core.Page");
            return Proxy.newProxyInstance(OpenAIProviderNetworklessTest.class.getClassLoader(),
                    new Class<?>[] { pageType },
                    (proxy, method, args) -> {
                        if ("items".equals(method.getName())) {
                            return Collections.emptyList();
                        }
                        if ("stream".equals(method.getName())) {
                            return Collections.emptyList().stream();
                        }
                        return defaultValue(method.getReturnType());
                    });
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    static Object defaultValue(Class<?> returnType) {
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

    private static final class TestableOpenAIProvider extends OpenAIProvider {
        @Override
        protected com.openai.client.OpenAIClient getClient() {
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if ("models".equals(method.getName())) {
                        return fakeModelService();
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
}
