package org.machanism.machai.gw.tools;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.Genai;

class ActFunctionToolsApplyToolsTest {

	@Test
	void applyTools_registersLoadActDetailsTool() {
		// Arrange
		Genai provider = mock(Genai.class);
		ActFunctionTools tools = new ActFunctionTools();

		// Act
		tools.applyTools(provider);

		// Assert
		verify(provider).addTool(
				eq("load_act_details"),
				anyString(),
				any(),
				eq("actName:string:required:The name of the Act to load."),
				eq("custom:boolean:optional:If true, retrieves the Act definition only from the user-defined (custom) acts directory. "
						+ "If false, retrieves only the built-in act. If not specified, retrieves effective user-defined acts."));
	}
}
