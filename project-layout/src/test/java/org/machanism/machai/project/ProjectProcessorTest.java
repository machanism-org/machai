package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorTest {

	@TempDir
	File tempDir;

	@Test
	void scanFolder_whenModulesPresent_processesEachModule() throws Exception {
		// Arrange
		RecordingProcessor processor = new RecordingProcessor();
		processor.layoutToReturn = new FixedModulesLayout(Arrays.asList("a", "b"));

		// Act
		processor.scanFolder(tempDir);

		// Assert
		assertEquals(Arrays.asList("a", "b"), processor.processedModules);
		assertEquals(0, processor.processFolderInvocations);
	}

	@Test
	void scanFolder_whenNoModules_callsProcessFolder() throws Exception {
		// Arrange
		RecordingProcessor processor = new RecordingProcessor();
		processor.layoutToReturn = new FixedModulesLayout(null);

		// Act
		processor.scanFolder(tempDir);

		// Assert
		assertEquals(1, processor.processFolderInvocations);
		assertEquals(0, processor.processedModules.size());
	}

	@Test
	void scanFolder_whenProcessFolderThrows_exceptionIsCaughtAndDoesNotPropagate() throws Exception {
		// Arrange
		RecordingProcessor processor = new RecordingProcessor();
		processor.layoutToReturn = new FixedModulesLayout(null);
		processor.throwInProcessFolder = true;

		// Act + Assert
		assertDoesNotThrow(() -> processor.scanFolder(tempDir));
		assertEquals(1, processor.processFolderInvocations);
	}

	@Test
	void getProjectLayout_whenDirectoryMissing_propagatesFileNotFoundException() {
		// Arrange
		RecordingProcessor processor = new RecordingProcessor();
		File missing = new File(tempDir, "missing");

		// Act + Assert
		assertThrows(FileNotFoundException.class, () -> processor.getProjectLayout(missing));
	}

	static final class RecordingProcessor extends ProjectProcessor {
		ProjectLayout layoutToReturn;
		int processFolderInvocations;
		boolean throwInProcessFolder;
		final java.util.ArrayList<String> processedModules = new java.util.ArrayList<>();

		@Override
		protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
			if (layoutToReturn == null) {
				return super.getProjectLayout(projectDir);
			}
			return layoutToReturn.projectDir(projectDir);
		}

		@Override
		protected void processModule(File projectDir, String module) throws IOException {
			processedModules.add(module);
		}

		@Override
		public void processFolder(ProjectLayout processor) {
			processFolderInvocations++;
			if (throwInProcessFolder) {
				throw new RuntimeException("boom");
			}
		}
	}

	static final class FixedModulesLayout extends ProjectLayout {
		private final List<String> modules;

		FixedModulesLayout(List<String> modules) {
			this.modules = modules;
		}

		@Override
		public List<String> getModules() {
			return modules;
		}

		@Override
		public List<String> getSources() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<String> getDocuments() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<String> getTests() {
			throw new UnsupportedOperationException();
		}
	}
}
