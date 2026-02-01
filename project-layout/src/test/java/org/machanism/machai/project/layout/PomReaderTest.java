package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_whenEffectiveFalse_shouldParsePomAndReplacePropertiesFromPreviousRuns() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		File pom1 = new File(dir, "pom1.xml");
		String pom1Str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n" + "  <groupId>g</groupId>\n"
				+ "  <artifactId>a</artifactId>\n" + "  <version>1.0</version>\n"
				+ "  <properties><myProp>myValue</myProp></properties>\n" + "</project>\n";
		Files.write(pom1.toPath(), pom1Str.getBytes(StandardCharsets.UTF_8));

		File pom2 = new File(dir, "pom2.xml");
		String pom2Str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n" + "  <groupId>g</groupId>\n"
				+ "  <artifactId>b</artifactId>\n" + "  <version>${myProp}</version>\n" + "</project>\n";
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
	void getProjectModel_shouldFallbackToNonEffectiveWhenEffectiveFails() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader-fallback");
		assertTrue(dir.mkdirs() || dir.isDirectory());
		File pom = new File(dir, "pom.xml");

		String pomStr = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n" + "  <groupId>g</groupId>\n"
				+ "  <artifactId>a</artifactId>\n" + "  <version>1.0</version>\n" + "</project>\n";
		Files.write(pom.toPath(), pomStr.getBytes(StandardCharsets.UTF_8));

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
		Model model = reader.getProjectModel(pom);

		// Assert
		assertEquals("a", model.getArtifactId());
	}

	@Test
	void getProjectModel_shouldPopulateDefaultLicensesFromFirstPomWhenLicensesPresent() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader-licenses");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		File pom1 = new File(dir, "pom1.xml");
		String pom1Str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n" + "  <groupId>g</groupId>\n"
				+ "  <artifactId>a</artifactId>\n" + "  <version>1.0</version>\n"
				+ "  <licenses><license><name>Apache-2.0</name><url>https://example.com</url></license></licenses>\n"
				+ "</project>\n";
		Files.write(pom1.toPath(), pom1Str.getBytes(StandardCharsets.UTF_8));

		File pom2 = new File(dir, "pom2.xml");
		String pom2Str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n" + "  <groupId>g</groupId>\n"
				+ "  <artifactId>b</artifactId>\n" + "  <version>1.0</version>\n" + "</project>\n";
		Files.write(pom2.toPath(), pom2Str.getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();

		// Act
		Model model1 = reader.getProjectModel(pom1, false);
		Model model2 = reader.getProjectModel(pom2, false);

		// Assert
		assertEquals(1, model1.getLicenses().size());
		assertEquals("Apache-2.0", model1.getLicenses().get(0).getName());
		assertEquals(1, model2.getLicenses().size());
		assertEquals("Apache-2.0", model2.getLicenses().get(0).getName());
	}

	@Test
	void getProjectModel_shouldUseDefaultLicensesWhenPomHasNoLicenses() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader-default-licenses");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		PomReader reader = new PomReader();

		File pomWithLicense = new File(dir, "pom1.xml");
		String pom1Str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n" + "  <groupId>g</groupId>\n"
				+ "  <artifactId>a</artifactId>\n" + "  <version>1.0</version>\n"
				+ "  <licenses><license><name>MIT</name><url>https://example.com</url></license></licenses>\n"
				+ "</project>\n";
		Files.write(pomWithLicense.toPath(), pom1Str.getBytes(StandardCharsets.UTF_8));
		reader.getProjectModel(pomWithLicense, false);

		File pomWithoutLicense = new File(dir, "pom2.xml");
		String pom2Str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n" + "  <groupId>g</groupId>\n"
				+ "  <artifactId>b</artifactId>\n" + "  <version>1.0</version>\n" + "</project>\n";
		Files.write(pomWithoutLicense.toPath(), pom2Str.getBytes(StandardCharsets.UTF_8));

		// Act
		Model model = reader.getProjectModel(pomWithoutLicense, false);

		// Assert
		assertEquals(1, model.getLicenses().size());
		License license = model.getLicenses().get(0);
		assertEquals("MIT", license.getName());
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
