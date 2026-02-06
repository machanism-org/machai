package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;

class MavenProjectLayoutTest {

	@Test
	void isMavenProject_shouldReturnTrueWhenPomXmlExists() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/maven-is-maven");
		Files.createDirectories(dir.toPath());
		Files.write(new File(dir, "pom.xml").toPath(), "<project/>".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = MavenProjectLayout.isMavenProject(dir);

		// Assert
		assertTrue(result);
	}

	@Test
	void isMavenProject_shouldReturnFalseWhenPomXmlMissing() throws Exception {
		// Arrange
		File dir = new File("target/test-tmp/maven-not-maven");
		Files.createDirectories(dir.toPath());

		// Act
		boolean result = MavenProjectLayout.isMavenProject(dir);

		// Assert
		assertFalse(result);
	}

	@Test
	void getModules_shouldReturnModulesWhenPackagingPom() {
		// Arrange
		File dir = new File("target/test-tmp/maven-modules");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("pom");
		model.setModules(Arrays.asList("module-a", "module-b"));

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(Arrays.asList("module-a", "module-b"), modules);
	}

	@Test
	void getModules_shouldReturnNullWhenPackagingNotPom() {
		// Arrange
		File dir = new File("target/test-tmp/maven-modules-null");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("jar");
		model.setModules(Arrays.asList("module-a"));

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_shouldDefaultBuildDirectoriesWhenBuildIsNull() {
		// Arrange
		File dir = new File("target/test-tmp/maven-default-build");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("jar");
		model.setBuild(null);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertTrue(sources.contains("src/main/java"));
	}

	@Test
	void getSources_shouldIncludeResourcesAndKeepRelativePaths() {
		// Arrange
		File dir = new File("target/test-tmp/maven-sources-with-resources");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Build build = new Build();
		build.setSourceDirectory(new File(dir, "custom-src").getAbsolutePath());
		Resource resource = new Resource();
		resource.setDirectory(new File(dir, "res").getAbsolutePath());
		build.setResources(Collections.singletonList(resource));

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("jar");
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertEquals(2, sources.size());
		assertTrue(sources.contains("custom-src"));
		assertTrue(sources.contains("res"));
	}

	@Test
	void getTests_shouldReturnEmptyListWhenBuildIsNull() {
		// Arrange
		File dir = new File("target/test-tmp/maven-tests-null-build");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("jar");
		model.setBuild(null);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNotNull(tests);
		assertTrue(tests.isEmpty());
	}

	@Test
	void getTests_shouldIncludeTestSourceDirectoryAndTestResources() {
		// Arrange
		File dir = new File("target/test-tmp/maven-tests-with-resources");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Build build = new Build();
		build.setTestSourceDirectory(new File(dir, "it").getAbsolutePath());
		Resource tr = new Resource();
		tr.setDirectory(new File(dir, "it-res").getAbsolutePath());
		build.setTestResources(Collections.singletonList(tr));

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(2, tests.size());
		assertTrue(tests.contains("it"));
		assertTrue(tests.contains("it-res"));
	}

	@Test
	void getDocuments_shouldAlwaysReturnSrcSite() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(new File("target/test-tmp/maven-docs"))
				.model(new Model());

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(Collections.singletonList("src/site"), docs);
	}

	@Test
	void effectivePomRequired_shouldBeChainable() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout();

		// Act
		MavenProjectLayout returned = layout.effectivePomRequired(false);

		// Assert
		assertSame(layout, returned);
	}

	@Test
	void projectDir_shouldReturnConcreteTypeForChaining() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout();
		File dir = new File("target/test-tmp/maven-chain");

		// Act
		MavenProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertSame(layout, returned);
		assertSame(dir, layout.getProjectDir());
	}

	@Test
	void getProjectId_shouldConcatenateCoordinatesUsingDefaultStringForNulls() {
		// Arrange
		File dir = new File("target/test-tmp/maven-id");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir).model(model);

		// Act
		String id = layout.getProjectId();

		// Assert
		assertEquals("g:a:1", id);
	}

	@Test
	void getProjectName_shouldReturnModelName() {
		// Arrange
		File dir = new File("target/test-tmp/maven-name");
		assertTrue(dir.mkdirs() || dir.isDirectory());

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setName("My Project");

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(dir).model(model);

		// Act
		String name = layout.getProjectName();

		// Assert
		assertEquals("My Project", name);
	}
}
