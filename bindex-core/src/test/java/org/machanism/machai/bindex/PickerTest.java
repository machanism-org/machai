package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Language;

class PickerTest {

	@Test
	void constructor_throwsOnNullGenai() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Picker(null, null, config));

		// Assert
		assertEquals("genai must not be null", ex.getMessage());
	}

	@Test
	void constructor_throwsOnNullConfig() {
		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Picker("openai", null, null));

		// Assert
		assertEquals("config must not be null", ex.getMessage());
	}

	@Test
	void getNormalizedLanguageName_trimsLowercasesAndStripsParentheses() {
		// Arrange
		Language language = new Language();
		language.setName("  Java (JVM) ");

		// Act
		String normalized = invokeGetNormalizedLanguageName(language);

		// Assert
		assertEquals("java", normalized);
	}

	@Test
	void addDependencies_addsTransitiveDependenciesOnlyOnce() throws Exception {
		// Arrange
		Picker picker = newUninitializedPickerForTest();

		Bindex a = new Bindex();
		a.setId("a");
		a.setDependencies(java.util.Arrays.asList("b", "c"));
		Bindex b = new Bindex();
		b.setId("b");
		b.setDependencies(Collections.singletonList("c"));
		Bindex c = new Bindex();
		c.setId("c");
		c.setDependencies(Collections.emptyList());

		Picker spy = org.mockito.Mockito.spy(picker);
		org.mockito.Mockito.doAnswer(inv -> {
			String id = inv.getArgument(0);
			switch (id) {
				case "a":
					return a;
				case "b":
					return b;
				case "c":
					return c;
				default:
					return null;
			}
		}).when(spy).getBindex(org.mockito.Mockito.anyString());

		Set<String> deps = new HashSet<>();

		// Act
		Method addDependencies = Picker.class.getDeclaredMethod("addDependencies", Set.class, String.class);
		addDependencies.setAccessible(true);
		addDependencies.invoke(spy, deps, "a");

		// Assert
		assertEquals(new HashSet<>(java.util.Arrays.asList("a", "b", "c")), deps);
	}

	@Test
	void getBindex_throwsOnNullId() throws Exception {
		// Arrange
		Picker picker = newUninitializedPickerForTest();

		// Act
		Method getBindex = Picker.class.getDeclaredMethod("getBindex", String.class);
		getBindex.setAccessible(true);

		java.lang.reflect.InvocationTargetException ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
				() -> getBindex.invoke(picker, new Object[] { null }));

		// Assert
		assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
		assertEquals("id must not be null", ex.getCause().getMessage());
	}

	@Test
	void close_closesMongoClient() throws Exception {
		// Arrange
		Picker picker = newUninitializedPickerForTest();
		com.mongodb.client.MongoClient mongoClient = org.mockito.Mockito.mock(com.mongodb.client.MongoClient.class);

		Field mongoClientField = Picker.class.getDeclaredField("mongoClient");
		mongoClientField.setAccessible(true);
		mongoClientField.set(picker, mongoClient);

		// Act
		picker.close();

		// Assert
		org.mockito.Mockito.verify(mongoClient).close();
	}

	private static String invokeGetNormalizedLanguageName(Language language) {
		try {
			Method method = Picker.class.getDeclaredMethod("getNormalizedLanguageName", Language.class);
			method.setAccessible(true);
			return (String) method.invoke(null, language);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private static Picker newUninitializedPickerForTest() {
		try (org.mockito.MockedStatic<GenAIProviderManager> providerManager = org.mockito.Mockito
				.mockStatic(GenAIProviderManager.class);
				org.mockito.MockedStatic<FunctionToolsLoader> loaderStatic = org.mockito.Mockito
						.mockStatic(FunctionToolsLoader.class)) {

			Configurator config = org.mockito.Mockito.mock(Configurator.class);
			GenAIProvider provider = org.mockito.Mockito.mock(GenAIProvider.class);
			providerManager.when(() -> GenAIProviderManager.getProvider("openai", config)).thenReturn(provider);
			FunctionToolsLoader loader = org.mockito.Mockito.mock(FunctionToolsLoader.class);
			loaderStatic.when(FunctionToolsLoader::getInstance).thenReturn(loader);

			return new Picker("openai", "mongodb://localhost:27017", config);
		}
	}
}
