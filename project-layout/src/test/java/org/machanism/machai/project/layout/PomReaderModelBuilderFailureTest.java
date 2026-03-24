package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class PomReaderModelBuilderFailureTest {

	@Test
	void replaceProperty_shouldNotReplaceWhenValueIsNull() throws Exception {
		// Arrange
		PomReader reader = new PomReader();
		reader.getPomProperties().put("a", null);

		Method replaceProperty = PomReader.class.getDeclaredMethod("replaceProperty", String.class);
		replaceProperty.setAccessible(true);
		String input = "<project>${a}</project>";

		// Act
		String result;
		try {
			result = (String) replaceProperty.invoke(reader, input);
		} catch (InvocationTargetException e) {
			throw (e.getCause() instanceof Exception) ? (Exception) e.getCause() : e;
		}

		// Assert
		assertEquals(input, result);
	}

}
