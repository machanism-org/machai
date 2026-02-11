package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorTest {

	@TempDir
	File tempDir;

	@Test
	void scanFolder_shouldProcessEachModuleWhenModulesPresent() throws IOException {
		// Arrange
		File moduleA = new File(tempDir, "module-a");
		File moduleB = new File(tempDir, "module-b");
		assertTrue(moduleA.mkdirs());
		assertTrue(moduleB.mkdirs());

		TestProcessor processor = new TestProcessor();
		processor.modulesToReturn.add("module-a");
		processor.modulesToReturn.add("module-b");

		// Act
		processor.scanFolder(tempDir);

		// Assert
		assertEquals(1, processor.getProjectLayoutCalls);
		assertEquals(2, processor.processModuleCalls);
		assertTrue(processor.processedModuleDirs.contains(moduleA));
		assertTrue(processor.processedModuleDirs.contains(moduleB));
		assertEquals(0, processor.processFolderCalls);
	}

	@Test
	void scanFolder_shouldProcessFolderWhenModulesNull() throws IOException {
		// Arrange
		TestProcessor processor = new TestProcessor();
		processor.returnModulesNull = true;

		// Act
		processor.scanFolder(tempDir);

		// Assert
		assertEquals(2, processor.getProjectLayoutCalls);
		assertEquals(0, processor.processModuleCalls);
		assertEquals(1, processor.processFolderCalls);
	}

	@Test
	void scanFolder_shouldSwallowProcessFolderExceptionAndContinue() throws IOException {
		// Arrange
		TestProcessor processor = new TestProcessor();
		processor.returnModulesNull = true;
		processor.throwInProcessFolder = true;

		// Act
		assertDoesNotThrow(() -> processor.scanFolder(tempDir));

		// Assert
		assertEquals(2, processor.getProjectLayoutCalls);
		assertEquals(1, processor.processFolderCalls);
	}

	private static final class TestProcessor extends ProjectProcessor {
		int getProjectLayoutCalls;
		int processFolderCalls;
		int processModuleCalls;

		final List<String> modulesToReturn = new ArrayList<String>();
		final List<File> processedModuleDirs = new ArrayList<File>();

		boolean returnModulesNull;
		boolean throwInProcessFolder;

		@Override
		protected ProjectLayout getProjectLayout(File projectDir) {
			getProjectLayoutCalls++;
			return new ProjectLayout() {
				@Override
				public List<String> getModules() {
					return returnModulesNull ? null : modulesToReturn;
				}

				@Override
				public List<String> getSources() {
					return null;
				}

				@Override
				public List<String> getDocuments() {
					return null;
				}

				@Override
				public List<String> getTests() {
					return null;
				}
			}.projectDir(projectDir);
		}

		@Override
		protected void processModule(File projectDir, String module) throws IOException {
			processModuleCalls++;
			processedModuleDirs.add(new File(projectDir, module));
			// Do not recurse to isolate scanFolder behavior.
		}

		@Override
		public void processFolder(ProjectLayout processor) {
			processFolderCalls++;
			if (throwInProcessFolder) {
				throw new RuntimeException("boom");
			}
		}
	}
}
