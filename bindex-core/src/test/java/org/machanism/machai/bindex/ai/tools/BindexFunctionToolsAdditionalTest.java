package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.Picker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsAdditionalTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void getRecommendedLibraries_shouldPassRepositoryUrlAndConfiguratorToPickerConstructor() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		when(configurator.get(Picker.MODEL_PROP_NAME, null)).thenReturn(null);
		when(configurator.get(BindexFunctionTools.MODEL_PROP_NAME)).thenReturn("fallback-model");
		when(configurator.getDouble(Picker.SCORE_PROP_NAME, Picker.DEFAULT_SCORE_VALUE)).thenReturn(Picker.DEFAULT_SCORE_VALUE);
		when(configurator.get("BINDEX_REPO_URL", null)).thenReturn("mongodb://repo");
		tools.setConfigurator(configurator);
		JsonNode props = OBJECT_MAPPER.readTree("{\"prompt\":\"build a web app\"}");
		final List<List<?>> arguments = new java.util.ArrayList<>();

		try (org.mockito.MockedConstruction<Picker> mocked = org.mockito.Mockito.mockConstruction(Picker.class,
				(mock, context) -> {
					arguments.add(context.arguments());
					when(mock.pick("build a web app")).thenReturn(Collections.emptyList());
				})) {
			// Act
			tools.getRecommendedLibraries(props, new File("."));

			// Assert
			assertNull(arguments.get(0).get(0));
			assertEquals("mongodb://repo", arguments.get(0).get(1));
			assertEquals(configurator, arguments.get(0).get(2));
			assertEquals(1, mocked.constructed().size());
		}
	}

	@Test
	void registerBindex_shouldPassConfiguredModelToPickerConstructor(@TempDir File tempDir) throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		when(configurator.get(BindexFunctionTools.MODEL_PROP_NAME)).thenReturn("gw-model");
		tools.setConfigurator(configurator);
		File bindexFile = new File(tempDir, "bindex.json");
		Files.write(bindexFile.toPath(),
				Collections.singletonList("{\"id\":\"lib-1\",\"name\":\"Library\",\"version\":\"1.0\",\"classification\":{\"domains\":[],\"layers\":[],\"languages\":[],\"integrations\":[]}}"),
				StandardCharsets.UTF_8);
		JsonNode props = OBJECT_MAPPER.readTree("{\"fileName\":\"bindex.json\"}");
		final List<List<?>> arguments = new java.util.ArrayList<>();

		try (org.mockito.MockedConstruction<Picker> mocked = org.mockito.Mockito.mockConstruction(Picker.class,
				(mock, context) -> {
					arguments.add(context.arguments());
					when(mock.create(any())).thenReturn("ObjectId(abc123)");
				})) {
			// Act
			String result = tools.registerBindex(props, tempDir);

			// Assert
			assertEquals("RecordId: ObjectId(abc123)", result);
			assertEquals("gw-model", arguments.get(0).get(0));
			assertNull(arguments.get(0).get(1));
			assertEquals(configurator, arguments.get(0).get(2));
		}
	}
}
