package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void applyTools_registersAllExpectedTools() {
		BindexFunctionTools tools = new BindexFunctionTools();
		Genai provider = mock(Genai.class);

		tools.applyTools(provider);

		verify(provider).addTool(eq("get_bindex"), any(String.class), any(), eq("id:string:required:The bindex id."));
		verify(provider).addTool(eq("get_bindex_schema"), any(String.class), any());
		verify(provider).addTool(eq("pick_libraries"), any(String.class), any(),
				eq("prompt:string:required:The user prompt describing project needs or requirements."));
		verify(provider).addTool(eq("register_bindex"), any(String.class), any(),
				eq("fileName:string:required:The name of the Bindex file to register (must exist in the working directory)."));
	}

	@Test
	void getBindex_returnsSerializedJsonWhenRepositoryFindsBindex() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexRepository repository = mock(BindexRepository.class);
		Bindex bindex = new Bindex();
		bindex.setId("lib-1");
		bindex.setName("Library");
		when(repository.getBindex("lib-1")).thenReturn(bindex);
		setField(tools, "bindexRepository", repository);
		JsonNode props = OBJECT_MAPPER.readTree("{\"id\":\"lib-1\"}");

		String result = tools.getBindex(props, new File("."));

		assertTrue(result.contains("\"id\":\"lib-1\""));
		assertTrue(result.contains("\"name\":\"Library\""));
	}

	@Test
	void getBindex_returnsNotFoundMarkerWhenRepositoryDoesNotFindBindex() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexRepository repository = mock(BindexRepository.class);
		when(repository.getBindex("missing")).thenReturn(null);
		setField(tools, "bindexRepository", repository);
		JsonNode props = OBJECT_MAPPER.readTree("{\"id\":\"missing\"}");

		String result = tools.getBindex(props, new File("."));

		assertEquals("<not found>", result);
	}

	@Test
	void getBindexSchema_returnsSchemaContent() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();

		String schema = tools.getBindexSchema(OBJECT_MAPPER.createObjectNode(), new File("."));

		assertTrue(schema.contains("classification"));
		assertTrue(schema.contains("properties"));
	}

	@Test
	void setConfigurator_storesConfiguratorReference() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);

		tools.setConfigurator(configurator);

		assertSame(configurator, getField(tools, "configurator"));
	}

	@Test
	void bindexElement_accessorsAndToString_workAsExpected() {
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexFunctionTools.BindexElement element = tools.new BindexElement("id-1", "desc-1");

		element.setId("id-2");
		element.setDescription("desc-2");

		assertEquals("id-2", element.getId());
		assertEquals("desc-2", element.getDescription());
		assertEquals("id-2", element.toString());
	}

	@Test
	void getRecommendedLibraries_returnsMappedElementsFromPickerResults() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		when(configurator.get(Picker.MODEL_PROP_NAME, null)).thenReturn("picker-model");
		when(configurator.get(BindexFunctionTools.MODEL_PROP_NAME)).thenReturn("fallback-model");
		when(configurator.getDouble(Picker.SCORE_PROP_NAME, Picker.DEFAULT_SCORE_VALUE)).thenReturn(0.91d);
		when(configurator.get("BINDEX_REPO_URL", null)).thenReturn("mongodb://repo");
		tools.setConfigurator(configurator);
		JsonNode props = OBJECT_MAPPER.readTree("{\"prompt\":\"build a web app\"}");

		Bindex b1 = new Bindex();
		b1.setId("lib-a");
		b1.setDescription("Library A");
		Bindex b2 = new Bindex();
		b2.setId("lib-b");
		b2.setDescription("Library B");

		try (org.mockito.MockedConstruction<Picker> mocked = org.mockito.Mockito.mockConstruction(Picker.class,
				(mock, context) -> when(mock.pick("build a web app")).thenReturn(Arrays.asList(b1, null, b2)))) {
			List<BindexFunctionTools.BindexElement> result = tools.getRecommendedLibraries(props, new File("."));

			assertEquals(2, result.size());
			assertEquals("lib-a", result.get(0).getId());
			assertEquals("Library A", result.get(0).getDescription());
			assertEquals("lib-b", result.get(1).getId());
			assertEquals("Library B", result.get(1).getDescription());
			Picker picker = mocked.constructed().get(0);
			verify(picker).setScore(0.91d);
			verify(picker).pick("build a web app");
		}
	}

	@Test
	void registerBindex_returnsRecordIdWhenFileExistsAndCreationSucceeds(@TempDir File tempDir) throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		when(configurator.get(BindexFunctionTools.MODEL_PROP_NAME)).thenReturn("gw-model");
		tools.setConfigurator(configurator);
		File bindexFile = new File(tempDir, "bindex.json");
		Files.write(bindexFile.toPath(),
				Collections.singletonList("{\"id\":\"lib-1\",\"name\":\"Library\",\"version\":\"1.0\"}"),
				StandardCharsets.UTF_8);
		JsonNode props = OBJECT_MAPPER.readTree("{\"fileName\":\"bindex.json\"}");

		try (org.mockito.MockedConstruction<Picker> mocked = org.mockito.Mockito.mockConstruction(Picker.class,
				(mock, context) -> when(mock.create(any(Bindex.class))).thenReturn("ObjectId(abc123)"))) {
			String result = tools.registerBindex(props, tempDir);

			assertEquals("RecordId: ObjectId(abc123)", result);
			verify(mocked.constructed().get(0)).create(any(Bindex.class));
		}
	}

	@Test
	void registerBindex_returnsFileNotFoundWhenRequestedFileDoesNotExist(@TempDir File tempDir) throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		when(configurator.get(BindexFunctionTools.MODEL_PROP_NAME)).thenReturn("gw-model");
		tools.setConfigurator(configurator);
		JsonNode props = OBJECT_MAPPER.readTree("{\"fileName\":\"missing.json\"}");

		try (org.mockito.MockedConstruction<Picker> mocked = org.mockito.Mockito.mockConstruction(Picker.class)) {
			String result = tools.registerBindex(props, tempDir);

			assertEquals("file not found", result);
			assertEquals(1, mocked.constructed().size());
		}
	}

	@Test
	void registerBindex_returnsErrorWhenReadingFileFails(@TempDir File tempDir) throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		when(configurator.get(BindexFunctionTools.MODEL_PROP_NAME)).thenReturn("gw-model");
		tools.setConfigurator(configurator);
		File bindexFile = new File(tempDir, "invalid.json");
		Files.write(bindexFile.toPath(), Collections.singletonList("not-json"), StandardCharsets.UTF_8);
		JsonNode props = OBJECT_MAPPER.readTree("{\"fileName\":\"invalid.json\"}");

		try (org.mockito.MockedConstruction<Picker> mocked = org.mockito.Mockito.mockConstruction(Picker.class)) {
			String result = tools.registerBindex(props, tempDir);

			assertTrue(result.startsWith("Error: "));
			assertEquals(1, mocked.constructed().size());
		}
	}

	@Test
	void getBindexRepository_createsRepositoryLazilyAndCachesIt() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		tools.setConfigurator(configurator);
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindexRepository");
		method.setAccessible(true);

		try (org.mockito.MockedConstruction<BindexRepository> mocked = org.mockito.Mockito.mockConstruction(BindexRepository.class)) {
			Object repository1 = method.invoke(tools);
			Object repository2 = method.invoke(tools);

			assertNotNull(repository1);
			assertSame(repository1, repository2);
			assertEquals(1, mocked.constructed().size());
		}
	}

	private static Object getField(Object target, String fieldName) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(target);
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
