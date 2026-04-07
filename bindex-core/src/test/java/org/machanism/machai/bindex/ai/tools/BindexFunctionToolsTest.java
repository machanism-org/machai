package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.schema.Bindex;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsTest {

	@Test
	void applyTools_registersAllToolsWithExpectedMetadata() {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Genai genai = Mockito.mock(Genai.class);

		// Act
		tools.applyTools(genai);

		// Assert
		Mockito.verify(genai).addTool(
				Mockito.eq("get_bindex"),
				Mockito.eq("Retrieves bindex metadata for a given project or library."),
				Mockito.any(),
				Mockito.eq("id:string:required:The bindex id."));

		Mockito.verify(genai).addTool(
				Mockito.eq("get_bindex_schema"),
				Mockito.eq("Retrieves the schema definition for bindex metadata."),
				Mockito.any());

		Mockito.verify(genai).addTool(
				Mockito.eq("pick_libraries"),
				Mockito.eq("Recommends libraries based on the user's prompt or project requirements."),
				Mockito.any(),
				Mockito.eq("prompt:string:required:The user prompt describing project needs or requirements."));
	}

	@Test
	void getBindex_throwsWhenConfiguratorNotSet() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"id\":\"x\"}");

		// Act + Assert
		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> invokeGetBindex(tools, new Object[] { props }));
		assertNotNull(ex.getCause());
		assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
	}

	@Test
	void getBindex_returnsNullLiteralWhenRepositoryReturnsNull() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = Mockito.mock(Configurator.class);
		tools.setConfigurator(configurator);

		BindexRepository repository = Mockito.mock(BindexRepository.class);
		Mockito.when(repository.getBindex("missing")).thenReturn(null);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"id\":\"missing\"}");

		// Act
		String out = invokeGetBindexWithInjectedRepository(tools, repository, new Object[] { props });

		// Assert
		assertEquals("<not found>", out);
		Mockito.verify(repository).getBindex("missing");
	}

	@Test
	void getBindex_returnsSerializedBindexWhenFound_andAvoidsAbbreviationBranchWhenLogInfoDisabled() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = Mockito.mock(Configurator.class);
		tools.setConfigurator(configurator);

		BindexRepository repository = Mockito.mock(BindexRepository.class);

		Bindex bindex = new Bindex();
		bindex.setId("my-lib");
		bindex.setDescription("My library");
		Mockito.when(repository.getBindex("my-lib")).thenReturn(bindex);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"id\":\"my-lib\"}");

		// Act
		String json = invokeGetBindexWithInjectedRepository(tools, repository, new Object[] { props });

		// Assert
		JsonNode out = mapper.readTree(json);
		assertEquals("my-lib", out.get("id").asText());
		assertEquals("My library", out.get("description").asText());
		Mockito.verify(repository).getBindex("my-lib");
	}

	@Test
	void getBindexRepository_returnsInjectedRepositoryAndReusesSameInstance() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = Mockito.mock(Configurator.class);
		tools.setConfigurator(configurator);

		BindexRepository repository = Mockito.mock(BindexRepository.class);
		injectField(tools, "bindexRepository", repository);

		// Act
		Object first = invokeGetBindexRepository(tools);
		Object second = invokeGetBindexRepository(tools);

		// Assert
		assertNotNull(first);
		assertEquals(repository, first);
		assertEquals(first, second);
	}

	@Test
	void getBindexSchema_returnsSchemaJson_andAvoidsDebugBranchByDefault() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();

		// Act
		String schema = invokeGetBindexSchema(tools);

		// Assert
		assertNotNull(schema);
		assertTrue(schema.trim().startsWith("{"));
	}

	@Test
	void getRecommendedLibraries_throwsWhenConfiguratorNotSet() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"prompt\":\"x\"}");

		// Act + Assert
		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> invokeGetRecommendedLibraries(tools, new Object[] { props }));
		assertNotNull(ex.getCause());
		assertEquals(NullPointerException.class, ex.getCause().getClass());
	}

	private static String invokeGetBindex(BindexFunctionTools tools, Object[] params) throws Exception {
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindex", Object[].class);
		method.setAccessible(true);
		return (String) method.invoke(tools, new Object[] { params });
	}

	private static Object invokeGetBindexRepository(BindexFunctionTools tools) throws Exception {
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindexRepository");
		method.setAccessible(true);
		return method.invoke(tools);
	}

	private static String invokeGetBindexWithInjectedRepository(
			BindexFunctionTools tools,
			BindexRepository repository,
			Object[] params) throws Exception {

		injectField(tools, "bindexRepository", repository);
		return invokeGetBindex(tools, params);
	}

	private static String invokeGetBindexSchema(BindexFunctionTools tools) throws Exception {
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindexSchema", Object[].class);
		method.setAccessible(true);
		return (String) method.invoke(tools, new Object[] { new Object[] {} });
	}

	private static String invokeGetRecommendedLibraries(BindexFunctionTools tools, Object[] params) throws Exception {
		Method method = BindexFunctionTools.class.getDeclaredMethod("getRecommendedLibraries", Object[].class);
		method.setAccessible(true);
		return (String) method.invoke(tools, new Object[] { params });
	}

	private static void injectField(Object target, String fieldName, Object value) throws Exception {
		java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
