package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.*;

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
		java.lang.reflect.Method m = BindexFunctionTools.class.getDeclaredMethod("getBindex", Object[].class);
		m.setAccessible(true);

		Exception ex = assertThrows(Exception.class, () -> m.invoke(tools, (Object) new Object[] { args }));
		assertTrue(ex.getCause() instanceof IllegalStateException);
		assertTrue(ex.getCause().getMessage().contains("setConfigurator"));
	}
}
