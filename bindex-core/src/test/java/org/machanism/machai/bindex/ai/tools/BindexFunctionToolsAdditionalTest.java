package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Additional tests for {@link BindexFunctionTools} focusing on error handling.
 */
class BindexFunctionToolsAdditionalTest {

	@Test
	void getBindex_shouldThrowIfConfiguratorNotSet() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		ObjectNode args = new ObjectMapper().createObjectNode().put("id", "any");

		// Act + Assert
		Method m = BindexFunctionTools.class.getDeclaredMethod("getBindex", Object[].class);
		m.setAccessible(true);

		Exception ex = assertThrows(Exception.class, () -> m.invoke(tools, (Object) new Object[] { args }));
		assertNotNull(ex.getCause());
		assertTrue(ex.getCause() instanceof IllegalStateException);
		assertTrue(ex.getCause().getMessage().contains("setConfigurator"));
	}
}
