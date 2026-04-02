package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class BindexFunctionToolsAdditionalTest {

	@Test
	void getBindexSchema_shouldReturnSchemaText_whenResourcePresent() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Method m = BindexFunctionTools.class.getDeclaredMethod("getBindexSchema", Object[].class);
		m.setAccessible(true);

		// Act
		String schema = (String) m.invoke(tools, (Object) new Object[0]);

		// Assert
		assertNotNull(schema);
		org.junit.jupiter.api.Assertions.assertTrue(schema.contains("$schema") || schema.contains("title"));
	}

	@Test
	void getBindexRepository_shouldThrow_whenConfiguratorNotSet() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Method m = BindexFunctionTools.class.getDeclaredMethod("getBindexRepository");
		m.setAccessible(true);

		// Act + Assert
		assertThrows(java.lang.reflect.InvocationTargetException.class, () -> m.invoke(tools));
	}

	@Test
	void getBindexRepository_shouldReturnRepository_whenConfiguratorSet() throws Exception {
		// Arrange
		Configurator cfg = mock(Configurator.class);
		org.mockito.Mockito.when(cfg.get(org.mockito.Mockito.eq("BINDEX_REPO_URL"), org.mockito.Mockito.anyString()))
				.thenReturn("mongodb://localhost");
		org.mockito.Mockito.when(cfg.get(org.mockito.Mockito.eq("BINDEX_REG_PASSWORD"), org.mockito.Mockito.isNull()))
				.thenReturn(null);

		BindexFunctionTools tools = new BindexFunctionTools();
		tools.setConfigurator(cfg);

		try (org.mockito.MockedStatic<com.mongodb.client.MongoClients> mongoClients = org.mockito.Mockito
				.mockStatic(com.mongodb.client.MongoClients.class)) {
			com.mongodb.client.MongoClient client = mock(com.mongodb.client.MongoClient.class);
			com.mongodb.client.MongoDatabase db = mock(com.mongodb.client.MongoDatabase.class);
			org.mockito.Mockito.when(client.getDatabase(org.mockito.Mockito.anyString())).thenReturn(db);
			org.mockito.Mockito.when(db.getCollection(org.mockito.Mockito.anyString()))
					.thenReturn(mock(com.mongodb.client.MongoCollection.class));

			mongoClients.when(() -> com.mongodb.client.MongoClients.create(org.mockito.Mockito.anyString())).thenReturn(client);

			Method m = BindexFunctionTools.class.getDeclaredMethod("getBindexRepository");
			m.setAccessible(true);

			// Act
			Object repo = m.invoke(tools);

			// Assert
			assertNotNull(repo);
		}
	}
}
