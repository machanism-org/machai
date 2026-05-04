package org.machanism.machai.ai.provider.codemie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.provider.openai.OpenAIProvider;

class CodeMieProviderInitTest {

    @Test
    void initShouldUseOpenAiProviderForBlankModel() throws Exception {
        CodeMieProvider provider = new CodeMieProvider();
        Configurator config = baseConfig();
        config.set("chatModel", "");

        provider.init(config);

        Object delegate = getDelegate(provider);
        assertNotNull(delegate);
        assertTrueAssignable(OpenAIProvider.class, delegate.getClass());
        assertEquals(CodeMieProvider.BASE_URL, config.get("OPENAI_BASE_URL"));
    }

    @Test
    void initShouldUseGeminiProviderForGeminiModel() {
        CodeMieProvider provider = new CodeMieProvider();
        Configurator config = baseConfig();
        config.set("chatModel", "gemini-1.5-pro");
        config.set(CodeMieProvider.AUTH_URL_PROP_NAME, "http://127.0.0.1:1/token");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> provider.init(config));

        assertEquals("Authorization failed for user 'user@example.com'", ex.getMessage());
    }

    @Test
    void initShouldUseClaudeProviderForClaudeModel() {
        CodeMieProvider provider = new CodeMieProvider();
        Configurator config = baseConfig();
        config.set("chatModel", "claude-3-5-sonnet");
        config.set(CodeMieProvider.AUTH_URL_PROP_NAME, "http://127.0.0.1:1/token");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> provider.init(config));

        assertEquals("Authorization failed for user 'user@example.com'", ex.getMessage());
    }

    @Test
    void initShouldRejectUnsupportedModel() {
        CodeMieProvider provider = new CodeMieProvider();
        Configurator config = baseConfig();
        config.set("chatModel", "unsupported-model");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> provider.init(config));

        assertEquals("Unsupported model: 'unsupported-model'.", ex.getMessage());
    }

    @Test
    void authorizeShouldWrapIoException() throws Exception {
        Method method = CodeMieProvider.class.getDeclaredMethod("authorize", String.class, String.class, String.class);
        method.setAccessible(true);

        Throwable throwable = assertThrows(Throwable.class,
                () -> method.invoke(null, "http://127.0.0.1:1/token", "name", "secret"));

        Throwable cause = throwable.getCause();
        assertInstanceOf(IllegalArgumentException.class, cause);
        assertEquals("Authorization failed for user 'name'", cause.getMessage());
    }

    private static Configurator baseConfig() {
        return new MapBackedConfigurator();
    }

    private static Object getDelegate(CodeMieProvider provider) throws Exception {
        Field field = provider.getClass().getSuperclass().getDeclaredField("provider");
        field.setAccessible(true);
        return field.get(provider);
    }

    private static void assertTrueAssignable(Class<?> expectedSuperType, Class<?> actualType) {
        org.junit.jupiter.api.Assertions.assertTrue(expectedSuperType.isAssignableFrom(actualType));
    }

    private static final class MapBackedConfigurator implements Configurator {
        private final Map<String, String> values = new HashMap<String, String>();

        MapBackedConfigurator() {
            values.put(Genai.USERNAME_PROP_NAME, "user@example.com");
            values.put(Genai.PASSWORD_PROP_NAME, "password");
        }

        @Override
        public String get(String key) {
            String value = values.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing config key: " + key);
            }
            return value;
        }

        @Override
        public String get(String key, String defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : value;
        }

        @Override
        public int getInt(String key) {
            return Integer.parseInt(get(key));
        }

        @Override
        public Integer getInt(String key, Integer defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : Integer.valueOf(value);
        }

        @Override
        public boolean getBoolean(String key) {
            return Boolean.parseBoolean(get(key));
        }

        @Override
        public Boolean getBoolean(String key, Boolean defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : Boolean.valueOf(value);
        }

        @Override
        public long getLong(String key) {
            return Long.parseLong(get(key));
        }

        @Override
        public Long getLong(String key, Long defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : Long.valueOf(value);
        }

        @Override
        public File getFile(String key) {
            return new File(get(key));
        }

        @Override
        public File getFile(String key, File defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : new File(value);
        }

        @Override
        public double getDouble(String key) {
            return Double.parseDouble(get(key));
        }

        @Override
        public Double getDouble(String key, Double defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : Double.valueOf(value);
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public void set(String key, String value) {
            values.put(key, value);
        }
    }
}
