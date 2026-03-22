package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectLayoutDefaultMethodsTest {

	@TempDir
	File tempDir;

	@Test
	void getProjectName_shouldReturnNullByDefault() {
		// Arrange
		ProjectLayout layout = new MinimalLayout().projectDir(tempDir);

		// Act
		String name = layout.getProjectName();

		// Assert
		assertNull(name);
	}

	@Test
	void getProjectId_shouldReturnNullByDefault() {
		// Arrange
		ProjectLayout layout = new MinimalLayout().projectDir(tempDir);

		// Act
		String id = layout.getProjectId();

		// Assert
		assertNull(id);
	}

	@Test
	void getParentId_shouldReturnNullByDefault() {
		// Arrange
		ProjectLayout layout = new MinimalLayout().projectDir(tempDir);

		// Act
		String parentId = layout.getParentId();

		// Assert
		assertNull(parentId);
	}

	private static class MinimalLayout extends ProjectLayout {
		@Override
		public java.util.List<String> getSources() {
			return java.util.Collections.emptyList();
		}

		@Override
		public java.util.List<String> getDocuments() {
			return java.util.Collections.emptyList();
		}

		@Override
		public java.util.List<String> getTests() {
			return java.util.Collections.emptyList();
		}
	}
}
