package org.machanism.machai.project.layout;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PomReaderTest {

	private File pomFile;

	@BeforeEach
	void setUp() {
		pomFile = new File("src/test/resources/mockPom.xml");
	}

	@Test
	@Disabled
	void testGetProjectModel() {
		// Arrange
		boolean effective = false;

		// Act
		Model model = PomReader.getProjectModel(pomFile, effective);

		// Assert
		assertNotNull(model);
		assertEquals("mock-artifact", model.getArtifactId());
	}

	@Test
	@Disabled
	void testGetEffectiveModel() {
		// Arrange
		boolean effective = true;

		// Act
		Model model = PomReader.getProjectModel(pomFile, effective);

		// Assert
		assertNotNull(model);
		assertEquals("mock-artifact", model.getArtifactId());
	}

	@Test
	@Disabled
	void testGetPomProperties() {
		// Arrange
		PomReader.getProjectModel(pomFile, false);

		// Act
		Map<String, String> properties = PomReader.getPomProperties();

		// Assert
		assertNotNull(properties);
		assertTrue(properties.containsKey("project.version"));
	}
}