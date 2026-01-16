package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Model;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_whenRawParsing_readsModelAndCollectsProperties() {
		// Arrange
		File pomFile = new File("src/test/resources/mockMavenProject/pom.xml");

		// Act
		Model model = PomReader.getProjectModel(pomFile, false);

		// Assert
		assertNotNull(model);
		assertEquals("mock-project", model.getArtifactId());
		assertEquals("1.0.0-SNAPSHOT", PomReader.getPomProperties().get("project.version"));
	}

	@Test
	void printModel_writesXml() throws IOException {
		// Arrange
		File pomFile = new File("src/test/resources/mockMavenProject/pom.xml");
		Model model = PomReader.getProjectModel(pomFile, false);

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
		assertThrows(IllegalArgumentException.class, () -> PomReader.getProjectModel(missing, false));
	}

	@Test
	void serviceLocator_returnsNonNull() {
		// Arrange + Act
		DefaultServiceLocator locator = PomReader.serviceLocator();

		// Assert
		assertNotNull(locator);
	}
}
