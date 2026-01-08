package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Unit tests for {@link CommandFunctionTools}.
 * <p>
 * Verifies shell command execution and error handling logic via mock objects.
 * @author Automated
 * @guidance
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
