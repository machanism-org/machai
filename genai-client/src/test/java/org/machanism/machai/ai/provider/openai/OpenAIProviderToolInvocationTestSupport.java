package org.machanism.machai.ai.provider.openai;

import java.util.Optional;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseUsage;

/**
 * Test-only helpers for constructing OpenAI SDK response model objects.
 */
final class OpenAIProviderToolInvocationTestSupport {

    private OpenAIProviderToolInvocationTestSupport() {
    }

    static Response responseWithMessage(String text, ResponseUsage usageOrNull) {
        ResponseOutputText outputText = ResponseOutputText.builder().text(text).build();

        // ResponseOutputMessage.Content is a generated type; construct via builder if present.
        Object contentObj;
        try {
            Class<?> contentType = Class.forName("com.openai.models.responses.ResponseOutputMessage$Content");
            Object builder = contentType.getMethod("builder").invoke(null);
            contentType.getMethod("outputText", ResponseOutputText.class).invoke(builder, outputText);
            contentObj = contentType.getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        ResponseOutputMessage.Builder msgBuilder = ResponseOutputMessage.builder();
        try {
            msgBuilder.getClass().getMethod("addContent", contentObj.getClass()).invoke(msgBuilder, contentObj);
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        ResponseOutputItem item = ResponseOutputItem.ofMessage(msgBuilder.build());

        Response.Builder responseBuilder = Response.builder().addOutput(item);
        if (usageOrNull != null) {
            responseBuilder.usage(usageOrNull);
        }
        return responseBuilder.build();
    }

    static Response responseWithToolCall(ResponseFunctionToolCall call, ResponseUsage usageOrNull) {
        ResponseOutputItem item = ResponseOutputItem.ofFunctionCall(call);
        Response.Builder responseBuilder = Response.builder().addOutput(item);
        if (usageOrNull != null) {
            responseBuilder.usage(usageOrNull);
        }
        return responseBuilder.build();
    }

    static ResponseUsage usage(long inputTokens, long cached, long outputTokens) {
        try {
            Class<?> detailsType = Class.forName("com.openai.models.responses.ResponseUsage$InputTokensDetails");
            Object detailsBuilder = detailsType.getMethod("builder").invoke(null);
            detailsType.getMethod("cachedTokens", long.class).invoke(detailsBuilder, cached);
            Object details = detailsType.getMethod("build").invoke(detailsBuilder);

            Object builder = ResponseUsage.class.getMethod("builder").invoke(null);
            ResponseUsage.class.getMethod("inputTokens", long.class).invoke(builder, inputTokens);
            ResponseUsage.class.getMethod("outputTokens", long.class).invoke(builder, outputTokens);
            ResponseUsage.class.getMethod("inputTokensDetails", detailsType).invoke(builder, details);
            return (ResponseUsage) ResponseUsage.class.getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static Optional<ResponseUsage> optionalUsage(ResponseUsage usage) {
        return Optional.ofNullable(usage);
    }
}
