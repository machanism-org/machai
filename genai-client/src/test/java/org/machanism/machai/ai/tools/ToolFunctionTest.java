package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ToolFunctionTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void apply_whenInvoked_passesParametersAndWorkingDirectoryToImplementation() throws Exception {
		// Arrange
		JsonNode params = OBJECT_MAPPER.readTree("{\"name\":\"value\"}");
		File workingDir = new File(".");
		Object expected = new Object();
		JsonNode[] capturedParams = new JsonNode[1];
		File[] capturedWorkingDir = new File[1];
		ToolFunction function = (actualParams, actualWorkingDir) -> {
			capturedParams[0] = actualParams;
			capturedWorkingDir[0] = actualWorkingDir;
			return expected;
		};

		// Act
		Object result = function.apply(params, workingDir);

		// Assert
		assertSame(expected, result);
		assertSame(params, capturedParams[0]);
		assertSame(workingDir, capturedWorkingDir[0]);
		assertEquals("value", capturedParams[0].get("name").asText());
	}
}
