package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ActCommandAnonymousInputTest {

	@Test
	void act_createsAnonymousActProcessor_inputDelegatesToLineReader() throws Exception {
		// Arrange
		LineReader lineReader = Mockito.mock(LineReader.class);
		Mockito.when(lineReader.readLine("prompt:> ")).thenReturn("hello");

		ActCommand cmd = new ActCommand(lineReader);

		Class<?> anonymousClass = Class.forName("org.machanism.machai.cli.ActCommand$1");
		Constructor<?> ctor = anonymousClass.getDeclaredConstructor(ActCommand.class, File.class,
				org.machanism.macha.core.commons.configurator.Configurator.class, String.class);
		ctor.setAccessible(true);

		Object processor = ctor.newInstance(cmd, new File("."), new org.machanism.macha.core.commons.configurator.PropertiesConfigurator(),
				"model");
		Method inputMethod = anonymousClass.getDeclaredMethod("input");
		inputMethod.setAccessible(true);

		// Act
		String result = (String) inputMethod.invoke(processor);

		// Assert
		assertEquals("hello", result);
		Mockito.verify(lineReader).readLine("prompt:> ");
	}
}
