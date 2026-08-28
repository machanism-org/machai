package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;

/** Exercises provider request/response loops without making network calls. */
class ProviderResponseFlowTest {

	@Test
	void openAiPerformReturnsMessageTextAndSendsBuiltRequestToClient() {
		// Arrange
		OpenAIClient client = mock(OpenAIClient.class, RETURNS_DEEP_STUBS);
		Response response = OpenAIProviderToolInvocationTestSupport.responseWithMessage("completed answer", null);
		when(client.responses().create(any(ResponseCreateParams.class))).thenReturn(response);
		StubOpenAIProvider provider = new StubOpenAIProvider(client);
		provider.init("gpt-test", TestConfigurators.mapBacked());
		provider.instructions("be concise");
		provider.prompt("question");

		// Act
		String result = provider.perform();

		// Assert
		assertEquals("completed answer", result);
		verify(client.responses()).create(any(ResponseCreateParams.class));
	}

	@Test
	void openAiPerformExecutesFunctionCallThenUsesFollowUpResponse() {
		// Arrange
		OpenAIClient client = mock(OpenAIClient.class, RETURNS_DEEP_STUBS);
		ResponseFunctionToolCall call = (ResponseFunctionToolCall) OpenAIResponseFakes.fakeFunctionCall(
				"lookup", "{\"value\":\"x\"}", "call-1");
		Response first = OpenAIProviderToolInvocationTestSupport.responseWithToolCall(call, null);
		Response second = OpenAIProviderToolInvocationTestSupport.responseWithMessage("tool answer", null);
		when(client.responses().create(any(ResponseCreateParams.class))).thenReturn(first, second);
		StubOpenAIProvider provider = new StubOpenAIProvider(client);
		provider.init("gpt-test", TestConfigurators.mapBacked());
		provider.register("lookup", (params, context) -> "tool-result");
		provider.prompt("question");

		// Act
		String result = provider.perform();

		// Assert
		assertEquals("tool answer", result);
		assertEquals(3, provider.inputs.size());
		verify(client.responses(), times(2)).create(any(ResponseCreateParams.class));
	}

	private static final class StubOpenAIProvider extends OpenAIProvider {
		private final OpenAIClient client;

		StubOpenAIProvider(OpenAIClient client) {
			this.client = client;
		}

		@Override
		public OpenAIClient getClient() {
			return client;
		}

		void register(String name, org.machanism.machai.ai.tools.ToolFunction function) {
			addTool(name, "test tool", function);
		}
	}

}
