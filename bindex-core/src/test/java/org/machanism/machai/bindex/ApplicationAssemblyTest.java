package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.schema.Bindex;

class ApplicationAssemblyTest {

	@Test
	void constructor_throwsOnNullGenai() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new ApplicationAssembly(null, config, new File(".")));

		// Assert
		assertEquals("genai must not be null", ex.getMessage());
	}

	@Test
	void constructor_throwsOnNullConfig() {
		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new ApplicationAssembly("openai", null, new File(".")));

		// Assert
		assertEquals("config must not be null", ex.getMessage());
	}

	@Test
	void constructor_throwsOnNullDir() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new ApplicationAssembly("openai", config, null));

		// Assert
		assertEquals("dir must not be null", ex.getMessage());
	}

	@Test
	void assembly_throwsOnNullPrompt(@TempDir File tempDir) {
		// Arrange
		CapturingProviderHandler handler = new CapturingProviderHandler();
		Object provider = newProviderProxy(handler);

		try (org.mockito.MockedStatic<GenAIProviderManager> providerManager = org.mockito.Mockito
				.mockStatic(GenAIProviderManager.class);
				org.mockito.MockedStatic<FunctionToolsLoader> loaderStatic = org.mockito.Mockito
						.mockStatic(FunctionToolsLoader.class)) {

			Configurator config = org.mockito.Mockito.mock(Configurator.class);
			providerManager.when(() -> GenAIProviderManager.getProvider("openai", config)).thenReturn(provider);

			FunctionToolsLoader loader = org.mockito.Mockito.mock(FunctionToolsLoader.class);
			loaderStatic.when(FunctionToolsLoader::getInstance).thenReturn(loader);

			ApplicationAssembly assembly = new ApplicationAssembly("openai", config, tempDir);

			// Act
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
					() -> assembly.assembly(null, java.util.Collections.emptyList()));

			// Assert
			assertEquals("prompt must not be null", ex.getMessage());
		}
	}

	@Test
	void assembly_throwsOnNullBindexList(@TempDir File tempDir) {
		// Arrange
		CapturingProviderHandler handler = new CapturingProviderHandler();
		Object provider = newProviderProxy(handler);

		try (org.mockito.MockedStatic<GenAIProviderManager> providerManager = org.mockito.Mockito
				.mockStatic(GenAIProviderManager.class);
				org.mockito.MockedStatic<FunctionToolsLoader> loaderStatic = org.mockito.Mockito
						.mockStatic(FunctionToolsLoader.class)) {

			Configurator config = org.mockito.Mockito.mock(Configurator.class);
			providerManager.when(() -> GenAIProviderManager.getProvider("openai", config)).thenReturn(provider);

			FunctionToolsLoader loader = org.mockito.Mockito.mock(FunctionToolsLoader.class);
			loaderStatic.when(FunctionToolsLoader::getInstance).thenReturn(loader);

			ApplicationAssembly assembly = new ApplicationAssembly("openai", config, tempDir);

			// Act
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
					() -> assembly.assembly("do", null));

			// Assert
			assertEquals("bindexList must not be null", ex.getMessage());
		}
	}

	@Test
	void projectDir_throwsOnNull(@TempDir File tempDir) {
		// Arrange
		CapturingProviderHandler handler = new CapturingProviderHandler();
		Object provider = newProviderProxy(handler);

		try (org.mockito.MockedStatic<GenAIProviderManager> providerManager = org.mockito.Mockito
				.mockStatic(GenAIProviderManager.class);
				org.mockito.MockedStatic<FunctionToolsLoader> loaderStatic = org.mockito.Mockito
						.mockStatic(FunctionToolsLoader.class)) {

			Configurator config = org.mockito.Mockito.mock(Configurator.class);
			providerManager.when(() -> GenAIProviderManager.getProvider("openai", config)).thenReturn(provider);

			FunctionToolsLoader loader = org.mockito.Mockito.mock(FunctionToolsLoader.class);
			loaderStatic.when(FunctionToolsLoader::getInstance).thenReturn(loader);

			ApplicationAssembly assembly = new ApplicationAssembly("openai", config, tempDir);

			// Act
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> assembly.projectDir(null));

			// Assert
			assertEquals("projectDir must not be null", ex.getMessage());
		}
	}

	@Test
	void assembly_buildsExpectedProviderInputs_andWritesLogPathUnderProjectDir(@TempDir File tempDir) throws Exception {
		// Arrange
		CapturingProviderHandler handler = new CapturingProviderHandler();
		Object provider = newProviderProxy(handler);

		try (org.mockito.MockedStatic<GenAIProviderManager> providerManager = org.mockito.Mockito
				.mockStatic(GenAIProviderManager.class);
				org.mockito.MockedStatic<FunctionToolsLoader> loaderStatic = org.mockito.Mockito
						.mockStatic(FunctionToolsLoader.class)) {

			Configurator config = org.mockito.Mockito.mock(Configurator.class);
			providerManager.when(() -> GenAIProviderManager.getProvider("openai", config)).thenReturn(provider);

			FunctionToolsLoader loader = org.mockito.Mockito.mock(FunctionToolsLoader.class);
			loaderStatic.when(FunctionToolsLoader::getInstance).thenReturn(loader);

			ApplicationAssembly assembly = new ApplicationAssembly("openai", config, tempDir).projectDir(tempDir);

			Bindex b1 = new Bindex();
			b1.setId("a:1");
			b1.setDescription("descA");
			Bindex b2 = new Bindex();
			b2.setId("b:2");
			b2.setDescription("descB");
			List<Bindex> bindexes = Arrays.asList(null, b1, b2);

			// Act
			assembly.assembly("Do X", bindexes);

			// Assert
			assertEquals(1, handler.instructionsCalls.size());
			assertNotNull(handler.instructionsCalls.get(0));

			assertEquals(1, handler.promptCalls.size());
			String prompt = handler.promptCalls.get(0);
			assertNotNull(prompt);
			org.junit.jupiter.api.Assertions.assertTrue(prompt.contains("Do X"));
			org.junit.jupiter.api.Assertions.assertTrue(prompt.contains("- `a:1`: `descA`"));
			org.junit.jupiter.api.Assertions.assertTrue(prompt.contains("- `b:2`: `descB`"));

			assertEquals(1, handler.inputsLogCalls.size());
			File expected = new File(tempDir, ".machai/assembly-inputs.txt");
			assertEquals(expected.getPath(), handler.inputsLogCalls.get(0).getPath());

			assertEquals(1, handler.performCalls);
			assertEquals(tempDir.getCanonicalFile(), handler.workingDir.getCanonicalFile());
			Files.createDirectories(expected.getParentFile().toPath());
		}
	}

	private static Object newProviderProxy(CapturingProviderHandler handler) {
		try {
			Class<?> providerInterface = Class.forName("org.machanism.machai.ai.manager.GenAIProvider");
			return Proxy.newProxyInstance(ApplicationAssemblyTest.class.getClassLoader(), new Class<?>[] { providerInterface },
				handler);
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	private static final class CapturingProviderHandler implements InvocationHandler {
		private final List<String> instructionsCalls = new ArrayList<>();
		private final List<String> promptCalls = new ArrayList<>();
		private final List<File> inputsLogCalls = new ArrayList<>();
		private int performCalls;
		private File workingDir;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			switch (method.getName()) {
				case "addTool":
					return null;
				case "instructions":
					instructionsCalls.add((String) args[0]);
					return null;
				case "prompt":
					promptCalls.add((String) args[0]);
					return null;
				case "inputsLog":
					inputsLogCalls.add((File) args[0]);
					return null;
				case "setWorkingDir":
					workingDir = (File) args[0];
					return null;
				case "perform":
					performCalls++;
					return "ok";
				case "embedding":
					int dimensions = (Integer) args[1];
					return java.util.Collections.nCopies(dimensions, 0.0d);
				case "usage":
					// return null or a default instance; not needed by ApplicationAssembly
					return null;
				default:
					// Return default for primitives.
					if (method.getReturnType().isPrimitive()) {
						if (boolean.class.equals(method.getReturnType())) {
							return false;
						}
						return 0;
					}
					return null;
			}
		}
	}
}
