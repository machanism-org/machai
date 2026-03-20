package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.BindexRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class BindexFunctionToolsTest {

	@Test
	void getBindex_throwsWhenRepositoryNotInitialized() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindex", Object[].class);
		method.setAccessible(true);
		ObjectNode props = new ObjectMapper().createObjectNode().put("id", "any");

		// Act
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
				() -> method.invoke(tools, (Object) new Object[] { props }));

		// Assert
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
		assertEquals("BindexRepository is not initialized. Call setConfigurator(...) first.", thrown.getCause().getMessage());
	}

	@Test
	void getBindexSchema_returnsNonEmptySchemaJson() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindexSchema", Object[].class);
		method.setAccessible(true);

		// Act
		String schema = (String) method.invoke(tools, (Object) new Object[] {});

		// Assert
		assertNotNull(schema);
		String trimmed = schema.trim();
		assertTrue(trimmed.length() > 10);
		assertEquals('{', trimmed.charAt(0));
		new ObjectMapper().readTree(trimmed);
	}

	@Test
	void setConfigurator_initializesRepositoryField() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Field repositoryField = BindexFunctionTools.class.getDeclaredField("bindexRepository");
		repositoryField.setAccessible(true);
		assertEquals(null, repositoryField.get(tools));

		Configurator configurator = org.mockito.Mockito.mock(Configurator.class, org.mockito.Mockito.RETURNS_DEFAULTS);

		// Act
		tools.setConfigurator(configurator);

		// Assert
		Object repo = repositoryField.get(tools);
		assertNotNull(repo);
		assertEquals(BindexRepository.class, repo.getClass());
	}

	@Test
	void applyTools_registersExpectedTools() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Object provider = java.lang.reflect.Proxy.newProxyInstance(
				BindexFunctionTools.class.getClassLoader(),
				new Class[] { Class.forName("org.machanism.machai.ai.manager.GenAIProvider") },
				new CapturingGenAIProviderInvocationHandler());

		// Act
		Method applyTools = BindexFunctionTools.class.getMethod("applyTools",
				Class.forName("org.machanism.machai.ai.manager.GenAIProvider"));
		applyTools.invoke(tools, provider);

		// Assert
		CapturingGenAIProviderInvocationHandler handler = (CapturingGenAIProviderInvocationHandler) java.lang.reflect.Proxy
				.getInvocationHandler(provider);
		assertEquals(2, handler.addToolCalls);
		assertTrue(handler.hasTool("get_bindex"));
		assertTrue(handler.hasTool("get_bindex_schema"));
		assertEquals("id:string:required:The bindex id.", handler.getBindexParameters);
		assertNotNull(handler.getBindexCallback);
		assertNotNull(handler.getBindexSchemaCallback);
	}

	private static final class CapturingGenAIProviderInvocationHandler implements java.lang.reflect.InvocationHandler {
		private int addToolCalls;
		private String getBindexParameters;
		private Object getBindexCallback;
		private Object getBindexSchemaCallback;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			if ("addTool".equals(method.getName()) && args != null && args.length >= 3) {
				addToolCalls++;
				String name = (String) args[0];
				Object callback = args[2];
				if ("get_bindex".equals(name)) {
					getBindexCallback = callback;
					if (args.length >= 4 && args[3] instanceof String[]) {
						String[] parameters = (String[]) args[3];
						getBindexParameters = (parameters.length > 0) ? parameters[0] : null;
					}
				} else if ("get_bindex_schema".equals(name)) {
					getBindexSchemaCallback = callback;
				}
				return null;
			}

			Class<?> returnType = method.getReturnType();
			if (returnType.equals(boolean.class)) {
				return false;
			}
			if (returnType.isPrimitive()) {
				return 0;
			}
			return null;
		}

		boolean hasTool(String name) {
			return ("get_bindex".equals(name) && getBindexCallback != null)
					|| ("get_bindex_schema".equals(name) && getBindexSchemaCallback != null);
		}
	}
}
