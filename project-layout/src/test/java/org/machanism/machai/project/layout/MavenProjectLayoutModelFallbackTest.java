package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class MavenProjectLayoutModelFallbackTest {

	@Test
	void getModel_whenEffectivePomRequiredTrueAndEffectiveBuildFails_shouldFallbackToRawModel() throws Exception {
		// Arrange
		File tempDir = Files.createTempDirectory("maven-layout-").toFile();
		tempDir.deleteOnExit();

		File pom = new File(tempDir, "pom.xml");
		Files.write(pom.toPath(), minimalPom().getBytes(StandardCharsets.UTF_8));
		pom.deleteOnExit();

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir);

		// Act
		Model model = layout.getModel();

		// Assert
		assertNotNull(model);
		assertEquals("a", model.getArtifactId());
		assertEquals("g", model.getGroupId());
		assertEquals("1", model.getVersion());
	}

	@Test
	void getModel_whenEffectivePomRequiredFalseAndPomInvalid_shouldRethrowIllegalArgumentException() throws Exception {
		// Arrange
		File tempDir = Files.createTempDirectory("maven-layout-").toFile();
		tempDir.deleteOnExit();

		File pom = new File(tempDir, "pom.xml");
		Files.write(pom.toPath(), "<project>".getBytes(StandardCharsets.UTF_8));
		pom.deleteOnExit();

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, layout::getModel);
	}

	private static String minimalPom() {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
				+ "<modelVersion>4.0.0</modelVersion>"
				+ "<groupId>g</groupId>"
				+ "<artifactId>a</artifactId>"
				+ "<version>1</version>"
				+ "</project>";
	}
}
