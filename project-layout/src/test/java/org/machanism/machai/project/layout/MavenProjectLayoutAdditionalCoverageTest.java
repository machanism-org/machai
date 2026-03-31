package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MavenProjectLayoutAdditionalCoverageTest {

	@TempDir
	Path tempDir;

	@Test
	void getModules_whenModelWasNotResolvedAndNotMultiModule_returnsEmptyList() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");
		model.setPackaging("jar");

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_whenBuildIsNull_appliesDefaultsAndReturnsRelativeSourceDir() throws Exception {
		// Arrange
		Path projectDir = tempDir.resolve("maven-project");
		Files.createDirectories(projectDir);
		Files.createDirectories(projectDir.resolve("src/main/java"));

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");
		model.setBuild(null);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir.toFile()).model(model);

		// Act
		Set<String> sources = layout.getSources();

		// Assert
		assertTrue(sources.contains("src/main/java"), "Should contain default src/main/java");
		assertNotNull(model.getBuild(), "Build should be initialized when missing");
		assertNotNull(model.getBuild().getSourceDirectory(), "Default sourceDirectory should be set");
		assertNotNull(model.getBuild().getTestSourceDirectory(), "Default testSourceDirectory should be set");
	}

	@Test
	void getTests_whenBuildHasTestResources_addsBothTestSourceDirAndResources() throws Exception {
		// Arrange
		Path projectDir = tempDir.resolve("maven-project-with-tests");
		Files.createDirectories(projectDir);
		Files.createDirectories(projectDir.resolve("custom-test-src"));
		Files.createDirectories(projectDir.resolve("custom-test-resources"));

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");

		Build build = new Build();
		build.setTestSourceDirectory(projectDir.resolve("custom-test-src").toFile().getAbsolutePath());

		org.apache.maven.model.Resource testResource = new org.apache.maven.model.Resource();
		testResource.setDirectory(projectDir.resolve("custom-test-resources").toFile().getAbsolutePath());
		build.addTestResource(testResource);
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir.toFile()).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(2, tests.size());
		assertTrue(tests.contains("custom-test-src"));
		assertTrue(tests.contains("custom-test-resources"));
	}

	@Test
	void getParentId_whenParentIsNull_returnsNull() {
		// Arrange
		Model model = new Model();
		model.setArtifactId("a");
		model.setVersion("1");
		model.setParent(null);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act + Assert
		assertNull(layout.getParentId());
	}

	@Test
	void getParentId_whenParentIsPresent_returnsParentArtifactId() {
		// Arrange
		Model model = new Model();
		model.setArtifactId("a");
		model.setVersion("1");

		Parent parent = new Parent();
		parent.setArtifactId("parent-artifact");
		model.setParent(parent);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		String parentId = layout.getParentId();

		// Assert
		assertEquals("parent-artifact", parentId);
	}

	@Test
	void isMavenProject_whenPomExists_returnsTrue() throws Exception {
		// Arrange
		Path projectDir = tempDir.resolve("project");
		Files.createDirectories(projectDir);
		Files.write(projectDir.resolve("pom.xml"), "<project/>".getBytes(StandardCharsets.UTF_8));

		// Act + Assert
		assertTrue(MavenProjectLayout.isMavenProject(projectDir.toFile()));
	}

	@Test
	void isMavenProject_whenPomMissing_returnsFalse() {
		// Arrange
		File projectDir = tempDir.resolve("no-pom").toFile();

		// Act + Assert
		assertFalse(MavenProjectLayout.isMavenProject(projectDir));
	}
}
