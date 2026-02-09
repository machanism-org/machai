package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MavenProjectLayoutTest {

	@TempDir
	Path tempDir;

	@Test
	void isMavenProject_shouldReturnTrueOnlyWhenPomXmlExists() throws Exception {
		// Arrange
		File dir = tempDir.toFile();
		File pom = new File(dir, "pom.xml");

		// Act + Assert
		assertFalse(MavenProjectLayout.isMavenProject(dir));
		assertTrue(pom.createNewFile());
		assertTrue(MavenProjectLayout.isMavenProject(dir));
	}

	@Test
	void getModules_shouldReturnModulesOnlyWhenPackagingIsPom() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("pom");
		model.setModules(Arrays.asList("a", "b"));

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(Arrays.asList("a", "b"), modules);
	}

	@Test
	void getModules_shouldReturnNullWhenPackagingIsNotPom() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("jar");
		model.setModules(Arrays.asList("a"));

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_shouldApplyDefaultsWhenBuildIsNull_andReturnSourceAndResourceDirectoriesAsRelativePaths() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setBuild(null);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNotNull(sources);
		assertTrue(sources.contains("src/main/java"));
	}

	@Test
	void getSources_shouldIncludeResourcesInOrder_andReturnNullForPathsOutsideProjectDir() {
		// Arrange
		File projectDir = tempDir.toFile();
		Model model = new Model();
		model.setModelVersion("4.0.0");

		Build build = new Build();
		build.setSourceDirectory(new File(projectDir, "custom-src").getAbsolutePath());
		Resource r1 = new Resource();
		r1.setDirectory(new File(projectDir, "res1").getAbsolutePath());
		Resource r2 = new Resource();
		r2.setDirectory(new File("Z:/outside").getAbsolutePath());
		build.setResources(Arrays.asList(r1, r2));
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertEquals("custom-src", sources.get(0));
		assertEquals("res1", sources.get(1));
		assertNull(sources.get(2));
	}

	@Test
	void getTests_shouldReturnEmptyWhenBuildIsNull() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setBuild(null);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNotNull(tests);
		assertTrue(tests.isEmpty());
	}

	@Test
	void getTests_shouldIncludeTestSourceAndTestResourcesWhenPresent() {
		// Arrange
		File projectDir = tempDir.toFile();
		Model model = new Model();
		model.setModelVersion("4.0.0");

		Build build = new Build();
		build.setTestSourceDirectory(new File(projectDir, "src/test/java").getAbsolutePath());
		Resource tr = new Resource();
		tr.setDirectory(new File(projectDir, "src/test/resources").getAbsolutePath());
		build.setTestResources(Collections.singletonList(tr));
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(Arrays.asList("src/test/java", "src/test/resources"), tests);
	}

	@Test
	void getDocuments_shouldAlwaysReturnSrcSite() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(new Model());

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(Collections.singletonList("src/site"), docs);
	}

	@Test
	void getProjectId_shouldJoinGroupArtifactVersionWithDefaultsForNulls() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId(null);
		model.setArtifactId("artifact");
		model.setVersion(null);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		String id = layout.getProjectId();

		// Assert
		assertEquals(":artifact:", id);
	}

	@Test
	void getProjectName_shouldReturnModelName() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setName("My Project");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		String name = layout.getProjectName();

		// Assert
		assertEquals("My Project", name);
	}
}
