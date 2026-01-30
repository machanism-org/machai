package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_whenPomIsInvalid_throwsIllegalArgumentException() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("pom-reader-invalid-");
		File pom = tempDir.resolve("pom.xml").toFile();
		writeFile(pom.toPath(), "<not-a-pom>");

		PomReader reader = new PomReader();

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> reader.getProjectModel(pom, false));
	}

	@Test
	void getProjectModel_whenRawPomHasPropertiesAndProjectVersion_populatesPomProperties() throws Exception {
		// Arrange
		Path tempDir = Files.createTempDirectory("pom-reader-props-");
		File pom = tempDir.resolve("pom.xml").toFile();

		String pomXml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
				+ "<modelVersion>4.0.0</modelVersion>"
				+ "<groupId>g</groupId><artifactId>a</artifactId><version>1.0.0</version>"
				+ "<properties><prop1>v1</prop1></properties>"
				+ "</project>";
		writeFile(pom.toPath(), pomXml);

		PomReader reader = new PomReader();

		// Act
		Model model = reader.getProjectModel(pom, false);

		// Assert
		assertNotNull(model);
		assertEquals("v1", reader.getPomProperties().get("prop1"));
		assertEquals("1.0.0", reader.getPomProperties().get("project.version"));
	}

	@Test
	void printModel_whenModelProvided_serializesToXmlContainingCoordinates() throws Exception {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");

		// Act
		String xml = PomReader.printModel(model);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(xml.contains("<groupId>g</groupId>"));
		org.junit.jupiter.api.Assertions.assertTrue(xml.contains("<artifactId>a</artifactId>"));
	}

	private static void writeFile(Path path, String content) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
			fos.write(content.getBytes(StandardCharsets.UTF_8));
		}
	}
}
