package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.apache.maven.model.Model;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CoverageGapAdditionalTest {

	@TempDir
	Path tempDir;

	@Test
	void projectLayoutGetRelativePathStaticReturnsNullForOutsideFileWhenAddSingleDotFalse() {
		// Arrange
		File baseDir = tempDir.resolve("base").toFile();
		File outsideFile = tempDir.resolveSibling("outside-file.txt").toFile();

		// Act
		String relativePath = ProjectLayout.getRelativePath(baseDir, outsideFile, false);

		// Assert
		assertNull(relativePath);
	}

	@Test
	void pomReaderGetProjectModelThrowsForMissingPomFile() {
		// Arrange
		PomReader reader = new PomReader();
		File missingPom = tempDir.resolve("missing.xml").toFile();

		// Act
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> reader.getProjectModel(missingPom));

		// Assert
		assertTrue(exception.getMessage().contains("POM file:"));
	}

	@Test
	void jscriptGetModulesReturnsMatchingWorkspaceDirectories() throws Exception {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{\"name\":\"root\",\"workspaces\":[\"*\"]}"
				.getBytes(StandardCharsets.UTF_8));
		Path childDir = Files.createDirectories(tempDir.resolve("child-module"));
		Files.write(childDir.resolve("package.json"), "{}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		Collection<String> modules = layout.getModules();

		// Assert
		assertEquals(Collections.singletonList("child-module"), modules);
	}

	@Test
	void gragleGetProjectReturnsNullWhenToolingApiFails() throws Exception {
		// Arrange
		Path projectDir = Files.createDirectories(tempDir.resolve("gradle-project"));
		Files.write(projectDir.resolve("build.gradle"), "plugins {}".getBytes(StandardCharsets.UTF_8));
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(projectDir.toFile());

		// Act
		Object project = invokePrivateGetProject(layout);

		// Assert
		assertNull(project);
	}

	@Test
	void gragleGetProjectIdAndNameReturnEmptyStringWhenCachedProjectIsNull() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act
		String projectId = layout.getProjectId();
		String projectName = layout.getProjectName();

		// Assert
		assertEquals("", projectId);
		assertEquals("", projectName);
	}

	@Test
	void gragleGetModulesReturnsChildProjectNamesWhenChildrenExist() throws Exception {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());
		DomainObjectSet<GradleProject> children = domainObjectSet(gradleProject("module-a", emptyDomainObjectSet()),
				gradleProject("module-b", emptyDomainObjectSet()));
		setProject(layout, gradleProject("root", children));

		// Act
		Collection<String> modules = layout.getModules();

		// Assert
		assertEquals(2, modules.size());
		assertTrue(modules.contains("module-a"));
		assertTrue(modules.contains("module-b"));
	}

	@Test
	void mavenGetSourcesKeepsRelativeResourceDirectoriesAndConvertsAbsoluteOnes() {
		// Arrange
		Path projectDir = tempDir.resolve("maven-project");
		projectDir.toFile().mkdirs();
		Model model = new Model();
		model.setArtifactId("artifact");
		org.apache.maven.model.Build build = new org.apache.maven.model.Build();
		build.setSourceDirectory(projectDir.resolve("src/main/java").toFile().getAbsolutePath());
		org.apache.maven.model.Resource relativeResource = new org.apache.maven.model.Resource();
		relativeResource.setDirectory("src/main/resources");
		org.apache.maven.model.Resource absoluteResource = new org.apache.maven.model.Resource();
		absoluteResource.setDirectory(projectDir.resolve("src/shared/resources").toFile().getAbsolutePath());
		build.addResource(relativeResource);
		build.addResource(absoluteResource);
		model.setBuild(build);
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(projectDir.toFile()).model(model);

		// Act
		Collection<String> sources = layout.getSources();

		// Assert
		assertTrue(sources.contains("src/main/java"));
		assertTrue(sources.contains("src/main/resources"));
		assertTrue(sources.contains("src/shared/resources"));
	}

	@Test
	void mavenGetTestsReturnsEmptyListWhenBuildIsNull() {
		// Arrange
		Model model = new Model();
		model.setArtifactId("artifact");
		MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

		// Act
		Collection<String> tests = layout.getTests();

		// Assert
		assertNotNull(tests);
		assertTrue(tests.isEmpty());
	}

	@Test
	void jscriptGetProjectIdReturnsNameFromPackageJson() throws Exception {
		// Arrange
		Files.write(tempDir.resolve("package.json"), "{\"name\":\"workspace-root\"}".getBytes(StandardCharsets.UTF_8));
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

		// Act
		String projectId = layout.getProjectId();

		// Assert
		assertEquals("workspace-root", projectId);
	}

	private static Object invokePrivateGetProject(GragleProjectLayout layout) throws Exception {
		Method method = GragleProjectLayout.class.getDeclaredMethod("getProject");
		method.setAccessible(true);
		return method.invoke(layout);
	}

	private static void setProject(GragleProjectLayout layout, GradleProject project) throws Exception {
		Field field = GragleProjectLayout.class.getDeclaredField("project");
		field.setAccessible(true);
		field.set(layout, project);
	}

	private static GradleProject gradleProject(String name, DomainObjectSet<GradleProject> children) {
		return (GradleProject) Proxy.newProxyInstance(GradleProject.class.getClassLoader(),
				new Class<?>[] { GradleProject.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "getName":
						return name;
					case "getChildren":
						return children;
					default:
						return defaultValue(method.getReturnType());
					}
				});
	}

	@SuppressWarnings("unchecked")
	private static <T> DomainObjectSet<T> emptyDomainObjectSet() {
		return (DomainObjectSet<T>) Proxy.newProxyInstance(DomainObjectSet.class.getClassLoader(),
				new Class<?>[] { DomainObjectSet.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "isEmpty":
						return true;
					case "getAll":
						return Collections.emptyList();
					case "iterator":
						return Collections.emptyIterator();
					default:
						return defaultValue(method.getReturnType());
					}
				});
	}

	@SuppressWarnings("unchecked")
	private static <T> DomainObjectSet<T> domainObjectSet(T... items) {
		return (DomainObjectSet<T>) Proxy.newProxyInstance(DomainObjectSet.class.getClassLoader(),
				new Class<?>[] { DomainObjectSet.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "isEmpty":
						return items.length == 0;
					case "getAll":
						return java.util.Arrays.asList(items);
					case "iterator":
						return java.util.Arrays.asList(items).iterator();
					default:
						return defaultValue(method.getReturnType());
					}
				});
	}

	private static Object defaultValue(Class<?> returnType) {
		if (!returnType.isPrimitive()) {
			return null;
		}
		if (returnType == boolean.class) {
			return false;
		}
		if (returnType == byte.class) {
			return (byte) 0;
		}
		if (returnType == short.class) {
			return (short) 0;
		}
		if (returnType == int.class) {
			return 0;
		}
		if (returnType == long.class) {
			return 0L;
		}
		if (returnType == float.class) {
			return 0f;
		}
		if (returnType == double.class) {
			return 0d;
		}
		if (returnType == char.class) {
			return (char) 0;
		}
		return null;
	}
}
