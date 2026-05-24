package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;

class FunctionToolsLoaderTest {

	private static final class CountingProvider implements Genai {
		public CountingProvider() {
			super();
		}

		private final AtomicInteger adds = new AtomicInteger();

		@Override
		public void init(String model, Configurator configurator) {
			// not used
		}

		@Override
		public void prompt(String prompt) {
			// not used
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

	@AfterEach
	void restoreLoaderState() throws Exception {
		FunctionToolsLoader loader = new FunctionToolsLoader();
		Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<FunctionTools> tools = (List<FunctionTools>) field.get(loader);
		tools.clear();
	}

	@Test
	void applyTools_withNoDiscoveredTools_doesNothing() {
		// Arrange
		FunctionToolsLoader loader = new FunctionToolsLoader();
		CountingProvider provider = new CountingProvider();

		// Act
		loader.applyTools(provider, null);

		// Assert
		assertEquals(0, provider.adds.get());
	}

}
