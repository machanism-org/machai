package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.machai.bindex.BindexRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsHappyPathTest {

	@Test
	void getBindex_returnsJsonNullLiteral_whenRepositoryReturnsNull() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();

		// No mocking: we can still cover the happy-path branch in BindexFunctionTools
		// by letting repository return null.
		BindexRepository repository = new BindexRepository(org.mockito.Mockito.mock(
				org.machanism.macha.core.commons.configurator.Configurator.class,
				org.mockito.Mockito.RETURNS_DEFAULTS));

		Field repositoryField = BindexFunctionTools.class.getDeclaredField("bindexRepository");
		repositoryField.setAccessible(true);
		repositoryField.set(tools, repository);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{\"id\":\"missing\"}");
		Method getBindex = BindexFunctionTools.class.getDeclaredMethod("getBindex", Object[].class);
		getBindex.setAccessible(true);

		// Act
		String json = (String) getBindex.invoke(tools, (Object) new Object[] { props });

		// Assert
		assertEquals("null", json);
	}

	@Test
	void getBindex_throwsNullPointerException_whenIdPropertyMissing() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexRepository repository = new BindexRepository(org.mockito.Mockito.mock(
				org.machanism.macha.core.commons.configurator.Configurator.class,
				org.mockito.Mockito.RETURNS_DEFAULTS));

		Field repositoryField = BindexFunctionTools.class.getDeclaredField("bindexRepository");
		repositoryField.setAccessible(true);
		repositoryField.set(tools, repository);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode props = mapper.readTree("{}");
		Method getBindex = BindexFunctionTools.class.getDeclaredMethod("getBindex", Object[].class);
		getBindex.setAccessible(true);

		// Act
		Exception ex = assertThrows(Exception.class, () -> getBindex.invoke(tools, (Object) new Object[] { props }));

		// Assert
		assertNotNull(ex.getCause());
		assertEquals(NullPointerException.class, ex.getCause().getClass());
	}
}
