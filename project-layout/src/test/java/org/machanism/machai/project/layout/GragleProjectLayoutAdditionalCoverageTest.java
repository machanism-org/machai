package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GragleProjectLayoutAdditionalCoverageTest {

	@TempDir
	File tempDir;

	@Test
	void getModules_shouldReturnEmptyListWhenProjectHasNoChildren() throws Exception {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout().projectDir(tempDir);
		setProject(layout, gradleProject("root", emptyDomainObjectSet()));

		// Act
		java.util.List<String> modules = layout.getModules();

		// Assert
		assertEquals(Collections.emptyList(), modules);
	}

	@Test
	void getProjectMethod_shouldReturnNullWhenProjectDirIsNotSet() throws Exception {
		// Arrange
		GragleProjectLayout layout = new GragleProjectLayout();

		// Act
		Object project = invokeGetProject(layout);

		// Assert
		assertNull(project);
	}

	private static Object invokeGetProject(GragleProjectLayout layout) throws Exception {
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
