package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_whenPomMissing_throwsIllegalArgumentException() {
		// Arrange
		File pom = new File("target\\test-data\\" + UUID.randomUUID(), "pom.xml");

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new PomReader().getProjectModel(pom, false));
	}

	@Test
	void getProjectModel_whenRawParsing_replacesPreviouslySeenProperties() throws Exception {
		// Arrange
		File dir = new File("target\\test-data\\" + UUID.randomUUID());
		dir.mkdirs();
		File pom1 = new File(dir, "p1.xml");
		Files.write(pom1.toPath(), pomWithProperty("artifact-one").getBytes(StandardCharsets.UTF_8));
		PomReader reader = new PomReader();
		Model model1 = reader.getProjectModel(pom1, false);
		assertEquals("artifact-one", model1.getArtifactId());

		File pom2 = new File(dir, "p2.xml");
		String pom2Content = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <groupId>g</groupId>\n"
				+ "  <artifactId>${my.artifact}</artifactId>\n"
				+ "  <version>1</version>\n"
				+ "</project>\n";
		Files.write(pom2.toPath(), pom2Content.getBytes(StandardCharsets.UTF_8));

		// Act
		Model model2 = reader.getProjectModel(pom2, false);

		// Assert
		assertEquals("artifact-one", model2.getArtifactId());
		assertEquals("artifact-one", reader.getPomProperties().get("my.artifact"));
	}

	@Test
	void getProjectModel_whenNoLicenses_usesDefaultLicensesFromPreviousModel() throws Exception {
		// Arrange
		File dir = new File("target\\test-data\\" + UUID.randomUUID());
		dir.mkdirs();
		File pomWithLicense = new File(dir, "pom1.xml");
		Files.write(pomWithLicense.toPath(), pomWithLicense().getBytes(StandardCharsets.UTF_8));

		File pomWithoutLicense = new File(dir, "pom2.xml");
		Files.write(pomWithoutLicense.toPath(), minimalPom().getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();
		Model first = reader.getProjectModel(pomWithLicense, false);
		assertEquals(1, first.getLicenses().size());
		License license = first.getLicenses().get(0);

		// Act
		Model second = reader.getProjectModel(pomWithoutLicense, false);

		// Assert
		assertNotNull(second.getLicenses());
		assertEquals(1, second.getLicenses().size());
		assertEquals(license.getName(), second.getLicenses().get(0).getName());
	}

	@Test
	void printModel_serializesModelToXmlContainingArtifactId() throws Exception {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");

		// Act
		String xml = PomReader.printModel(model);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(xml.contains("<artifactId>a</artifactId>"));
	}

	@Test
	void serviceLocator_returnsNonNullLocator() {
		// Arrange
		PomReader reader = new PomReader();

		// Act
		org.eclipse.aether.impl.DefaultServiceLocator locator = reader.serviceLocator();

		// Assert
		assertNotNull(locator);
	}

	@Test
	void getProjectModel_fallbackUsesRawParsingWhenEffectiveFailsForUnresolvableParent() throws Exception {
		// Arrange
		File dir = new File("target\\test-data\\" + UUID.randomUUID());
		dir.mkdirs();
		File pom = new File(dir, "pom.xml");
		String pomXml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <parent>\n"
				+ "    <groupId>no.such</groupId>\n"
				+ "    <artifactId>no-such-parent</artifactId>\n"
				+ "    <version>0.0.0</version>\n"
				+ "  </parent>\n"
				+ "  <artifactId>child</artifactId>\n"
				+ "</project>\n";
		Files.write(pom.toPath(), pomXml.getBytes(StandardCharsets.UTF_8));
		PomReader reader = new PomReader();

		// Act
		Model model = reader.getProjectModel(pom);

		// Assert
		assertNotNull(model);
		assertEquals("child", model.getArtifactId());
	}

	private static String pomWithProperty(String artifactIdValue) {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <groupId>g</groupId>\n"
				+ "  <artifactId>" + artifactIdValue + "</artifactId>\n"
				+ "  <version>1</version>\n"
				+ "  <properties>\n"
				+ "    <my.artifact>" + artifactIdValue + "</my.artifact>\n"
				+ "  </properties>\n"
				+ "</project>\n";
	}

	private static String pomWithLicense() {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <groupId>g</groupId>\n"
				+ "  <artifactId>a</artifactId>\n"
				+ "  <version>1</version>\n"
				+ "  <licenses>\n"
				+ "    <license>\n"
				+ "      <name>Apache-2.0</name>\n"
				+ "      <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>\n"
				+ "    </license>\n"
				+ "  </licenses>\n"
				+ "</project>\n";
	}

	private static String minimalPom() {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <groupId>g</groupId>\n"
				+ "  <artifactId>a2</artifactId>\n"
				+ "  <version>1</version>\n"
				+ "</project>\n";
	}
}
