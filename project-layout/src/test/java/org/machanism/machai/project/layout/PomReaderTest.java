package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_whenEffectiveFalse_shouldParsePomAndPopulateProjectVersionProperty_andSupportPlaceholderReplacementAcrossCalls() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("machai-pomreader-");
		Path pom = tempDir.resolve("pom.xml");
		try {
			String pomXml1 = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
					+ "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
					+ "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
					+ "  <modelVersion>4.0.0</modelVersion>\n"
					+ "  <groupId>g</groupId>\n"
					+ "  <artifactId>a</artifactId>\n"
					+ "  <version>1.2.3</version>\n"
					+ "  <properties>\n"
					+ "    <my.prop>hello</my.prop>\n"
					+ "  </properties>\n"
					+ "</project>\n";
			Files.write(pom, pomXml1.getBytes(StandardCharsets.UTF_8));

			PomReader reader = new PomReader();

			// Act
			Model model1 = reader.getProjectModel(pom.toFile(), false);
			Map<String, String> props1 = reader.getPomProperties();

			String pomXml2 = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
					+ "  <modelVersion>4.0.0</modelVersion>\n"
					+ "  <groupId>g</groupId>\n"
					+ "  <artifactId>a</artifactId>\n"
					+ "  <version>${project.version}</version>\n"
					+ "  <name>${my.prop}</name>\n"
					+ "</project>\n";
			Files.write(pom, pomXml2.getBytes(StandardCharsets.UTF_8));
			Model model2 = reader.getProjectModel(pom.toFile(), false);

			// Assert
			assertEquals("1.2.3", model1.getVersion());
			assertEquals("1.2.3", props1.get("project.version"));
			assertEquals("hello", props1.get("my.prop"));

			assertEquals("1.2.3", model2.getVersion(),
					"Expected ${project.version} placeholder to be replaced from cached properties");
			assertEquals("hello", model2.getName(), "Expected ${my.prop} placeholder to be replaced from cached properties");
		} finally {
			try {
				TestFileSupport.deleteRecursivelyBestEffort(tempDir);
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@Test
	void getProjectModel_whenPomIsInvalid_shouldThrowIllegalArgumentException() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("machai-pomreader-");
		Path pom = tempDir.resolve("pom.xml");
		try {
			Files.write(pom, "<project><bad".getBytes(StandardCharsets.UTF_8));
			PomReader reader = new PomReader();

			// Act
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
					() -> reader.getProjectModel(pom.toFile(), false));

			// Assert
			assertTrue(ex.getMessage().contains("POM file:"));
			assertNotNull(ex.getCause());
		} finally {
			try {
				TestFileSupport.deleteRecursivelyBestEffort(tempDir);
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@Test
	void printModel_shouldSerializeModelToXml() throws IOException {
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
		assertTrue(xml.contains("<groupId>g</groupId>"));
		assertTrue(xml.contains("<artifactId>a</artifactId>"));
		assertTrue(xml.contains("<version>1</version>"));
	}

	@Test
	void getProjectModel_singleArg_shouldFallbackToNonEffectiveWhenEffectiveFails() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("machai-pomreader-");
		Path pom = tempDir.resolve("pom.xml");
		try {
			String pomXml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
					+ "  <modelVersion>4.0.0</modelVersion>\n"
					+ "  <groupId>g</groupId>\n"
					+ "  <artifactId>a</artifactId>\n"
					+ "  <version>1</version>\n"
					+ "</project>\n";
			Files.write(pom, pomXml.getBytes(StandardCharsets.UTF_8));

			PomReader reader = new PomReader() {
				@Override
				public Model getProjectModel(File pomFile, boolean effective) {
					if (effective) {
						throw new IllegalArgumentException("boom");
					}
					return super.getProjectModel(pomFile, false);
				}
			};

			// Act
			Model model = reader.getProjectModel(pom.toFile());

			// Assert
			assertEquals("g", model.getGroupId());
			assertEquals("a", model.getArtifactId());
			assertEquals("1", model.getVersion());
		} finally {
			try {
				TestFileSupport.deleteRecursivelyBestEffort(tempDir);
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
