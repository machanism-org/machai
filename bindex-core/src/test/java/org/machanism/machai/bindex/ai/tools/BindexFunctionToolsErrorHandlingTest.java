package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests error-handling branches of {@link BindexFunctionTools}.
 */
class BindexFunctionToolsErrorHandlingTest {

	@Test
	void getBindex_shouldThrowIllegalStateExceptionWhenRepositoryNotInitialized() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		Object[] params = new Object[] { mapper.readTree("{\"id\":\"some\"}") };

		Method getBindex = BindexFunctionTools.class.getDeclaredMethod("getBindex", Object[].class);
		getBindex.setAccessible(true);

		// Act
		Exception ex = assertThrows(Exception.class, () -> getBindex.invoke(tools, (Object) params));

		// Assert
		Throwable cause = ex.getCause();
		assertNotNull(cause);
		assertInstanceOf(IllegalStateException.class, cause);
		assertTrue(cause.getMessage().contains("setConfigurator"));
	}
}
