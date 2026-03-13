package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class GenAIProviderManagerTest {

	private static final class MapConfigurator implements Configurator {
		private final Map<String, String> values = new HashMap<>();

		@Override
		public String get(String key) {
			return values.get(key);
		}

		@Override
		public String get(String key, String def) {
			String v = values.get(key);
			return v != null ? v : def;
		}

		@Override
		public int getInt(String key) {
			return Integer.parseInt(get(key));
		}

		@Override
		public Integer getInt(String key, Integer def) {
			String v = values.get(key);
			return v != null ? Integer.valueOf(v) : def;
		}

		@Override
		public boolean getBoolean(String key) {
			return Boolean.parseBoolean(get(key));
		}

		@Override
		public Boolean getBoolean(String key, Boolean def) {
			String v = values.get(key);
			return v != null ? Boolean.valueOf(v) : def;
		}

		@Override
		public long getLong(String key) {
			return Long.parseLong(get(key));
		}

		@Override
		public Long getLong(String key, Long def) {
			String v = values.get(key);
			return v != null ? Long.valueOf(v) : def;
		}

		@Override
		public File getFile(String key) {
			String v = get(key);
			return v != null ? new File(v) : null;
		}

		@Override
		public File getFile(String key, File def) {
			File f = getFile(key);
			return f != null ? f : def;
		}

		@Override
		public double getDouble(String key) {
			return Double.parseDouble(get(key));
		}

		@Override
		public Double getDouble(String key, Double def) {
			String v = values.get(key);
			return v != null ? Double.valueOf(v) : def;
		}

		@Override
		public String getName() {
			return "MapConfigurator";
		}

		@Override
		public void set(String key, String value) {
			values.put(key, value);
		}
	}

	public static class ProviderXProvider implements GenAIProvider {
		static Configurator lastInitConf;

		@Override
		public void init(Configurator conf) {
			lastInitConf = conf;
		}

		@Override
		public void prompt(String text) {
			// no-op
		}

		@Override
		public void addFile(java.io.File file) throws java.io.IOException {
			// no-op
		}

		@Override
		public void addFile(java.net.URL fileUrl) throws java.io.IOException {
			// no-op
		}

		@Override
		public java.util.List<Double> embedding(String text, long dimensions) {
			return java.util.Collections.emptyList();
		}

		@Override
		public void clear() {
			// no-op
		}

		@Override
		public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
			// no-op
		}

		@Override
		public void instructions(String instructions) {
			// no-op
		}

		@Override
		public String perform() {
			return "";
		}

		@Override
		public void inputsLog(java.io.File bindexTempDir) {
			// no-op
		}

		@Override
		public void setWorkingDir(java.io.File workingDir) {
			// no-op
		}

		@Override
		public Usage usage() {
			return new Usage(0, 0, 0);
		}
	}

	@AfterEach
	void clearUsageAggregation() throws Exception {
		Field f = GenAIProviderManager.class.getDeclaredField("usages");
		f.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<Usage> usages = (List<Usage>) f.get(null);
		usages.clear();
	}

	@Test
	void getProvider_whenFullyQualifiedName_instantiatesAndInitializesAndSetsChatModel() {
		// Arrange
		Configurator conf = new MapConfigurator();

		// Act
		GenAIProvider provider = GenAIProviderManager.getProvider(ProviderXProvider.class.getName() + ":m1", conf);

		// Assert
		assertNotNull(provider);
		assertEquals(ProviderXProvider.class, provider.getClass());
		assertSame(conf, ProviderXProvider.lastInitConf);
		assertEquals("m1", conf.get("chatModel"));
	}

	@Test
	void getProvider_whenShortProviderName_buildsClassNameFromConvention() {
		// Arrange
		Configurator conf = new MapConfigurator();

		// Act
		GenAIProvider provider = GenAIProviderManager.getProvider("None:ignored", conf);

		// Assert
		assertNotNull(provider);
		assertEquals("org.machanism.machai.ai.provider.none.NoneProvider", provider.getClass().getName());
		assertEquals("ignored", conf.get("chatModel"));
	}

	@Test
	void getProvider_whenProviderPrefixOmitted_attemptsToResolveProviderFromTheModelNameAndFails() {
		// Arrange
		Configurator conf = new MapConfigurator();

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> GenAIProviderManager.getProvider("my-model", conf));

		// Assert
		assertNotNull(ex.getCause());
		assertNull(conf.get("chatModel"));
	}

	@Test
	void getProvider_whenClassNotFound_throwsIllegalArgumentExceptionWithCause() {
		// Arrange
		Configurator conf = new MapConfigurator();

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> GenAIProviderManager.getProvider("com.example.DoesNotExist:model", conf));

		// Assert
		assertNotNull(ex.getCause());
	}

	@Test
	void getProvider_whenConstructorMissing_throwsIllegalArgumentException() {
		// Arrange
		Configurator conf = new MapConfigurator();

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> GenAIProviderManager.getProvider(NoDefaultConstructorProvider.class.getName() + ":m", conf));

		// Assert
		assertNotNull(ex.getCause());
	}

	@Test
	void addUsage_aggregatesUsages() throws Exception {
		// Arrange
		Usage u1 = new Usage(1, 2, 3);
		Usage u2 = new Usage(10, 20, 30);

		// Act
		GenAIProviderManager.addUsage(u1);
		GenAIProviderManager.addUsage(u2);

		// Assert
		Field f = GenAIProviderManager.class.getDeclaredField("usages");
		f.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<Usage> usages = (List<Usage>) f.get(null);
		assertEquals(2, usages.size());
		assertSame(u1, usages.get(0));
		assertSame(u2, usages.get(1));
	}

	@Test
	void logUsage_whenNoUsages_doesNotThrow() {
		// Arrange

		// Act
		GenAIProviderManager.logUsage();

		// Assert
		// No exception expected.
	}

	@Test
	void logUsage_whenUsagesPresent_doesNotThrow() {
		// Arrange
		GenAIProviderManager.addUsage(new Usage(1, 1, 1));
		GenAIProviderManager.addUsage(new Usage(2, 0, 3));

		// Act
		GenAIProviderManager.logUsage();

		// Assert
		// No exception expected.
	}

	public static class NoDefaultConstructorProvider implements GenAIProvider {
		public NoDefaultConstructorProvider(@SuppressWarnings("unused") String arg) {
			// intentionally not a no-args constructor
		}

		@Override
		public void init(Configurator conf) {
			// no-op
		}

		@Override
		public void prompt(String text) {
			// no-op
		}

		@Override
		public void addFile(java.io.File file) throws java.io.IOException {
			// no-op
		}

		@Override
		public void addFile(java.net.URL fileUrl) throws java.io.IOException {
			// no-op
		}

		@Override
		public java.util.List<Double> embedding(String text, long dimensions) {
			return java.util.Collections.emptyList();
		}

		@Override
		public void clear() {
			// no-op
		}

		@Override
		public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
			// no-op
		}

		@Override
		public void instructions(String instructions) {
			// no-op
		}

		@Override
		public String perform() {
			return "";
		}

		@Override
		public void inputsLog(java.io.File bindexTempDir) {
			// no-op
		}

		@Override
		public void setWorkingDir(java.io.File workingDir) {
			// no-op
		}

		@Override
		public Usage usage() {
			return new Usage(0, 0, 0);
		}
	}
}
