package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_whenEffectiveFalse_shouldParsePomAndReplacePropertiesFromPreviousRuns() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		File pom1 = new File(dir, "pom1.xml");
		String pom1Str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>g</groupId>\n" +
				"  <artifactId>a</artifactId>\n" +
				"  <version>1.0</version>\n" +
				"  <properties><myProp>myValue</myProp></properties>\n" +
				"</project>\n";
		Files.write(pom1.toPath(), pom1Str.getBytes(StandardCharsets.UTF_8));

		File pom2 = new File(dir, "pom2.xml");
		String pom2Str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>g</groupId>\n" +
				"  <artifactId>b</artifactId>\n" +
				"  <version>${myProp}</version>\n" +
				"</project>\n";
		Files.write(pom2.toPath(), pom2Str.getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();

		// Act
		Model model1 = reader.getProjectModel(pom1, false);
		Model model2 = reader.getProjectModel(pom2, false);

		// Assert
		assertEquals("a", model1.getArtifactId());
		assertEquals("myValue", model2.getVersion());
		assertEquals("myValue", reader.getPomProperties().get("myProp"));
		assertEquals("myValue", reader.getPomProperties().get("project.version"));
	}

	@Test
	void getProjectModel_whenPomIsInvalid_shouldThrowIllegalArgumentException() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader-invalid");
		assertTrue(dir.mkdirs() || dir.isDirectory());
		File pom = new File(dir, "pom.xml");
		Files.write(pom.toPath(), "not xml".getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> reader.getProjectModel(pom, false));
	}

	@Test
	void printModel_shouldProduceXmlContainingArtifactId() throws Exception {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");

		// Act
		String xml = PomReader.printModel(model);

		// Assert
		assertNotNull(xml);
		assertTrue(xml.contains("<artifactId>a</artifactId>"));
	}
}
