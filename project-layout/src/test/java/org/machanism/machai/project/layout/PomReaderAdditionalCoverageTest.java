package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderAdditionalCoverageTest {

	private static Path createTempDir() throws Exception {
		Path dir = Files.createTempDirectory("pomreader-");
		dir.toFile().deleteOnExit();
		return dir;
	}

	private static Path write(Path file, String content) throws Exception {
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
		file.toFile().deleteOnExit();
		return file;
	}

	@Test
	void getProjectModel_whenPomContainsProperties_populatesPomPropertiesAndKeepsUnresolvedPlaceholders() throws Exception {
		// Arrange
		Path dir = createTempDir();
		Path pom = dir.resolve("pom.xml");
		String xml = "" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo</artifactId>\n" +
				"  <version>1.2.3</version>\n" +
				"  <properties>\n" +
				"    <java.version>17</java.version>\n" +
				"  </properties>\n" +
				"  <name>${java.version}</name>\n" +
				"</project>\n";
		write(pom, xml);

		PomReader reader = new PomReader();

		// Act
		Model model = reader.getProjectModel(pom.toFile());

		// Assert
		// Replacement only occurs using properties already collected from prior reads.
		assertEquals("${java.version}", model.getName());
		assertEquals("1.2.3", reader.getPomProperties().get("project.version"), "Should store project.version");
		assertEquals("17", reader.getPomProperties().get("java.version"));
	}

	@Test
	void getProjectModel_whenPomHasLicenses_initialCallSetsDefaultLicenses_secondEmptyPomUsesDefaultLicenses() throws Exception {
		// Arrange
		Path dir = createTempDir();
		Path pomWithLicense = dir.resolve("pom-with-license.xml");
		String withLicense = "" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo</artifactId>\n" +
				"  <version>1</version>\n" +
				"  <licenses>\n" +
				"    <license>\n" +
				"      <name>The Apache License, Version 2.0</name>\n" +
				"      <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>\n" +
				"    </license>\n" +
				"  </licenses>\n" +
				"</project>\n";
		write(pomWithLicense, withLicense);

		Path pomWithoutLicense = dir.resolve("pom-without-license.xml");
		String withoutLicense = "" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo2</artifactId>\n" +
				"  <version>2</version>\n" +
				"</project>\n";
		write(pomWithoutLicense, withoutLicense);

		PomReader reader = new PomReader();

		// Act
		Model first = reader.getProjectModel(pomWithLicense.toFile());
		Model second = reader.getProjectModel(pomWithoutLicense.toFile());

		// Assert
		assertFalse(first.getLicenses().isEmpty(), "First POM should contain declared licenses");
		assertFalse(second.getLicenses().isEmpty(), "Second POM without licenses should inherit default licenses");
		License inherited = second.getLicenses().get(0);
		assertEquals("The Apache License, Version 2.0", inherited.getName());
	}

	@Test
	void getProjectModel_whenPomIsInvalid_throwsIllegalArgumentException() throws Exception {
		// Arrange
		Path dir = createTempDir();
		Path pom = dir.resolve("broken.xml");
		write(pom, "<project><not-closed>");
		PomReader reader = new PomReader();

		Path pomFile = pom;
		java.io.File pomAsFile = pomFile.toFile();

		// Act
		// Sonar java:S5778 - Ensure a single invocation in the lambda may throw.
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> reader.getProjectModel(pomAsFile));

		// Assert
		assertTrue(ex.getMessage().contains("POM file:"), "Exception message should include file context");
	}

	@Test
	void printModel_serializesModelToXml() throws Exception {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("com.acme");
		model.setArtifactId("demo");
		model.setVersion("1");

		// Act
		String xml = PomReader.printModel(model);

		// Assert
		assertNotNull(xml);
		assertTrue(xml.contains("<artifactId>demo</artifactId>"));
	}

	@Test
	void getPomProperties_returnsLiveMapThatIsPopulatedByReads() throws Exception {
		// Arrange
		Path dir = createTempDir();
		Path pom = dir.resolve("pom.xml");
		String xml = "" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo</artifactId>\n" +
				"  <version>1</version>\n" +
				"  <properties><k>v</k></properties>\n" +
				"</project>\n";
		write(pom, xml);

		PomReader reader = new PomReader();

		// Act
		reader.getProjectModel(pom.toFile());

		// Assert
		assertEquals("v", reader.getPomProperties().get("k"));
	}
}
