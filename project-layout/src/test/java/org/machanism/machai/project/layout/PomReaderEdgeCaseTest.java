package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class PomReaderEdgeCaseTest {

	@Test
	void replaceProperty_shouldReturnNullWhenPomStringIsNull() throws Exception {
		// Arrange
		PomReader reader = new PomReader();
		Method method = PomReader.class.getDeclaredMethod("replaceProperty", String.class);
		method.setAccessible(true);

		// Act
		Object result = method.invoke(reader, new Object[] { null });

		// Assert
		assertNull(result); // Sonar fix java:S5785
	}
}
