package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class FunctionToolsReplaceTest {

	private static final class TestTools implements FunctionTools {
		@Override
		public void applyTools(org.machanism.machai.ai.provider.Genai provider) {
			// not used
		}
	}

	private static final class MapConfigurator implements Configurator {
		private final Map<String, String> map;
		private final String name;

		private MapConfigurator(String name, Map<String, String> map) {
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
	void replace_whenConfiguratorNull_returnsOriginalInstance() {
		// Arrange
		TestTools tools = new TestTools();
		String value = "Hello ${name}";

		// Act
		String result = tools.replace(value, null);

		// Assert
		assertSame(value, result);
	}

	@Test
	void replace_whenPlainTextProvided_returnsOriginalText() {
		// Arrange
		TestTools tools = new TestTools();
		Configurator conf = new MapConfigurator("test", new HashMap<>());
		String value = "Hello world";

		// Act
		String result = tools.replace(value, conf);

		// Assert
		assertEquals(value, result);
	}

	@Test
	void replace_whenPlaceholderMissing_keepsPlaceholderAndTerminates() {
		// Arrange
		TestTools tools = new TestTools();
		Configurator conf = new MapConfigurator("test", new HashMap<>());
		String value = "Hello ${name}";

		// Act
		String result = tools.replace(value, conf);

		// Assert
		assertEquals(value, result);
	}

	@Test
	void replace_whenSinglePlaceholderPresent_replacesValue() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("name", "Viktor");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("Hello ${name}", conf);

		// Assert
		assertEquals("Hello Viktor", result);
	}

	@Test
	void replace_whenSamePlaceholderAppearsMultipleTimes_replacesEveryOccurrence() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("name", "Viktor");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("${name}-${name}-${name}", conf);

		// Assert
		assertEquals("Viktor-Viktor-Viktor", result);
	}

	@Test
	void replace_whenMultiplePlaceholdersPresent_replacesAllResolvableValues() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("greeting", "Hello");
		map.put("name", "Viktor");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("${greeting} ${name}", conf);

		// Assert
		assertEquals("Hello Viktor", result);
	}

	@Test
	void replace_whenPlaceholderValueIsEmptyString_replacesWithEmptyString() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("name", "");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("Hello ${name}!", conf);

		// Assert
		assertEquals("Hello !", result);
	}

	@Test
	void replace_whenPlaceholderResolvesToTextContainingNonPlaceholderDollarBrace_returnsResolvedText() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("value", "cost is $5");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("The ${value}", conf);

		// Assert
		assertEquals("The cost is $5", result);
	}

	@Test
	void replace_whenMixedResolvableAndUnresolvablePlaceholders_keepsUnknownValues() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("name", "Viktor");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("Hello ${name} from ${city}", conf);

		// Assert
		assertEquals("Hello Viktor from ${city}", result);
	}

	@Test
	void replace_whenNestedPlaceholders_resolvesRecursively() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("a", "${b}");
		map.put("b", "final");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("Value=${a}", conf);

		// Assert
		assertEquals("Value=final", result);
	}

	@Test
	void replace_whenNestedPlaceholdersBecomePartiallyUnresolvable_returnsLastPartiallyResolvedValue() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("outer", "${inner}");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("Value=${outer}", conf);

		// Assert
		assertEquals("Value=${inner}", result);
	}

	@Test
	void replace_whenResolutionChainExceedsIterationLimit_stopsAfterTenPasses() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("a1", "${a2}");
		map.put("a2", "${a3}");
		map.put("a3", "${a4}");
		map.put("a4", "${a5}");
		map.put("a5", "${a6}");
		map.put("a6", "${a7}");
		map.put("a7", "${a8}");
		map.put("a8", "${a9}");
		map.put("a9", "${a10}");
		map.put("a10", "${a11}");
		map.put("a11", "${a12}");
		map.put("a12", "done");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("${a1}", conf);

		// Assert
		assertEquals("${a11}", result);
	}

	@Test
	void replace_whenPlaceholdersReferenceEachOther_stopsWhenReplacementNoLongerChanges() {
		// Arrange
		TestTools tools = new TestTools();
		Map<String, String> map = new HashMap<>();
		map.put("a", "${b}");
		map.put("b", "${a}");
		Configurator conf = new MapConfigurator("test", map);

		// Act
		String result = tools.replace("${a}", conf);

		// Assert
		assertEquals("${a}", result);
	}
}
