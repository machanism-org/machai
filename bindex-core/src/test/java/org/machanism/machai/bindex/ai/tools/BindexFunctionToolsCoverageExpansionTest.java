package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.schema.Bindex;

class BindexFunctionToolsCoverageExpansionTest {

	@Test
	void getBindex_shouldReturnNullLiteralWhenRepositoryDoesNotContainRecord() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexRepository repository = mock(BindexRepository.class);
		when(repository.getBindex("missing")).thenReturn(null);
		Field field = BindexFunctionTools.class.getDeclaredField("bindexRepository");
		field.setAccessible(true);
		field.set(tools, repository);
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindex", String.class);
		method.setAccessible(true);

		String result = (String) method.invoke(tools, "missing");

		assertEquals("null", result);
	}

	@Test
	void getRecommendedLibraries_shouldConvertBindexesToElementsAndSkipNulls() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		tools.setConfigurator(configurator);
		when(configurator.get(anyString(), anyString())).thenReturn("value");
		when(configurator.getDouble(anyString(), any(Double.class))).thenReturn(0.75D);

		Bindex first = new Bindex();
		first.setId("lib-a");
		first.setDescription("desc-a");
		Bindex second = null;
		Bindex third = new Bindex();
		third.setId("lib-c");
		third.setDescription("desc-c");

		try (org.mockito.MockedConstruction<org.machanism.machai.bindex.Picker> mocked = org.mockito.Mockito
				.mockConstruction(org.machanism.machai.bindex.Picker.class, (picker, context) -> {
					when(picker.pick("need libs")).thenReturn(java.util.Arrays.asList(first, second, third));
				})) {
			Method method = BindexFunctionTools.class.getDeclaredMethod("getRecommendedLibraries", Object[].class);
			method.setAccessible(true);
			com.fasterxml.jackson.databind.JsonNode props = new com.fasterxml.jackson.databind.ObjectMapper()
					.readTree("{\"prompt\":\"need libs\"}");

			@SuppressWarnings("unchecked")
			List<Object> result = (List<Object>) method.invoke(tools, (Object) new Object[] { props });

			assertEquals(2, result.size());
			assertEquals("lib-a", result.get(0).toString());
			assertEquals("lib-c", result.get(1).toString());
		}
	}

	@Test
	void registerBindex_shouldReturnRecordIdWhenFileExists(@TempDir File tempDir) throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		tools.setConfigurator(configurator);
		when(configurator.get("gw.model")).thenReturn("model");
		File file = new File(tempDir, "bindex.json");
		Files.write(file.toPath(), ("{\"id\":\"lib-id\",\"name\":\"lib\",\"version\":\"1.0\"," +
				"\"description\":\"desc\",\"classification\":{\"domains\":[],\"layers\":[],\"languages\":[],\"integrations\":[]}," +
				"\"dependencies\":[]}").getBytes(StandardCharsets.UTF_8));

		try (org.mockito.MockedConstruction<org.machanism.machai.bindex.Picker> mocked = org.mockito.Mockito
				.mockConstruction(org.machanism.machai.bindex.Picker.class, (picker, context) -> {
					when(picker.create(any(Bindex.class))).thenReturn("507f1f77bcf86cd799439011");
				})) {
			Method method = BindexFunctionTools.class.getDeclaredMethod("registerBindex", Object[].class);
			method.setAccessible(true);
			com.fasterxml.jackson.databind.JsonNode props = new com.fasterxml.jackson.databind.ObjectMapper()
					.readTree("{\"fileName\":\"bindex.json\"}");

			String result = (String) method.invoke(tools, (Object) new Object[] { props, tempDir });

			assertEquals("RecordId: 507f1f77bcf86cd799439011", result);
		}
	}

	@Test
	void registerBindex_shouldReturnFileNotFoundWhenMissing(@TempDir File tempDir) throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		tools.setConfigurator(configurator);
		when(configurator.get("gw.model")).thenReturn("model");

		try (org.mockito.MockedConstruction<org.machanism.machai.bindex.Picker> mocked = org.mockito.Mockito
				.mockConstruction(org.machanism.machai.bindex.Picker.class, (picker, context) -> {
				})) {
			Method method = BindexFunctionTools.class.getDeclaredMethod("registerBindex", Object[].class);
			method.setAccessible(true);
			com.fasterxml.jackson.databind.JsonNode props = new com.fasterxml.jackson.databind.ObjectMapper()
					.readTree("{\"fileName\":\"missing.json\"}");

			String result = (String) method.invoke(tools, (Object) new Object[] { props, tempDir });

			assertEquals("file not found", result);
		}
	}

	@Test
	void bindexElement_accessorsAndToString_shouldExposeState() {
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexFunctionTools.BindexElement element = tools.new BindexElement("id-1", "desc-1");

		element.setId("id-2");
		element.setDescription("desc-2");

		assertEquals("id-2", element.getId());
		assertEquals("desc-2", element.getDescription());
		assertEquals("id-2", element.toString());
		assertNotNull(element);
		assertTrue(element.toString().contains("id-2"));
	}
}
