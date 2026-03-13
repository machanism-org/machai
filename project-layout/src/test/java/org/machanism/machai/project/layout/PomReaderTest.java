package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

	@Test
	void getProjectModel_nonEffective_shouldReplacePropertiesFromPreviouslyParsedPom() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("pomreader-test-");
		Path pom1 = tempDir.resolve("pom1.xml");
		Files.write(pom1, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>g</groupId><artifactId>a</artifactId><version>1</version>" +
				"<properties><my.prop>value123</my.prop></properties>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		Path pom2 = tempDir.resolve("pom2.xml");
		Files.write(pom2, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>g</groupId><artifactId>${my.prop}</artifactId><version>1</version>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();

		try {
			// Act
			reader.getProjectModel(pom1.toFile(), false);
			Model model2 = reader.getProjectModel(pom2.toFile(), false);

			// Assert
			assertEquals("value123", model2.getArtifactId());
			Map<String, String> props = reader.getPomProperties();
			assertEquals("value123", props.get("my.prop"));
		} finally {
			deleteRecursively(tempDir);
		}
	}

	@Test
	void getProjectModel_nonEffective_shouldAddProjectVersionPropertyWhenVersionPresent() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("pomreader-test-");
		Path pom = tempDir.resolve("pom.xml");
		Files.write(pom, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>g</groupId><artifactId>a</artifactId><version>9.9.9</version>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader();

		try {
			// Act
			reader.getProjectModel(pom.toFile(), false);

			// Assert
			assertEquals("9.9.9", reader.getPomProperties().get("project.version"));
		} finally {
			deleteRecursively(tempDir);
		}
	}

	@Test
	void getProjectModel_shouldThrowIllegalArgumentExceptionWhenPomFileDoesNotExist() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("pomreader-test-");
		Path missing = tempDir.resolve("missing.xml");
		PomReader reader = new PomReader();

		try {
			// Act
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
					() -> reader.getProjectModel(missing.toFile(), false));

			// Assert
			assertTrue(ex.getMessage().contains("POM file:"));
		} finally {
			deleteRecursively(tempDir);
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
		assertTrue(xml.contains("<artifactId>a</artifactId>"));
		assertTrue(xml.contains("<groupId>g</groupId>"));
		assertTrue(xml.contains("<version>1</version>"));
	}

	@Test
	void serviceLocator_shouldReturnNonNullServiceLocator() {
		// Arrange
		PomReader reader = new PomReader();

		// Act
		Object locator = reader.serviceLocator();

		// Assert
		assertNotNull(locator);
	}

	@Test
	void getProjectModel_singleArg_shouldFallbackToNonEffectiveWhenEffectiveFails() throws IOException {
		// Arrange
		Path tempDir = Files.createTempDirectory("pomreader-test-");
		Path pom = tempDir.resolve("pom.xml");
		Files.write(pom, ("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
				"<modelVersion>4.0.0</modelVersion>" +
				"<groupId>g</groupId><artifactId>a</artifactId><version>1</version>" +
				"</project>").getBytes(StandardCharsets.UTF_8));

		PomReader reader = new PomReader() {
			@Override
			public Model getProjectModel(java.io.File pomFile, boolean effective) {
				if (effective) {
					throw new RuntimeException("boom");
				}
				return super.getProjectModel(pomFile, false);
			}
		};

		try {
			// Act
			Model model = reader.getProjectModel(pom.toFile());

			// Assert
			assertNotNull(model);
			assertEquals("a", model.getArtifactId());
		} finally {
			deleteRecursively(tempDir);
		}
	}

	private static void deleteRecursively(Path dir) {
		if (dir == null) {
			return;
		}
		try {
			Files.walk(dir)
					.sorted((a, b) -> b.getNameCount() - a.getNameCount())
					.forEach(p -> {
						try {
							Files.deleteIfExists(p);
						} catch (IOException ignored) {
							// Best-effort cleanup on Windows.
						}
					});
		} catch (IOException ignored) {
			// Best-effort cleanup on Windows.
		}
	}
}
