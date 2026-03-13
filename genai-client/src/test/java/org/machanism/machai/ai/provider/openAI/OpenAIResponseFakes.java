package org.machanism.machai.ai.provider.openai;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

final class OpenAIResponseFakes {

	private OpenAIResponseFakes() {
	}

	static Object fakeFunctionCall(String name, String arguments, String callId) throws Exception {
		Class<?> iface = Class.forName("com.openai.models.responses.ResponseFunctionToolCall");
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				switch (method.getName()) {
				case "name":
					return name;
				case "arguments":
					return arguments;
				case "callId":
					return callId;
				default:
					Class<?> returnType = method.getReturnType();
					if (returnType.equals(boolean.class)) {
						return false;
					}
					if (returnType.equals(int.class)) {
						return 0;
					}
					if (returnType.equals(long.class)) {
						return 0L;
					}
					return null;
				}
			}
		};
		return Proxy.newProxyInstance(OpenAIResponseFakes.class.getClassLoader(), new Class<?>[] { iface }, handler);
	}
}
