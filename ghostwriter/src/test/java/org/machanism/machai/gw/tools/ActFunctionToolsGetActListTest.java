package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Focused tests for ActFunctionTools#getActList (private) via reflection.
 */
class ActFunctionToolsGetActListTest {

	private static Method getActListMethod() throws Exception {
		Method m = ActFunctionTools.class.getDeclaredMethod("getActList", Object[].class);
		m.setAccessible(true);
		return m;
	}

	@Test
	void getActList_formatsEachEntryAsMarkdownBullet() throws Exception {
		// Arrange
		ActFunctionTools tools = Mockito.spy(new ActFunctionTools());
		Set<String> base = new LinkedHashSet<>();
		base.add("`a`: desc A");
		base.add("`b`: desc B");
		Mockito.doReturn(base).when(tools).getBaseActList();

		// Act
		Object result = getActListMethod().invoke(tools, (Object) new Object[0]);

		// Assert
		assertTrue(result instanceof String);
		String text = (String) result;
		// note: the implementation prefixes each entry with "- `" (backtick is part of entry)
		assertEquals("- ``a`: desc A\n- ``b`: desc B", text);
	}

	@Test
	void getActList_whenBaseActListThrowsIOException_propagates() throws Exception {
		// Arrange
		ActFunctionTools tools = Mockito.spy(new ActFunctionTools());
		Mockito.doThrow(new IOException("boom")).when(tools).getBaseActList();

		// Act + Assert (Sonar java:S5783 - single invocation in lambda; exception unwrapped outside)
		InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> invokeGetActList(tools));
		assertNotNull(ex.getCause());
		assertTrue(ex.getCause() instanceof IOException);
		assertEquals("boom", ex.getCause().getMessage());
	}

	private static void invokeGetActList(ActFunctionTools tools) throws Exception {
		getActListMethod().invoke(tools, (Object) new Object[0]);
	}
}
