package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.UsageStatistics;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.beta.messages.BetaContentBlock;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaTextBlock;
import com.anthropic.models.beta.messages.BetaUsage;
import com.anthropic.models.beta.messages.MessageCreateParams;

/** Verifies the Anthropic request path using an isolated SDK client. */
class AnthropicProviderResponseFlowTest {

	@Test
	void performReturnsTextAddsAssistantMessageAndCapturesUsage() {
		// Arrange
		UsageStatistics.init();
		AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);
		BetaMessage response = textResponse("completed answer", 11L, 2L, 3L, 5L);
		when(client.beta().messages().create(any(MessageCreateParams.class))).thenReturn(response);
		StubAnthropicProvider provider = new StubAnthropicProvider(client);
		provider.init("claude-test", TestConfigurators.mapBacked());
		provider.prompt("question");

		// Act
		String result = provider.perform();

		// Assert
		assertEquals("completed answer", result);
		assertEquals(2, provider.inputCount());
		assertEquals(1, UsageStatistics.getUsageForModel("claude-test").size());
		assertEquals(11L, UsageStatistics.getUsageForModel("claude-test").get(0).getInputTokens());
		assertEquals(5L, UsageStatistics.getUsageForModel("claude-test").get(0).getOutputTokens());
		verify(client.beta().messages()).create(any(MessageCreateParams.class));
	}

	@Test
	void performAcceptsResponseWithoutUsageWhenSdkMarksItInvalid() {
		// Arrange
		UsageStatistics.init();
		AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);
		BetaMessage response = textResponse("answer", 0L, 0L, 0L, 0L);
		when(response.isValid()).thenReturn(false);
		when(client.beta().messages().create(any(MessageCreateParams.class))).thenReturn(response);
		StubAnthropicProvider provider = new StubAnthropicProvider(client);
		provider.init("claude-invalid", TestConfigurators.mapBacked());

		// Act
		String result = provider.perform();

		// Assert
		assertEquals("answer", result);
		assertTrue(UsageStatistics.getUsageForModel("claude-invalid").isEmpty());
		verify(client.beta().messages(), times(1)).create(any(MessageCreateParams.class));
	}

	private static BetaMessage textResponse(String text, long input, long created, long read, long output) {
		BetaTextBlock textBlock = mock(BetaTextBlock.class);
		when(textBlock.text()).thenReturn(text);
		BetaContentBlock content = mock(BetaContentBlock.class);
		when(content.isText()).thenReturn(true);
		when(content.text()).thenReturn(Optional.of(textBlock));
		BetaUsage usage = mock(BetaUsage.class);
		when(usage.inputTokens()).thenReturn(input);
		when(usage.cacheCreationInputTokens()).thenReturn(Optional.of(created));
		when(usage.cacheReadInputTokens()).thenReturn(Optional.of(read));
		when(usage.outputTokens()).thenReturn(output);
		BetaMessage response = mock(BetaMessage.class);
		when(response.content()).thenReturn(Collections.singletonList(content));
		when(response.isValid()).thenReturn(true);
		when(response.usage()).thenReturn(usage);
		return response;
	}

	private static final class StubAnthropicProvider extends AnthropicProvider {
		private final AnthropicClient client;

		StubAnthropicProvider(AnthropicClient client) {
			this.client = client;
		}

		@Override
		protected AnthropicClient getClient() {
			return client;
		}

		int inputCount() {
			try {
				java.lang.reflect.Field field = AnthropicProvider.class.getDeclaredField("inputs");
				field.setAccessible(true);
				return ((java.util.List<?>) field.get(this)).size();
			} catch (ReflectiveOperationException exception) {
				throw new AssertionError(exception);
			}
		}
	}
}
