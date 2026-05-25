package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsCoverageTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void getBindex_shouldThrowWhenIdPropertyMissing() {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();

		// Act + Assert
		assertThrows(NullPointerException.class, () -> tools.getBindex(OBJECT_MAPPER.createObjectNode(), new File(".")));
	}

	@Test
	void getBindex_shouldSerializeRepositoryResultWithDescription() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexRepository repository = mock(BindexRepository.class);
		Bindex bindex = new Bindex();
		bindex.setId("id-1");
		bindex.setDescription("desc");
		when(repository.getBindex("id-1")).thenReturn(bindex);
		setField(tools, "bindexRepository", repository);
		JsonNode props = OBJECT_MAPPER.readTree("{\"id\":\"id-1\"}");

		// Act
		String result = tools.getBindex(props, new File("."));

		// Assert
		assertEquals(true, result.contains("\"description\":\"desc\""));
	}

	@Test
	void getBindex_shouldReturnNotFoundMarkerWhenRepositoryReturnsNull() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexRepository repository = mock(BindexRepository.class);
		when(repository.getBindex("missing")).thenReturn(null);
		setField(tools, "bindexRepository", repository);
		JsonNode props = OBJECT_MAPPER.readTree("{\"id\":\"missing\"}");

		// Act
		String result = tools.getBindex(props, new File("."));

		// Assert
		assertEquals("<not found>", result);
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
