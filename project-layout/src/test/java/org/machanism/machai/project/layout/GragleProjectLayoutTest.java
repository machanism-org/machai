package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GragleProjectLayoutTest {

	@TempDir
	Path tempDir;

	@Test
	void isGradleProject_shouldReturnTrueWhenBuildGradleExists() throws IOException {
		// Arrange
		Files.write(tempDir.resolve("build.gradle"), "".getBytes(StandardCharsets.UTF_8));

		// Act
		boolean result = GragleProjectLayout.isGradleProject(tempDir.toFile());

		// Assert
		assertTrue(result);
	}

	@Test
	void isGradleProject_shouldReturnFalseWhenBuildGradleMissing() {
		// Arrange
		// no build.gradle

		// Act
		boolean result = GragleProjectLayout.isGradleProject(tempDir.toFile());

		// Assert
		assertFalse(result);
	}

	@Test
	void getSources_getTests_getDocuments_shouldReturnConventionalRoots() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act / Assert
		assertEquals(Collections.singletonList("src/main"), layout.getSources());
		assertEquals(Collections.singletonList("src/test"), layout.getTests());
		assertEquals(Collections.singletonList("src/site"), layout.getDocuments());
	}

	@Test
	void getModules_shouldReturnEmptyListWhenProjectModelCannotBeLoaded() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertTrue(modules.isEmpty());
	}

	@Test
	void getProjectId_getProjectName_shouldReturnEmptyStringWhenProjectModelCannotBeLoaded() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());

		// Act
		String id = layout.getProjectId();
		String name = layout.getProjectName();

		// Assert
		assertEquals("", id);
		assertEquals("", name);
	}

	@Test
	void projectDir_shouldReturnGragleProjectLayoutForFluentChaining() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout();

		// Act
		GragleProjectLayout returned = layout.projectDir(tempDir.toFile());

		// Assert
		assertSame(layout, returned);
		assertEquals(tempDir.toFile(), returned.getProjectDir());
	}

	@Test
	void getModules_shouldReturnChildProjectNamesWhenProjectIsProvided() {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir.toFile());
		setProject(layout, gradleProjectWithChildren("a", "b"));

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertEquals(Arrays.asList("a", "b"), modules);
	}

	private static void setProject(GragleProjectLayout layout, GradleProject project) {
		try {
			java.lang.reflect.Field field = GragleProjectLayout.class.getDeclaredField("project");
			field.setAccessible(true);
			field.set(layout, project);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static GradleProject gradleProjectWithChildren(String... childNames) {
		return (GradleProject) Proxy.newProxyInstance(GradleProject.class.getClassLoader(),
				new Class<?>[] { GradleProject.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "getChildren":
						return domainObjectSetOfProjects(childNames);
					case "getName":
						return "root";
					default:
						return defaultValue(method.getReturnType());
					}
				});
	}

	@SuppressWarnings("unchecked")
	private static DomainObjectSet<GradleProject> domainObjectSetOfProjects(String... names) {
		List<GradleProject> children = Arrays.stream(names).map(GragleProjectLayoutTest::leafProject)
				.collect(Collectors.toList());
		return (DomainObjectSet<GradleProject>) Proxy.newProxyInstance(DomainObjectSet.class.getClassLoader(),
				new Class<?>[] { DomainObjectSet.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "isEmpty":
						return children.isEmpty();
					case "getAll":
						return children;
					case "iterator":
						return children.iterator();
					case "size":
						return children.size();
					case "getAt":
						return children.get((Integer) args[0]);
					default:
						return defaultValue(method.getReturnType());
					}
				});
	}

	private static GradleProject leafProject(String name) {
		return (GradleProject) Proxy.newProxyInstance(GradleProject.class.getClassLoader(),
				new Class<?>[] { GradleProject.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "getName":
						return name;
					case "getChildren":
						return emptyDomainObjectSet();
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
					case "size":
						return 0;
					case "getAt":
						throw new IndexOutOfBoundsException("empty");
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
