package org.machanism.machai.ai.provider.openai;

import java.util.Optional;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseReasoningItem;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ResponseUsage;

/**
 * Test-only helpers for constructing OpenAI SDK response model objects.
 */
final class OpenAIProviderToolInvocationTestSupport {

    private OpenAIProviderToolInvocationTestSupport() {
    }

    static Response responseWithMessage(String text, ResponseUsage usageOrNull) {
        ResponseOutputItem item = outputMessageItem(text);
        Response.Builder responseBuilder = responseBuilder().addOutput(item);
        if (usageOrNull != null) {
            responseBuilder.usage(usageOrNull);
        }
        return responseBuilder.build();
    }

    static Response responseWithToolCall(ResponseFunctionToolCall call, ResponseUsage usageOrNull) {
        ResponseOutputItem item = ResponseOutputItem.ofFunctionCall(call);
        Response.Builder responseBuilder = responseBuilder().addOutput(item);
        if (usageOrNull != null) {
            responseBuilder.usage(usageOrNull);
        }
        return responseBuilder.build();
    }

    static Response.Builder responseBuilder() {
        return Response.builder().id("response-id").status(ResponseStatus.COMPLETED).createdAt(0D)
                .error(Optional.empty()).incompleteDetails(Optional.empty()).instructions(Optional.empty())
                .maxOutputTokens(Optional.empty()).maxToolCalls(Optional.empty()).model("test-model")
                .output(java.util.Collections.emptyList()).parallelToolCalls(false).temperature(Optional.empty())
                .tools(java.util.Collections.emptyList()).topP(Optional.empty()).background(Optional.empty())
                .conversation(Optional.empty()).previousResponseId(Optional.empty()).prompt(Optional.empty())
                .reasoning(Optional.empty()).serviceTier(Optional.empty()).topLogprobs(Optional.empty())
                .truncation(Optional.empty());
    }

    static ResponseOutputItem outputMessageItem(String text) {
        try {
            ResponseOutputMessage.Builder messageBuilder = ResponseOutputMessage.builder();
            messageBuilder.id("message-id");
            messageBuilder.status(ResponseOutputMessage.Status.COMPLETED);
            messageBuilder.addContent(ResponseOutputText.builder().text(text).annotations(java.util.Collections.emptyList()).build());
            return ResponseOutputItem.ofMessage(messageBuilder.build());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static ResponseOutputItem reasoningItem(String text) {
        ResponseReasoningItem.Summary summary = ResponseReasoningItem.Summary.builder().text(text).build();
        ResponseReasoningItem.Content content = ResponseReasoningItem.Content.builder().text(text).build();
        ResponseReasoningItem reasoning = ResponseReasoningItem.builder()
                .id("reasoning-id")
                .addSummary(summary)
                .addContent(content)
                .build();
        return ResponseOutputItem.ofReasoning(reasoning);
    }

    static ResponseUsage usage(long inputTokens, long cached, long outputTokens) {
        try {
            Object detailsBuilder = ResponseUsage.InputTokensDetails.class.getMethod("builder").invoke(null);
            detailsBuilder.getClass().getMethod("cachedTokens", long.class).invoke(detailsBuilder, cached);
            ResponseUsage.InputTokensDetails details = (ResponseUsage.InputTokensDetails) detailsBuilder.getClass().getMethod("build")
                    .invoke(detailsBuilder);
            ResponseUsage.OutputTokensDetails outputDetails = ResponseUsage.OutputTokensDetails.builder().reasoningTokens(0L)
                    .build();
            return ResponseUsage.builder()
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .totalTokens(inputTokens + outputTokens)
                    .inputTokensDetails(details)
                    .outputTokensDetails(outputDetails)
                    .build();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static Optional<ResponseUsage> optionalUsage(ResponseUsage usage) {
        return Optional.ofNullable(usage);
    }
}
