package org.machanism.machai.ai.provider.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;

final class OpenAIProviderBrokenObjects {

    private OpenAIProviderBrokenObjects() {
    }

    static ResponseOutputItem brokenMessageOutputItem() throws Exception {
        Object unsafe = getUnsafe();
        ResponseOutputText brokenText = (ResponseOutputText) allocateInstance(unsafe, ResponseOutputText.class);
        ResponseOutputMessage.Content content = invokeContentConstructor(brokenText);
        ResponseOutputMessage message = ResponseOutputMessage.builder()
                .id("broken-message")
                .status(ResponseOutputMessage.Status.COMPLETED)
                .addContent(content)
                .build();
        return ResponseOutputItem.ofMessage(message);
    }

    private static ResponseOutputMessage.Content invokeContentConstructor(ResponseOutputText outputText) throws Exception {
        Constructor<ResponseOutputMessage.Content> constructor = ResponseOutputMessage.Content.class
                .getDeclaredConstructor(ResponseOutputText.class, Class.forName("com.openai.models.responses.ResponseOutputRefusal"),
                        com.openai.core.JsonValue.class);
        constructor.setAccessible(true);
        return constructor.newInstance(outputText, null, null);
    }

    private static Object getUnsafe() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field field = unsafeClass.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return field.get(null);
    }

    private static Object allocateInstance(Object unsafe, Class<?> type) throws Exception {
        Method method = unsafe.getClass().getMethod("allocateInstance", Class.class);
        return method.invoke(unsafe, type);
    }
}
