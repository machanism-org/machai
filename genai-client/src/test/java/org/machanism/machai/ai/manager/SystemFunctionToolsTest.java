package org.machanism.machai.ai.manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SystemFunctionTools}.
 * <p>
 * Verifies integrated tool attachment to GenAIProvider.
 * @author Automated
 * @guidance
 */
class SystemFunctionToolsTest {

    /**
     * Tests the tools attachment to the provider.
     */
    @Test
    void testApplyTools() {
        SystemFunctionTools systemTools = new SystemFunctionTools();
        GenAIProvider provider = mock(GenAIProvider.class);
        systemTools.applyTools(provider);
        verify(provider, atLeastOnce()).addTool(anyString(), anyString(), any(), anyString());
    }
}
