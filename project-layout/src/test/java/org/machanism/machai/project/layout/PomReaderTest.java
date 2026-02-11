package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_nonEffective_shouldReplacePropertiesFromPreviouslyParsedPom() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("PomReaderTest");
		tempDir.toFile().deleteOnExit();

		Path pom1 = tempDir.resolve("pom1.xml");
		Files.write(pom1, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>com.acme</groupId><artifactId>a</artifactId><version>1.0</version>" +
				"<properties><my.prop>VALUE</my.prop></properties>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		Path pom2 = tempDir.resolve("pom2.xml");
		Files.write(pom2, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>com.acme</groupId><artifactId>b</artifactId><version>${my.prop}</version>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();
		reader.getProjectModel(pom1.toFile(), false);

		// Act
		Model model2 = reader.getProjectModel(pom2.toFile(), false);

		// Assert
		assertEquals("VALUE", model2.getVersion());
		Map<String, String> props = reader.getPomProperties();
		assertEquals("VALUE", props.get("my.prop"));
		assertEquals("VALUE", props.get("project.version"));
	}

	@Test
	void getProjectModel_nonEffective_shouldSetProjectVersionPropertyWhenVersionPresent() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("PomReaderTest");
		tempDir.toFile().deleteOnExit();

		Path pom = tempDir.resolve("pom.xml");
		Files.write(pom, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>com.acme</groupId><artifactId>a</artifactId><version>2.1</version>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();

		// Act
		reader.getProjectModel(pom.toFile(), false);

		// Assert
		assertEquals("2.1", reader.getPomProperties().get("project.version"));
	}

	@Test
	void getProjectModel_shouldApplyDefaultLicensesWhenMissingInLaterPom() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("PomReaderTest");
		tempDir.toFile().deleteOnExit();

		Path pomWithLicense = tempDir.resolve("pom1.xml");
		Files.write(pomWithLicense, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>com.acme</groupId><artifactId>a</artifactId><version>1</version>" +
				"<licenses><license><name>Apache-2.0</name></license></licenses>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		Path pomWithoutLicense = tempDir.resolve("pom2.xml");
		Files.write(pomWithoutLicense, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>com.acme</groupId><artifactId>b</artifactId><version>1</version>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();
		Model m1 = reader.getProjectModel(pomWithLicense.toFile(), false);
		assertEquals(1, m1.getLicenses().size());

		// Act
		Model m2 = reader.getProjectModel(pomWithoutLicense.toFile(), false);

		// Assert
		assertEquals(1, m2.getLicenses().size());
		License license = m2.getLicenses().get(0);
		assertEquals("Apache-2.0", license.getName());
	}

	@Test
	void printModel_shouldReturnXmlContainingArtifactId() throws IOException {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("com.acme");
		model.setArtifactId("demo");
		model.setVersion("1.0");

		// Act
		String xml = PomReader.printModel(model);

		// Assert
		assertNotNull(xml);
		assertTrue(xml.contains("<artifactId>demo</artifactId>"));
	}

	@Test
	void getProjectModel_fileConvenienceMethod_shouldFallbackToNonEffectiveWhenEffectiveFails() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("PomReaderTest");
		tempDir.toFile().deleteOnExit();

		Path pom = tempDir.resolve("pom.xml");
		Files.write(pom, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>com.acme</groupId><artifactId>a</artifactId><version>1</version>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();

		// Act
		Model model = reader.getProjectModel(pom.toFile());

		// Assert
		assertNotNull(model);
		assertEquals("com.acme", model.getGroupId());
		assertEquals("a", model.getArtifactId());
		assertEquals("1", model.getVersion());
	}

	@Test
	void getProjectModel_shouldThrowIllegalArgumentExceptionForMissingPom() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("PomReaderTest");
		tempDir.toFile().deleteOnExit();
		File missing = tempDir.resolve("missing.xml").toFile();
		PomReader reader = new PomReader();

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> reader.getProjectModel(missing, false));
	}
}
