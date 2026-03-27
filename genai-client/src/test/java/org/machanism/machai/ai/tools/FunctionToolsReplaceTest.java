package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class FunctionToolsReplaceTest {

	private static final class TestTools implements FunctionTools {
		@Override
		public void applyTools(org.machanism.machai.ai.manager.Genai provider) {
			// not used
		}
	}

	private static final class MapConfigurator implements Configurator {
		private final java.util.Map<String, String> map;
		private final String name;

		private MapConfigurator(String name, java.util.Map<String, String> map) {
			this.name = name;
			this.map = map;
		}

		@Override
		public String get(String key) {
			return map.get(key);
		}

		@Override
		public String get(String key, String defaultValue) {
			String val = map.get(key);
			return val == null ? defaultValue : val;
		}

		@Override
		public int getInt(String key) {
			return Integer.parseInt(get(key));
		}

		@Override
		public Integer getInt(String key, Integer defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Integer.valueOf(val);
		}

		@Override
		public boolean getBoolean(String key) {
			return Boolean.parseBoolean(get(key));
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Boolean.valueOf(val);
		}

		@Override
		public long getLong(String key) {
			return Long.parseLong(get(key));
		}

		@Override
		public Long getLong(String key, Long defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Long.valueOf(val);
		}

		@Override
		public File getFile(String key) {
			String val = get(key);
			return val == null ? null : new File(val);
		}

		@Override
		public File getFile(String key, File defaultValue) {
			File val = getFile(key);
			return val == null ? defaultValue : val;
		}

		@Override
		public double getDouble(String key) {
			return Double.parseDouble(get(key));
		}

		@Override
		public Double getDouble(String key, Double defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Double.valueOf(val);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void set(String key, String value) {
			map.put(key, value);
		}
	}

	@Test
	void replace_whenValueNull_returnsNull() {
		// Arrange
		TestTools tools = new TestTools();

		// Act
		String result = tools.replace(null, null);

		// Assert
		assertNull(result);
	}

	@Test
	void replace_whenConfiguratorNull_returnsOriginal() {
		// Arrange
		TestTools tools = new TestTools();
		String value = "Hello ${name}";

		// Act
		String result = tools.replace(value, null);

		// Assert
		assertEquals(value, result);
	}

	@Test
	void replace_whenPlaceholderMissing_keepsPlaceholderAndTerminates() {
		// Arrange
		TestTools tools = new TestTools();
		Configurator conf = new MapConfigurator("test", new java.util.HashMap<>());
		String value = "Hello ${name}";

		// Act
		String result = tools.replace(value, conf);

		// Assert
		assertEquals(value, result);
	}

	@Test
	void replace_whenSinglePlaceholder_present_replaces() {
		// Arrange
		TestTools tools = new TestTools();
		java.util.Map<String, String> map = new java.util.HashMap<>();
		map.put("name", "Viktor");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("Hello ${name}", conf);

		// Assert
		assertEquals("Hello Viktor", result);
	}

	@Test
	void replace_whenNestedPlaceholders_resolvesRecursively() {
		// Arrange
		TestTools tools = new TestTools();
		java.util.Map<String, String> map = new java.util.HashMap<>();
		map.put("a", "${b}");
		map.put("b", "final");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("Value=${a}", conf);

		// Assert
		assertEquals("Value=final", result);
	}
}
