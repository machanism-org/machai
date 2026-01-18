package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	@Disabled
	void getProjectModel_whenRawParsing_readsModelAndCollectsProperties() {
		// Arrange
		File pomFile = new File("src/test/resources/mockMavenProject/pom.xml");

		// Act
		Model model = new PomReader().getProjectModel(pomFile, false);

		// Assert
		assertNotNull(model);
		assertEquals("mock-project", model.getArtifactId());
		assertEquals("1.0.0-SNAPSHOT", new PomReader().getPomProperties().get("project.version"));
	}

	@Test
	void printModel_writesXml() throws IOException {
		// Arrange
		File pomFile = new File("src/test/resources/mockMavenProject/pom.xml");
		Model model = new PomReader().getProjectModel(pomFile, false);

		// Act
		String xml = PomReader.printModel(model);

		// Assert
		assertNotNull(xml);
		assertEquals(true, xml.contains("<artifactId>mock-project</artifactId>"));
	}

	@Test
	void getProjectModel_whenPomMissing_throwsIllegalArgumentException() {
		// Arrange
		File missing = new File("src/test/resources/mockMavenProject/does-not-exist.xml");

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new PomReader().getProjectModel(missing, false));
	}

	@Test
	void serviceLocator_returnsNonNull() {
		// Arrange + Act
		DefaultServiceLocator locator = new PomReader().serviceLocator();

		// Assert
		assertNotNull(locator);
	}
}
