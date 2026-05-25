package org.machanism.machai.ai.provider.openai;

import com.openai.core.JsonValue;
import com.openai.models.responses.ResponseFunctionToolCall;

final class OpenAIResponseFakes {

	private OpenAIResponseFakes() {
	}

	static Object fakeFunctionCall(String name, String arguments, String callId) {
		return ResponseFunctionToolCall.builder().name(name).arguments(arguments).callId(callId)
				.type(JsonValue.from("function_call")).id("id-" + callId).build();
	}
}
