package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

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
		assertNotNull(schema);
		assertTrue(!schema.trim().isEmpty());
		assertTrue(schema.trim().startsWith("{"));
	}
}
