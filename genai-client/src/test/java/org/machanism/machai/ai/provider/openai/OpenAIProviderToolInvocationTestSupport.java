package org.machanism.machai.ai.provider.openai;

import java.lang.reflect.Method;
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
        ResponseOutputItem item = outputMessageItem(text);
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

    static ResponseOutputItem outputMessageItem(String text) {
        ResponseOutputText outputText = ResponseOutputText.builder().text(text).build();

        Object contentObj;
        try {
            Class<?> contentType = Class.forName("com.openai.models.responses.ResponseOutputMessage$Content");
            Object builder = contentType.getMethod("builder").invoke(null);
            invokeFirstMatching(builder, "outputText", outputText);
            contentObj = builder.getClass().getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        ResponseOutputMessage.Builder msgBuilder = ResponseOutputMessage.builder();
        try {
            msgBuilder.getClass().getMethod("addContent", contentObj.getClass()).invoke(msgBuilder, contentObj);
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        return ResponseOutputItem.ofMessage(msgBuilder.build());
    }

    static ResponseOutputItem reasoningItem(String text) {
        try {
            Class<?> contentType = Class.forName("com.openai.models.responses.ResponseReasoningItem$Content");
            Object content = contentType.getMethod("builder").invoke(null);
            invokeFirstMatching(content, "text", text);
            Object builtContent = content.getClass().getMethod("build").invoke(content);

            Class<?> reasoningType = Class.forName("com.openai.models.responses.ResponseReasoningItem");
            Object builder = reasoningType.getMethod("builder").invoke(null);
            invokeFirstMatching(builder, "addContent", builtContent);
            Object reasoning = builder.getClass().getMethod("build").invoke(builder);
            return (ResponseOutputItem) ResponseOutputItem.class.getMethod("ofReasoning", reasoningType).invoke(null,
                    reasoning);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
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

    private static void invokeFirstMatching(Object target, String methodName, Object argument) throws Exception {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(argument.getClass())) {
                method.invoke(target, argument);
                return;
            }
        }
        throw new NoSuchMethodException(methodName);
    }
}
