package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MavenProjectLayoutTest {

	@TempDir
	Path tempDir;

	@Test
	void isMavenProject_shouldReturnTrueWhenPomExists() throws IOException {
		// Arrange
		Path pom = tempDir.resolve("pom.xml");
		Files.write(pom, "<project></project>".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = MavenProjectLayout.isMavenProject(tempDir.toFile());

		// Assert
		assertTrue(result);
	}

	@Test
	void isMavenProject_shouldReturnFalseWhenPomMissing() {
		// Arrange
		File dir = tempDir.toFile();

		// Act
		boolean result = MavenProjectLayout.isMavenProject(dir);

		// Assert
		assertFalse(result);
	}

	@Test
	void getModules_shouldReturnModulesWhenPackagingIsPom() {
		// Arrange
		Model model = new Model();
		model.setPackaging("pom");
		model.setModules(Arrays.asList("module-a", "module-b"));

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(Arrays.asList("module-a", "module-b"), modules);
	}

	@Test
	void getModules_shouldReturnEmptyListWhenNotPomPackaging() {
		// Arrange
		Model model = new Model();
		model.setPackaging("jar");
		model.setModules(Collections.singletonList("module-a"));

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		// Sonar java:S1168 - contract changed to return empty list instead of null.
		assertEquals(Collections.emptyList(), modules);
	}

	@Test
	void getSources_shouldApplyDefaultsWhenBuildDirectoriesMissingAndReturnRelativePaths() {
		// Arrange
		Model model = new Model();
		model.setBuild(null);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertTrue(sources.contains("src/main/java"));
		Build build = model.getBuild();
		assertNotNull(build);
		assertNotNull(build.getSourceDirectory());
		assertNotNull(build.getTestSourceDirectory());
	}

	@Test
	void getSources_shouldIncludeResourcesWhenProvided() {
		// Arrange
		Model model = new Model();
		Build build = new Build();
		build.setSourceDirectory(tempDir.resolve("src/main/java").toString());

		Resource r1 = new Resource();
		r1.setDirectory(tempDir.resolve("src/main/resources").toString());
		Resource r2 = new Resource();
		r2.setDirectory(tempDir.resolve("assets").toString());
		build.setResources(Arrays.asList(r1, r2));
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertTrue(sources.contains("src/main/java"));
		assertTrue(sources.contains("src/main/resources"));
		assertTrue(sources.contains("assets"));
	}

	@Test
	void getTests_shouldIncludeTestSourceAndTestResourcesWhenProvided() {
		// Arrange
		Model model = new Model();
		Build build = new Build();
		build.setTestSourceDirectory(tempDir.resolve("src/test/java").toString());

		Resource tr = new Resource();
		tr.setDirectory(tempDir.resolve("src/test/resources").toString());
		build.setTestResources(Collections.singletonList(tr));
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(Arrays.asList("src/test/java", "src/test/resources"), tests);
	}

	@Test
	void getDocuments_shouldAlwaysContainSrcSite() {
		// Arrange
		Model model = new Model();
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(Collections.singletonList("src/site"), docs);
	}

	@Test
	void getProjectId_shouldReturnArtifactId() {
		// Arrange
		Model model = new Model();
		model.setArtifactId("artifact-x");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		String id = layout.getProjectId();

		// Assert
		assertEquals("artifact-x", id);
	}

	@Test
	void getProjectName_shouldReturnName() {
		// Arrange
		Model model = new Model();
		model.setName("My Project");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		String name = layout.getProjectName();

		// Assert
		assertEquals("My Project", name);
	}

	@Test
	void getParentId_shouldReturnNullWhenNoParent() {
		// Arrange
		Model model = new Model();
		model.setParent(null);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		String parentId = layout.getParentId();

		// Assert
		org.junit.jupiter.api.Assertions.assertNull(parentId);
	}

	@Test
	void getParentId_shouldReturnParentArtifactIdWhenPresent() {
		// Arrange
		Model model = new Model();
		Parent parent = new Parent();
		parent.setArtifactId("parent-artifact");
		model.setParent(parent);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		String parentId = layout.getParentId();

		// Assert
		assertEquals("parent-artifact", parentId);
	}
}
