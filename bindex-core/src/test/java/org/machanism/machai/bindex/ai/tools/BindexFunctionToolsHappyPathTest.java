package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.BindexRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BindexFunctionToolsHappyPathTest {

	@Test
	void getBindex_returnsJsonNullLiteral_whenRepositoryReturnsNull() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();

		BindexRepository repository = new BindexRepository(org.mockito.Mockito.mock(
				Configurator.class,
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
	void getBindexSchema_returnsValidJsonObject() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Method getBindexSchema = BindexFunctionTools.class.getDeclaredMethod("getBindexSchema", Object[].class);
		getBindexSchema.setAccessible(true);

		// Act
		String schema = (String) getBindexSchema.invoke(tools, (Object) new Object[] {});

		// Assert
		assertNotNull(schema);
		new ObjectMapper().readTree(schema);
	}

	@Test
	void getBindexSchema_canBeReadViaClassResourceUrl() throws Exception {
		// Arrange
		String resourcePath = org.machanism.machai.bindex.builder.BindexBuilder.BINDEX_SCHEMA_RESOURCE;

		// Act
		java.net.URL url = org.machanism.machai.schema.Bindex.class.getResource(resourcePath);

		// Assert
		assertNotNull(url);
		assertNotNull(url.toString());
		assertNotNull(new File(url.toURI()));
	}
}
