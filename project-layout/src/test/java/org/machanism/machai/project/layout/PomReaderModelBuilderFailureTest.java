package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PomReaderModelBuilderFailureTest {

	@TempDir
	File tempDir;

	@Test
	void getProjectModel_effectiveTrue_shouldThrowIllegalArgumentExceptionWhenModelBuilderFails() throws IOException {
		// Arrange
		File pom = new File(tempDir, "pom.xml");
		Files.write(pom.toPath(), minimalPom().getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader() {
			@Override
			public org.eclipse.aether.impl.DefaultServiceLocator serviceLocator() {
				throw new RuntimeException("no locator");
			}
		};

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> reader.getProjectModel(pom, true));

		// Assert
		assertTrue(ex.getMessage().contains("POM file:"));
		assertNotNull(ex.getCause());
		assertTrue(ex.getCause().getMessage().contains("no locator"));
	}

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

	private static String minimalPom() {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
				+ "<modelVersion>4.0.0</modelVersion>"
				+ "<groupId>g</groupId>"
				+ "<artifactId>a</artifactId>"
				+ "<version>1</version>"
				+ "</project>";
	}
}
