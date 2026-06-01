package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;

class FunctionToolsLoaderTest {

	private static final class CountingProvider implements Genai {

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
	}

	public static final class RecordingFunctionTools implements FunctionTools {

		static final AtomicInteger constructorCalls = new AtomicInteger();
		static final AtomicInteger configuratorCalls = new AtomicInteger();
		static final AtomicInteger applyCalls = new AtomicInteger();
		static volatile Configurator lastConfigurator;
		static volatile Genai lastProvider;

		public RecordingFunctionTools() {
			constructorCalls.incrementAndGet();
		}

		static void reset() {
			constructorCalls.set(0);
			configuratorCalls.set(0);
			applyCalls.set(0);
			lastConfigurator = null;
			lastProvider = null;
		}

		@Override
		public void applyTools(Genai provider) {
			applyCalls.incrementAndGet();
			lastProvider = provider;
		}

		@Override
		public void setConfigurator(Configurator configurator) {
			configuratorCalls.incrementAndGet();
			lastConfigurator = configurator;
		}
	}

	public static final class NoDefaultConstructorFunctionTools implements FunctionTools {

		public NoDefaultConstructorFunctionTools(String value) {
			// constructor intentionally requires an argument
		}

		@Override
		public void applyTools(Genai provider) {
			// not used
		}
	}

	@AfterEach
	void resetState() {
		RecordingFunctionTools.reset();
	}

	@Test
	void applyTools_withNoDiscoveredTools_doesNothing() throws Exception {
		// Arrange
		FunctionToolsLoader loader = new FunctionToolsLoader();
		setFunctionTools(loader);
		CountingProvider provider = new CountingProvider();

		// Act
		loader.applyTools(provider, null);

		// Assert
		assertEquals(0, provider.adds.get());
	}

	@Test
	void applyTools_withDiscoveredTool_createsFreshInstanceConfiguresAndAppliesIt() throws Exception {
		// Arrange
		FunctionToolsLoader loader = new FunctionToolsLoader();
		RecordingFunctionTools discoveredInstance = new RecordingFunctionTools();
		setFunctionTools(loader, discoveredInstance);
		CountingProvider provider = new CountingProvider();
		Configurator configurator = null;
		RecordingFunctionTools.reset();

		// Act
		loader.applyTools(provider, configurator);

		// Assert
		assertEquals(1, RecordingFunctionTools.constructorCalls.get());
		assertEquals(1, RecordingFunctionTools.configuratorCalls.get());
		assertEquals(1, RecordingFunctionTools.applyCalls.get());
		assertSame(provider, RecordingFunctionTools.lastProvider);
		assertNull(RecordingFunctionTools.lastConfigurator);
		assertNotNull(discoveredInstance);
		assertEquals(0, provider.adds.get());
	}

	@Test
	void applyTools_withToolThatCannotBeInstantiated_wrapsFailureInIllegalArgumentException() throws Exception {
		// Arrange
		FunctionToolsLoader loader = new FunctionToolsLoader();
		FunctionTools inaccessibleTool = instantiateWithoutDefaultConstructor();
		setFunctionTools(loader, inaccessibleTool);
		CountingProvider provider = new CountingProvider();

		// Act
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> loader.applyTools(provider, null));

		// Assert
		assertEquals(
				"FunctionTools class initialization failed: class org.machanism.machai.ai.tools.FunctionToolsLoaderTest$NoDefaultConstructorFunctionTools",
				exception.getMessage());
		assertNotNull(exception.getCause());
	}

	private static void setFunctionTools(FunctionToolsLoader loader, FunctionTools... tools) throws Exception {
		Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<FunctionTools> functionTools = (List<FunctionTools>) field.get(loader);
		functionTools.clear();
		for (FunctionTools tool : tools) {
			functionTools.add(tool);
		}
	}

	private static FunctionTools instantiateWithoutDefaultConstructor() throws Exception {
		Constructor<NoDefaultConstructorFunctionTools> constructor = NoDefaultConstructorFunctionTools.class
				.getDeclaredConstructor(String.class);
			constructor.setAccessible(true);
			return constructor.newInstance("value");
	}
}
