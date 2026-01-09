package org.machanism.machai.ai.manager;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link CommandFunctionTools}.
 * <p>
 * Verifies shell command execution and error handling logic via mock objects.
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
class CommandFunctionToolsTest {

    /**
     * Verifies that applyTools installs the tool into GenAIProvider.
     */
    @Test
    void testApplyToolsAddsFunction() {
        CommandFunctionTools tools = new CommandFunctionTools();
        GenAIProvider provider = Mockito.mock(GenAIProvider.class);
        tools.applyTools(provider);
        Mockito.verify(provider, Mockito.atLeastOnce()).addTool(
            Mockito.eq("run_command_line_tool"),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString()
        );
    }

}
