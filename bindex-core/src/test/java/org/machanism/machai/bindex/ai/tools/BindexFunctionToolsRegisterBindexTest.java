package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.bindex.Picker;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsRegisterBindexTest {

	@TempDir
	File tempDir;

	@Test
	void registerBindex_returnsFileNotFound_whenFileDoesNotExist() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get("gw.model")).thenReturn("model-1");

		BindexFunctionTools tools = new BindexFunctionTools();
		tools.setConfigurator(configurator);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"fileName\":\"missing.json\"}");

		Method method = BindexFunctionTools.class.getDeclaredMethod("registerBindex", Object[].class);
		method.setAccessible(true);

		try (MockedConstruction<Picker> construction = Mockito.mockConstruction(Picker.class, (mock, context) -> {
			Mockito.when(mock.create(Mockito.any())).thenReturn("RID-IGNORED");
		})) {
			// Act
			String result = (String) method.invoke(tools, new Object[] { new Object[] { props, tempDir } });

			// Assert
			assertEquals("file not found", result);
			assertEquals(1, construction.constructed().size());
		}
	}

	@Test
	void registerBindex_returnsRecordId_whenFileExistsAndIsValidJson() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get("gw.model")).thenReturn("model-1");

		BindexFunctionTools tools = new BindexFunctionTools();
		tools.setConfigurator(configurator);

		File file = new File(tempDir, "bindex.json");
		String json = "{\"id\":\"lib\",\"description\":\"desc\"}";
		Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));

		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"fileName\":\"bindex.json\"}");

		Method method = BindexFunctionTools.class.getDeclaredMethod("registerBindex", Object[].class);
		method.setAccessible(true);

		Genai genai = Mockito.mock(Genai.class);

		try (MockedConstruction<Picker> construction = Mockito.mockConstruction(Picker.class, (mock, context) -> {
			Mockito.when(mock.create(Mockito.any())).thenReturn("RID-123");
		})) {
			// Act
			String result = (String) method.invoke(tools, new Object[] { new Object[] { props, tempDir } });

			// Assert
			assertEquals("RecordId: RID-123", result);
			Picker created = construction.constructed().get(0);
			Mockito.verify(created).create(Mockito.any());
			Mockito.verifyNoInteractions(genai);
		}
	}

	@Test
	void registerBindex_returnsErrorMessage_whenFileExistsButInvalidJson() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get("gw.model")).thenReturn("model-1");

		BindexFunctionTools tools = new BindexFunctionTools();
		tools.setConfigurator(configurator);

		File file = new File(tempDir, "bindex.json");
		Files.write(file.toPath(), "{not-json".getBytes(StandardCharsets.UTF_8));

		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"fileName\":\"bindex.json\"}");

		Method method = BindexFunctionTools.class.getDeclaredMethod("registerBindex", Object[].class);
		method.setAccessible(true);

		try (MockedConstruction<Picker> construction = Mockito.mockConstruction(Picker.class)) {
			// Act
			String result = (String) method.invoke(tools, new Object[] { new Object[] { props, tempDir } });

			// Assert
			assertEquals(true, result.startsWith("Error: "));
			assertEquals(1, construction.constructed().size());
		}
	}
}
