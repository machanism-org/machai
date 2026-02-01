package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_nonEffective_shouldParseAndCollectPropertiesAndProjectVersionProperty() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader-non-effective");
		Files.createDirectories(dir.toPath());
		File pom = new File(dir, "pom.xml");
		String pomXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo</artifactId>\n" +
				"  <version>1.2.3</version>\n" +
				"  <properties>\n" +
				"    <demo.prop>value</demo.prop>\n" +
				"  </properties>\n" +
				"</project>\n";
		Files.write(pom.toPath(), pomXml.getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();

		// Act
		Model model = reader.getProjectModel(pom, false);
		Map<String, String> props = reader.getPomProperties();

		// Assert
		assertNotNull(model);
		assertEquals("demo", model.getArtifactId());
		assertEquals("1.2.3", model.getVersion());
		assertEquals("value", props.get("demo.prop"));
		assertEquals("1.2.3", props.get("project.version"));
	}

	@Test
	void getProjectModel_nonEffective_shouldReplacePropertyPlaceholdersUsingPreviouslyCollectedProperties() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader-replace");
		Files.createDirectories(dir.toPath());
		PomReader reader = new PomReader();

		File pom1 = new File(dir, "pom1.xml");
		String pomXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo</artifactId>\n" +
				"  <version>1</version>\n" +
				"  <properties>\n" +
				"    <x>replaced</x>\n" +
				"  </properties>\n" +
				"</project>\n";
		Files.write(pom1.toPath(), pomXml1.getBytes(StandardCharsets.UTF_8));
		reader.getProjectModel(pom1, false);

		File pom2 = new File(dir, "pom2.xml");
		String pomXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>${x}</artifactId>\n" +
				"  <version>1</version>\n" +
				"</project>\n";
		Files.write(pom2.toPath(), pomXml2.getBytes(StandardCharsets.UTF_8));

		// Act
		Model model2 = reader.getProjectModel(pom2, false);

		// Assert
		assertEquals("replaced", model2.getArtifactId());
	}

	@Test
	void getProjectModel_nonEffective_shouldApplyDefaultLicensesWhenModelHasNone() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader-licenses");
		Files.createDirectories(dir.toPath());
		PomReader reader = new PomReader();

		File pomWithLicense = new File(dir, "pom-with-license.xml");
		String pomXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>a</artifactId>\n" +
				"  <version>1</version>\n" +
				"  <licenses>\n" +
				"    <license><name>MIT</name></license>\n" +
				"  </licenses>\n" +
				"</project>\n";
		Files.write(pomWithLicense.toPath(), pomXml1.getBytes(StandardCharsets.UTF_8));
		Model m1 = reader.getProjectModel(pomWithLicense, false);
		assertEquals(1, m1.getLicenses().size());

		File pomNoLicense = new File(dir, "pom-no-license.xml");
		String pomXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>b</artifactId>\n" +
				"  <version>1</version>\n" +
				"</project>\n";
		Files.write(pomNoLicense.toPath(), pomXml2.getBytes(StandardCharsets.UTF_8));

		// Act
		Model m2 = reader.getProjectModel(pomNoLicense, false);

		// Assert
		assertEquals(1, m2.getLicenses().size());
		License l = m2.getLicenses().get(0);
		assertEquals("MIT", l.getName());
	}

	@Test
	void printModel_shouldSerializeModelToXml() throws Exception {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("com.acme");
		model.setArtifactId("demo");
		model.setVersion("1");
		model.setLicenses(Collections.emptyList());

		// Act
		String xml = PomReader.printModel(model);

		// Assert
		assertNotNull(xml);
		assertTrue(xml.contains("<artifactId>demo</artifactId>"));
		assertTrue(xml.contains("<groupId>com.acme</groupId>"));
	}

	@Test
	void getProjectModel_shouldFallbackToNonEffectiveWhenEffectiveFails() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/pomreader-fallback");
		Files.createDirectories(dir.toPath());
		File pom = new File(dir, "pom.xml");
		String pomXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>com.acme</groupId>\n" +
				"  <artifactId>demo</artifactId>\n" +
				"  <version>1</version>\n" +
				"</project>\n";
		Files.write(pom.toPath(), pomXml.getBytes(StandardCharsets.UTF_8));

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
		assertNotNull(model);
		assertEquals("demo", model.getArtifactId());
	}

	@Test
	void serviceLocator_shouldCreateNonNullLocator() {
		// Arrange
		PomReader reader = new PomReader();

		// Act
		Object locator = reader.serviceLocator();

		// Assert
		assertNotNull(locator);
	}
}
