package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsRecommendedLibrariesTest {

	@Test
	void getRecommendedLibraries_buildsResultFromPickerOutput_andFiltersNulls() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get(Picker.MODEL_PROP_NAME, Picker.DEFAULT_MODEL)).thenReturn("model-x");
		Mockito.when(configurator.getDouble(Picker.SCORE_PROP_NAME, Picker.DEFAULT_SCORE_VALUE)).thenReturn(0.42d);
		Mockito.when(configurator.get("BINDEX_REPO_URL", null)).thenReturn("http://repo.example");

		BindexFunctionTools tools = new BindexFunctionTools();
		tools.setConfigurator(configurator);

		Bindex b1 = new Bindex();
		b1.setId("lib-1");
		b1.setDescription("Library One");
		Bindex b2 = new Bindex();
		b2.setId("lib-2");
		b2.setDescription("Library Two");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"prompt\":\"need http client\"}");

		Method method = BindexFunctionTools.class.getDeclaredMethod("getRecommendedLibraries", Object[].class);
		method.setAccessible(true);

		try (MockedConstruction<Picker> construction = Mockito.mockConstruction(Picker.class, (mock, context) -> {
			Mockito.when(mock.pick("need http client")).thenReturn(Arrays.asList(b1, null, b2));
		})) {
			// Act
			@SuppressWarnings("unchecked")
			List<Object> result = (List<Object>) method.invoke(tools, new Object[] { new Object[] { props } });

			// Assert
			assertNotNull(result);
			assertEquals(2, result.size());

			Object first = result.get(0);
			Object second = result.get(1);

			assertEquals("lib-1", invokeGetter(first, "getId"));
			assertEquals("Library One", invokeGetter(first, "getDescription"));
			assertEquals("lib-2", invokeGetter(second, "getId"));
			assertEquals("Library Two", invokeGetter(second, "getDescription"));

			Picker created = construction.constructed().get(0);
			Mockito.verify(created).setScore(0.42d);
			Mockito.verify(created).pick("need http client");
		}
	}

	@Test
	void bindexElement_accessors_and_toString_workAsExpected() {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexFunctionTools.BindexElement element = tools.new BindexElement("id-1", "desc-1");

		// Act
		element.setId("id-2");
		element.setDescription("desc-2");

		// Assert
		assertEquals("id-2", element.getId());
		assertEquals("desc-2", element.getDescription());
		assertEquals("id-2", element.toString());
	}

	private static Object invokeGetter(Object target, String methodName) throws Exception {
		Method m = target.getClass().getDeclaredMethod(methodName);
		m.setAccessible(true);
		return m.invoke(target);
	}
}
