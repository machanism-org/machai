package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.openai.models.responses.ResponseReasoningItem;

/** Covers isolated response helper edge cases without network calls. */
class ProviderPrivateLogicCoverageTest {

    @Test
    void firstNonBlankReasoningReturnsFirstUsefulFragmentOrNull() throws Exception {
        // Arrange
        OpenAIProvider provider = new OpenAIProvider();
        ResponseReasoningItem.Content blank = org.mockito.Mockito.mock(ResponseReasoningItem.Content.class);
        ResponseReasoningItem.Content useful = org.mockito.Mockito.mock(ResponseReasoningItem.Content.class);
        org.mockito.Mockito.when(blank.text()).thenReturn("  ");
        org.mockito.Mockito.when(useful.text()).thenReturn("reasoning");
        Method method = OpenAIProvider.class.getDeclaredMethod("firstNonBlankReasoning", java.util.List.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(provider, Arrays.asList(blank, useful));
        String noResult = (String) method.invoke(provider, Collections.singletonList(blank));

        // Assert
        assertEquals("reasoning", result);
        assertNull(noResult);
    }

    @Test
    void codeMieUrlEncodingEncodesSpacesAndReservedCharacters() throws Exception {
        // Arrange
        Method method = CodeMieProvider.class.getDeclaredMethod("urlEncode", String.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(null, "a b+&");

        // Assert
        assertEquals("a+b%2B%26", result);
    }
}
