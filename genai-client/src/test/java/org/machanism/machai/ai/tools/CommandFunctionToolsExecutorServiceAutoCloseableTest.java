package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

class CommandFunctionToolsExecutorServiceAutoCloseableTest {

	@Test
	void executorServiceAutoCloseable_getReturnsSameExecutor_andCloseShutsDown() throws Exception {
		// Arrange
		ExecutorService executor = Executors.newSingleThreadExecutor();

		Class<?> clazz = Class.forName(
				"org.machanism.machai.ai.tools.CommandFunctionTools$ExecutorServiceAutoCloseable");
		Constructor<?> ctor = clazz.getDeclaredConstructor(ExecutorService.class);
		ctor.setAccessible(true);
		Object wrapper = ctor.newInstance(executor);

		Method get = clazz.getDeclaredMethod("get");
		get.setAccessible(true);
		Method close = clazz.getDeclaredMethod("close");
		close.setAccessible(true);

		// Act
		Object returned = get.invoke(wrapper);
		close.invoke(wrapper);

		// Assert
		assertSame(executor, returned);
		assertTrue(executor.isShutdown());
	}
}
