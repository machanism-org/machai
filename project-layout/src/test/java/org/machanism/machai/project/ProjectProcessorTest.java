package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorTest {

	@TempDir
	File tempDir;

	@Test
	void scanFolder_shouldInvokeProcessFolderWhenModulesNull() throws Exception {
		// Arrange
		TestProcessor processor = new TestProcessor();
		processor.layoutToReturn = new TestLayout(tempDir, null);

		// Act
		processor.scanFolder(tempDir);

		// Assert
		assertEquals(1, processor.processFolderCalls);
		assertSame(processor.layoutToReturn, processor.lastProcessedLayout);
	}

	@Test
	void scanFolder_shouldInvokeProcessModuleForEachModuleWhenModulesNonNull() throws Exception {
		// Arrange
		TestProcessor processor = new TestProcessor();
		processor.layoutToReturn = new TestLayout(tempDir, java.util.Arrays.asList("m1", "m2"));

		// Act
		processor.scanFolder(tempDir);

		// Assert
		assertEquals(java.util.Arrays.asList("m1", "m2"), processor.processedModules);
		assertEquals(0, processor.processFolderCalls);
	}

	@Test
	void getProjectLayout_shouldDelegateToProjectLayoutManager() throws Exception {
		// Arrange
		TestProcessor processor = new TestProcessor();

		// Act
		ProjectLayout layout = processor.getProjectLayout(tempDir);

		// Assert
		assertNotNull(layout);
		assertEquals(tempDir, layout.getProjectDir());
	}

	@Test
	void scanFolder_shouldPropagateFileNotFoundExceptionFromGetProjectLayout() {
		// Arrange
		TestProcessor processor = new TestProcessor();
		processor.throwFromGetProjectLayout = true;

		// Act
		FileNotFoundException ex = assertThrows(FileNotFoundException.class, () -> processor.scanFolder(tempDir));

		// Assert
		assertEquals("boom", ex.getMessage());
	}

	private static class TestProcessor extends ProjectProcessor {
		int processFolderCalls;
		ProjectLayout lastProcessedLayout;
		final java.util.List<String> processedModules = new java.util.ArrayList<String>();
		TestLayout layoutToReturn;
		boolean throwFromGetProjectLayout;

		@Override
		public void processFolder(ProjectLayout processor) {
			processFolderCalls++;
			lastProcessedLayout = processor;
		}

		@Override
		protected void processModule(File projectDir, String module) {
			processedModules.add(module);
			// Intentionally do not delegate to super.processModule to avoid recursive scanFolder calls.
		}

		@Override
		public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
			if (throwFromGetProjectLayout) {
				throw new FileNotFoundException("boom");
			}
			if (layoutToReturn != null) {
				return layoutToReturn;
			}
			return super.getProjectLayout(projectDir);
		}
	}

	private static class TestLayout extends ProjectLayout {
		private final File dir;
		private final java.util.List<String> modules;

		private TestLayout(File dir, java.util.List<String> modules) {
			this.dir = dir;
			this.modules = modules;
		}

		@Override
		public File getProjectDir() {
			return dir;
		}

		@Override
		public java.util.List<String> getModules() {
			return modules;
		}

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
