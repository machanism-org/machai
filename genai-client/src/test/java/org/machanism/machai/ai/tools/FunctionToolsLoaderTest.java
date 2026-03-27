package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;

class FunctionToolsLoaderTest {

	private static final class CountingProvider implements Genai {
		private final AtomicInteger adds = new AtomicInteger();

		@Override
		public void init(Configurator configurator) {
			// not used
		}

		@Override
		public void prompt(String prompt) {
			// not used
		}

		@Override
		public void addFile(java.io.File file) {
			// not used
		}

		@Override
		public void addFile(java.net.URL file) {
			// not used
		}

		@Override
		public java.util.List<Double> embedding(String text, long size) {
			return java.util.Collections.emptyList();
		}

		@Override
		public void clear() {
			// not used
		}

		@Override
		public void addTool(String name, String description, ToolFunction function, String... args) {
			adds.incrementAndGet();
		}

		@Override
		public void instructions(String instructions) {
			// not used
		}

		@Override
		public String perform() {
			return "";
		}

		@Override
		public void inputsLog(java.io.File parent) {
			// not used
		}

		@Override
		public void setWorkingDir(java.io.File projectDir) {
			// not used
		}

		@Override
		public org.machanism.machai.ai.manager.Usage usage() {
			return new org.machanism.machai.ai.manager.Usage(0, 0, 0);
		}
	}

	private static final class NoopConfigurator implements Configurator {
		@Override
		public String get(String key) {
			return null;
		}

		@Override
		public String get(String key, String defaultValue) {
			return defaultValue;
		}

		@Override
		public int getInt(String key) {
			return 0;
		}

		@Override
		public Integer getInt(String key, Integer defaultValue) {
			return defaultValue;
		}

		@Override
		public boolean getBoolean(String key) {
			return false;
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			return defaultValue;
		}

		@Override
		public long getLong(String key) {
			return 0L;
		}

		@Override
		public Long getLong(String key, Long defaultValue) {
			return defaultValue;
		}

		@Override
		public java.io.File getFile(String key) {
			return null;
		}

		@Override
		public java.io.File getFile(String key, java.io.File defaultValue) {
			return defaultValue;
		}

		@Override
		public double getDouble(String key) {
			return 0.0;
		}

		@Override
		public Double getDouble(String key, Double defaultValue) {
			return defaultValue;
		}

		@Override
		public String getName() {
			return "noop";
		}

		@Override
		public void set(String key, String value) {
			// not used
		}
	}

	@AfterEach
	void restoreLoaderState() throws Exception {
		FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
		Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<FunctionTools> tools = (List<FunctionTools>) field.get(loader);
		tools.clear();
	}

	@Test
	void getInstance_returnsSingleton() {
		// Arrange
		FunctionToolsLoader a = FunctionToolsLoader.getInstance();
		FunctionToolsLoader b = FunctionToolsLoader.getInstance();

		// Act + Assert
		assertSame(a, b);
	}

	@Test
	void applyTools_appliesAllDiscoveredToolsInOrder() throws Exception {
		// Arrange
		FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
		CountingProvider provider = new CountingProvider();

		AtomicInteger applied = new AtomicInteger();
		FunctionTools t1 = new FunctionTools() {
			@Override
			public void applyTools(Genai p) {
				applied.incrementAndGet();
				p.addTool("a", "", args -> null);
			}
		};
		FunctionTools t2 = new FunctionTools() {
			@Override
			public void applyTools(Genai p) {
				applied.incrementAndGet();
				p.addTool("b", "", args -> null);
			}
		};

		Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<FunctionTools> list = (List<FunctionTools>) field.get(loader);
		list.add(t1);
		list.add(t2);

		// Act
		loader.applyTools(provider);

		// Assert
		assertEquals(2, applied.get());
		assertEquals(2, provider.adds.get());
	}

	@Test
	void setConfiguration_propagatesConfiguratorToAllTools() throws Exception {
		// Arrange
		FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
		Configurator conf = new NoopConfigurator();

		AtomicInteger configured = new AtomicInteger();
		FunctionTools t1 = new FunctionTools() {
			@Override
			public void applyTools(Genai provider) {
				// not used
			}

			@Override
			public void setConfigurator(Configurator configurator) {
				assertSame(conf, configurator);
				configured.incrementAndGet();
			}
		};
		FunctionTools t2 = new FunctionTools() {
			@Override
			public void applyTools(Genai provider) {
				// not used
			}

			@Override
			public void setConfigurator(Configurator configurator) {
				assertSame(conf, configurator);
				configured.incrementAndGet();
			}
		};

		Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<FunctionTools> list = (List<FunctionTools>) field.get(loader);
		list.add(t1);
		list.add(t2);

		// Act
		loader.setConfiguration(conf);

		// Assert
		assertEquals(2, configured.get());
	}
}
