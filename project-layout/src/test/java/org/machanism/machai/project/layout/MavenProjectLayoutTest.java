package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MavenProjectLayoutTest {

	@TempDir
	File tempDir;

	@Test
	void isMavenProject_whenPomExists_returnsTrue() throws Exception {
		// Arrange
		Files.write(new File(tempDir, "pom.xml").toPath(), minimalPom("jar").getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = MavenProjectLayout.isMavenProject(tempDir);

		// Assert
		org.junit.jupiter.api.Assertions.assertTrue(result);
	}

	@Test
	void getModules_whenPackagingPom_returnsModules() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");
		model.setPackaging("pom");
		model.setModules(java.util.Arrays.asList("m1", "m2"));

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(java.util.Arrays.asList("m1", "m2"), modules);
	}

	@Test
	void getModules_whenPackagingNotPom_returnsNull() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");
		model.setPackaging("jar");
		model.setModules(java.util.Arrays.asList("m1"));

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir).model(model);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_whenBuildNull_setsDefaultsAndReturnsRelativeSourceDir() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");
		model.setBuild(null);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir).model(model);

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
	void getTests_whenBuildHasTestSourceDirectory_returnsRelative() {
		// Arrange
		Build build = new Build();
		build.setTestSourceDirectory(new File(tempDir, "custom-tests").getAbsolutePath());
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir).model(model);

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(1, tests.size());
		assertEquals("custom-tests", tests.get(0));
	}

	@Test
	void getDocuments_returnsSrcSite() {
		// Arrange
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("a");
		model.setVersion("1");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir).model(model);

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(java.util.Collections.singletonList("src/site"), docs);
	}

	@Test
	void getProjectIdAndNameAndParentId_fromModel() {
		// Arrange
		Parent parent = new Parent();
		parent.setGroupId("pg");
		parent.setArtifactId("parent-art");
		parent.setVersion("1");
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("g");
		model.setArtifactId("my-art");
		model.setVersion("1");
		model.setName("My Name");
		model.setParent(parent);

		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir).model(model);

		// Act
		String id = layout.getProjectId();
		String name = layout.getProjectName();
		String parentId = layout.getParentId();

		// Assert
		assertEquals("my-art", id);
		assertEquals("My Name", name);
		assertEquals("parent-art", parentId);
	}

	@Test
	void projectDir_returnsSameTypeForChaining() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout();

		// Act
		MavenProjectLayout chained = layout.projectDir(tempDir);

		// Assert
		org.junit.jupiter.api.Assertions.assertSame(layout, chained);
		assertEquals(tempDir.getAbsolutePath(), chained.getProjectDir().getAbsolutePath());
	}

	private static String minimalPom(String packaging) {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <groupId>org.example</groupId>\n"
				+ "  <artifactId>demo</artifactId>\n"
				+ "  <version>1.0.0</version>\n"
				+ "  <packaging>" + packaging + "</packaging>\n"
				+ "</project>\n";
	}
}
