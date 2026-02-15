package org.machanism.machai.ai.manager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.tools.SystemFunctionTools;

/**
 * Unit tests for {@link SystemFunctionTools}.
 * <p>
 * Verifies integrated tool attachment to GenAIProvider.
 * 
 * @author Viktor Tovstyi
 * @since 0.0.2
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
