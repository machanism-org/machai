package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsAdditionalTest {

	@Test
	void getBindexSchema_returnsNonEmptySchemaJson() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindexSchema", Object[].class);
		method.setAccessible(true);

		// Act
		String schema = (String) method.invoke(tools, new Object[] { new Object[] {} });

		// Assert
		// Sonar java:S5785 - use assertNotNull instead of null-check in assertTrue.
		assertNotNull(schema);
		// Sonar java:S7158 - use isEmpty() to check if the string is empty.
		assertTrue(!schema.trim().isEmpty());
		assertTrue(schema.trim().startsWith("{"));
	}

	@Test
	void getRecommendedLibraries_returnsMarkdownTable_andOmitsNullEntries() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();

		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get(Picker.MODEL_PROP_NAME, Picker.DEFAULT_MODEL)).thenReturn("model-x");
		Mockito.when(configurator.getDouble("score", Picker.DEFAULT_SCORE_VALUE)).thenReturn(0.12);
		Mockito.when(configurator.get("BINDEX_REPO_URL", null)).thenReturn("http://example.invalid");
		tools.setConfigurator(configurator);

		Bindex b1 = new Bindex();
		b1.setId("a");
		b1.setDescription("desc-a");

		try (MockedConstruction<Picker> mocked = Mockito.mockConstruction(Picker.class, (mock, ctx) -> {
			Mockito.doNothing().when(mock).setScore(Mockito.anyDouble());
			Mockito.when(mock.pick("need libs")).thenReturn(Arrays.asList(null, b1));
		})) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode props = mapper.readTree("{\"prompt\":\"need libs\"}");

			Method method = BindexFunctionTools.class.getDeclaredMethod("getRecommendedLibraries", Object[].class);
			method.setAccessible(true);

			// Act
			String out = (String) method.invoke(tools, new Object[] { new Object[] { props } });

			// Assert
			assertTrue(out.startsWith("```md\n# Recommended Artifacts\n"));
			assertTrue(out.contains("| Library Id | Description |"));
			assertTrue(out.contains("| a | desc-a |"));
			assertEquals(1, mocked.constructed().size());
			Mockito.verify(mocked.constructed().get(0)).setScore(0.12);
			Mockito.verify(mocked.constructed().get(0)).pick("need libs");
		}
	}
}
