package org.machanism.machai.ai.provider.codemie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;

class CodeMieProviderInitTest {

	@TempDir
	File tempDir;

	@Test
	void init_whenChatModelBlank_setsBaseUrl() {
		// Arrange
		Configurator conf = mapBacked();
		conf.set("chatModel", "");
		conf.set(Genai.USERNAME_PROP_NAME, "user@example.com");
		conf.set(Genai.PASSWORD_PROP_NAME, "pw");
		conf.set("workingDir", tempDir.getAbsolutePath());

		CodeMieProvider provider = new CodeMieProvider();

		// Act
		provider.init(conf);

		// Assert
		assertEquals(CodeMieProvider.BASE_URL, conf.get("OPENAI_BASE_URL"));
		assertNotNull(provider.usage());
	}

	@Test
	void init_whenChatModelUnsupported_throwsIllegalArgumentException() {
		// Arrange
		Configurator conf = mapBacked();
		conf.set("chatModel", "unknown-model");
		conf.set(Genai.USERNAME_PROP_NAME, "user");
		conf.set(Genai.PASSWORD_PROP_NAME, "pw");

		CodeMieProvider provider = new CodeMieProvider();

		// Act + Assert
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> provider.init(conf));
		assertEquals("Unsupported model: 'unknown-model'.", ex.getMessage());
	}

	private static Configurator mapBacked() {
		return new Configurator() {
			private final Map<String, String> map = new HashMap<>();

			@Override
			public String get(String key) {
				return map.get(key);
			}

			@Override
			public String get(String key, String defaultValue) {
				return map.getOrDefault(key, defaultValue);
			}

			@Override
			public int getInt(String key) {
				return Integer.parseInt(get(key));
			}

			@Override
			public Integer getInt(String key, Integer defaultValue) {
				String v = map.get(key);
				return v == null ? defaultValue : Integer.valueOf(v);
			}

			@Override
			public boolean getBoolean(String key) {
				return Boolean.parseBoolean(get(key));
			}

			@Override
			public Boolean getBoolean(String key, Boolean defaultValue) {
				String v = map.get(key);
				return v == null ? defaultValue : Boolean.valueOf(v);
			}

			@Override
			public long getLong(String key) {
				return Long.parseLong(get(key));
			}

			@Override
			public Long getLong(String key, Long defaultValue) {
				String v = map.get(key);
				return v == null ? defaultValue : Long.valueOf(v);
			}

			@Override
			public File getFile(String key) {
				String v = map.get(key);
				return v == null ? null : new File(v);
			}

			@Override
			public File getFile(String key, File defaultValue) {
				File f = getFile(key);
				return f == null ? defaultValue : f;
			}

			@Override
			public double getDouble(String key) {
				return Double.parseDouble(get(key));
			}

			@Override
			public Double getDouble(String key, Double defaultValue) {
				String v = map.get(key);
				return v == null ? defaultValue : Double.valueOf(v);
			}

			@Override
			public String getName() {
				return "map";
			}

			@Override
			public void set(String key, String value) {
				map.put(key, value);
			}
		};
	}
}
