package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.schema.Bindex;

class BindexFunctionToolsMissingCoverageTest {

	@Test
	void getBindex_shouldReturnNotFoundWhenRepositoryReturnsNull() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexRepository repository = mock(BindexRepository.class);
		when(repository.getBindex("missing")).thenReturn(null);
		setRepository(tools, repository);
		com.fasterxml.jackson.databind.JsonNode props = new com.fasterxml.jackson.databind.ObjectMapper().readTree("{\"id\":\"missing\"}");

		String result = tools.getBindex(new Object[] { props });

		assertEquals("<not found>", result);
	}

	@Test
	void getBindex_shouldReuseExistingRepositoryWithoutConfigurator() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		BindexRepository repository = mock(BindexRepository.class);
		Bindex bindex = new Bindex();
		bindex.setId("lib");
		when(repository.getBindex("lib")).thenReturn(bindex);
		setRepository(tools, repository);
		com.fasterxml.jackson.databind.JsonNode props = new com.fasterxml.jackson.databind.ObjectMapper().readTree("{\"id\":\"lib\"}");

		String result = tools.getBindex(new Object[] { props });

		assertEquals(true, result.contains("\"id\":\"lib\""));
	}

	@Test
	void getBindexSchema_shouldReturnSchemaContent() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();

		String schema = tools.getBindexSchema(new Object[0]);

		assertEquals(true, schema.contains("classification"));
	}

	@Test
	void getRecommendedLibraries_shouldFailWhenConfiguratorMissing() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		com.fasterxml.jackson.databind.JsonNode props = new com.fasterxml.jackson.databind.ObjectMapper().readTree("{\"prompt\":\"need libs\"}");

		assertThrows(NullPointerException.class, () -> tools.getRecommendedLibraries(new Object[] { props }));
	}

	@Test
	void setConfigurator_shouldStoreConfigurator() throws Exception {
		BindexFunctionTools tools = new BindexFunctionTools();
		Configurator configurator = mock(Configurator.class);
		when(configurator.get(any(String.class))).thenReturn("value");

		tools.setConfigurator(configurator);

		Field field = BindexFunctionTools.class.getDeclaredField("configurator");
		field.setAccessible(true);
		assertEquals(configurator, field.get(tools));
	}

	private static void setRepository(BindexFunctionTools tools, BindexRepository repository) throws Exception {
		Field field = BindexFunctionTools.class.getDeclaredField("bindexRepository");
		field.setAccessible(true);
		field.set(tools, repository);
	}
}
