package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
	void isMavenProject_shouldReturnTrueWhenPomExists() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("pom.xml"), minimalPom("com.acme", "demo", "1.0").getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = MavenProjectLayout.isMavenProject(tempDir.toFile());

		// Assert
		assertTrue(result);
	}

	@Test
	void isMavenProject_shouldReturnFalseWhenPomAbsent() {
		// Arrange
		// Act
		boolean result = MavenProjectLayout.isMavenProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void getModules_shouldReturnModulesWhenPackagingPom() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("pom");
		model.addModule("module-a");
		model.addModule("module-b");

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(Arrays.asList("module-a", "module-b"), modules);
	}

	@Test
	void getModules_shouldReturnNullWhenNotPomPackaging() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setPackaging("jar");
		model.addModule("module-a");

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_shouldAddDefaultSourceAndTestDirsWhenBuildMissing() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setBuild(null);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertEquals(1, sources.size());
		assertEquals("src/main/java", sources.get(0));
		assertNotNull(model.getBuild());
		assertNotNull(model.getBuild().getSourceDirectory());
		assertNotNull(model.getBuild().getTestSourceDirectory());
	}

	@Test
	void getSources_shouldIncludeResourcesDirectoriesAsRelativePaths() {
		// Arrange
		Build build = new Build();
		build.setSourceDirectory(new File(tempDir.toFile(), "custom-src").getAbsolutePath());
		Resource res1 = new Resource();
		res1.setDirectory(new File(tempDir.toFile(), "src/main/resources").getAbsolutePath());
		Resource res2 = new Resource();
		res2.setDirectory(new File(tempDir.toFile(), "other-res").getAbsolutePath());
		build.setResources(Arrays.asList(res1, res2));

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertEquals(3, sources.size());
		assertTrue(sources.contains("custom-src"));
		assertTrue(sources.contains("src/main/resources"));
		assertTrue(sources.contains("other-res"));
	}

	@Test
	void getTests_shouldReturnEmptyListWhenBuildMissing() {
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
	void getTests_shouldIncludeTestSourceDirectoryAndTestResources() {
		// Arrange
		Build build = new Build();
		build.setTestSourceDirectory(new File(tempDir.toFile(), "src/test/java").getAbsolutePath());
		Resource tr = new Resource();
		tr.setDirectory(new File(tempDir.toFile(), "src/test/resources").getAbsolutePath());
		build.setTestResources(Arrays.asList(tr));

		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(2, tests.size());
		assertTrue(tests.contains("src/test/java"));
		assertTrue(tests.contains("src/test/resources"));
	}

	@Test
	void getDocuments_shouldReturnDefaultSiteDir() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(new Model());

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(Arrays.asList("src/site"), docs);
	}

	@Test
	void getProjectId_shouldUseEmptyStringsForNullParts() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId(null);
		model.setArtifactId("a");
		model.setVersion(null);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		String id = layout.getProjectId();

		// Assert
		assertEquals(":a:", id);
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

	@Test
	void getModel_shouldFallbackToNonEffectivePomWhenEffectiveFails() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("pom.xml"), minimalPom("com.acme", "demo", "1.0").getBytes(StandardCharsets.UTF_8));

		MavenProjectLayout layout = new MavenProjectLayout();
		layout.projectDir(tempDir.toFile());
		layout.effectivePomRequired(true);

		// Act
		Model model = layout.getModel();

		// Assert
		assertNotNull(model);
		assertEquals("com.acme", model.getGroupId());
		assertEquals("demo", model.getArtifactId());
		assertEquals("1.0", model.getVersion());
	}

	private static String minimalPom(String groupId, String artifactId, String version) {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <modelVersion>4.0.0</modelVersion>\n" +
				"  <groupId>" + groupId + "</groupId>\n" +
				"  <artifactId>" + artifactId + "</artifactId>\n" +
				"  <version>" + version + "</version>\n" +
				"</project>\n";
	}
}
