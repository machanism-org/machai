package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.schema.Bindex;

class ApplicationAssemblyTest {

	@Test
	void constructor_throwsOnNullGenai() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);
		File dir = new File(".");

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new ApplicationAssembly(null, config, dir));

		// Assert
		assertEquals("genai must not be null", ex.getMessage());
	}

	@Test
	void constructor_throwsOnNullConfig() {
		// Arrange
		File dir = new File(".");

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new ApplicationAssembly("openai", null, dir));

		// Assert
		assertEquals("config must not be null", ex.getMessage());
	}

	@Test
	void constructor_throwsOnNullDir() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new ApplicationAssembly("openai", config, null));

		// Assert
		assertEquals("dir must not be null", ex.getMessage());
	}

	@Test
	void assembly_throwsOnNullPrompt(@TempDir File tempDir) {
		// Arrange
		CapturingProviderHandler handler = new CapturingProviderHandler();
		Object provider = newProviderProxy(handler);

		try (org.mockito.MockedStatic<GenAIProviderManager> providerManager = org.mockito.Mockito
				.mockStatic(GenAIProviderManager.class)) {

			Configurator config = org.mockito.Mockito.mock(Configurator.class);
			providerManager.when(() -> GenAIProviderManager.getProvider("openai", config)).thenReturn(provider);

			final ApplicationAssembly assembly = new ApplicationAssembly("openai", config, tempDir);
			final String prompt = null;
			final List<Bindex> bindexes = java.util.Collections.emptyList();

			// Act
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> assembly.assembly(prompt, bindexes));

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
				.mockStatic(GenAIProviderManager.class)) {

			Configurator config = org.mockito.Mockito.mock(Configurator.class);
			providerManager.when(() -> GenAIProviderManager.getProvider("openai", config)).thenReturn(provider);

			final ApplicationAssembly assembly = new ApplicationAssembly("openai", config, tempDir);
			String promptText = "do";
			List<Bindex> bindexes = null;

			// Act
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> assembly.assembly(promptText, bindexes));

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
				.mockStatic(GenAIProviderManager.class)) {

			Configurator config = org.mockito.Mockito.mock(Configurator.class);
			providerManager.when(() -> GenAIProviderManager.getProvider("openai", config)).thenReturn(provider);

			final ApplicationAssembly assembly = new ApplicationAssembly("openai", config, tempDir);
			File nullDir = null;

			// Act
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> assembly.projectDir(nullDir));

			// Assert
			assertEquals("projectDir must not be null", ex.getMessage());
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
					return null;
				default:
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
