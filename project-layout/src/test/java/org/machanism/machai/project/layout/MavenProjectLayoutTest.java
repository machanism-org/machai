package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
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
		File dir = new File("target/test-tmp/maven-project");
		assertTrue(dir.mkdirs() || dir.isDirectory());
		File pom = new File(dir, "pom.xml");
		assertTrue(pom.createNewFile() || pom.isFile());

		// Act
		boolean result = MavenProjectLayout.isMavenProject(dir);

		// Assert
		assertTrue(result);
	}

	@Test
	void isMavenProject_shouldReturnFalseWhenPomXmlMissing() {
		// Arrange
		File dir = new File("target/test-tmp/not-maven-project");
		assertTrue(dir.mkdirs() || dir.isDirectory());

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

		MavenProjectLayout layout = new MavenProjectLayout().model(model).effectivePomRequired(false)
				.projectDir(new File("/repo"));

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(Arrays.asList("module-a", "module-b"), modules);
	}

	@Test
	void getModules_shouldReturnNullWhenPackagingNotPom() {
		// Arrange
		Model model = new Model();
		model.setPackaging("jar");
		model.setModules(Arrays.asList("module-a"));

		MavenProjectLayout layout = new MavenProjectLayout().model(model).effectivePomRequired(false)
				.projectDir(new File("/repo"));

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNull(modules);
	}

	@Test
	void getSources_shouldIncludeSourceDirectoryAndResources() {
		// Arrange
		Build build = new Build();
		build.setSourceDirectory("/repo/src/main/java");
		Resource res = new Resource();
		res.setDirectory("/repo/src/main/resources");
		build.setResources(Collections.singletonList(res));

		Model model = new Model();
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().model(model).projectDir(new File("/repo"));

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertEquals(Arrays.asList("src/main/java", "src/main/resources"), sources);
	}

	@Test
	void getSources_shouldReturnEmptyWhenBuildNull() {
		// Arrange
		Model model = new Model();
		model.setBuild(null);
		MavenProjectLayout layout = new MavenProjectLayout().model(model).projectDir(new File("/repo"));

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNotNull(sources);
		assertTrue(sources.isEmpty());
	}

	@Test
	void getTests_shouldIncludeTestSourceDirectoryAndTestResources() {
		// Arrange
		Build build = new Build();
		build.setTestSourceDirectory("/repo/src/test/java");
		Resource res = new Resource();
		res.setDirectory("/repo/src/test/resources");
		build.setTestResources(Collections.singletonList(res));

		Model model = new Model();
		model.setBuild(build);

		MavenProjectLayout layout = new MavenProjectLayout().model(model).projectDir(new File("/repo"));

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertEquals(Arrays.asList("src/test/java", "src/test/resources"), tests);
	}

	@Test
	void getDocuments_shouldReturnDefaultSiteFolder() {
		// Arrange
		MavenProjectLayout layout = new MavenProjectLayout().model(new Model()).projectDir(new File("/repo"));

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertEquals(Collections.singletonList("src/site"), docs);
	}
}
