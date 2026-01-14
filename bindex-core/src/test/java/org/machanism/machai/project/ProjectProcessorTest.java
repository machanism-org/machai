package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorTest {

	private static final class RecordingProcessor extends ProjectProcessor {
		private final ProjectLayout layout;
		private int processFolderCalls;
		private int processModuleCalls;
		private File lastModuleProjectDir;
		private String lastModule;

		private RecordingProcessor(ProjectLayout layout) {
			this.layout = layout;
		}

		@Override
		protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
			return layout.projectDir(projectDir);
		}

		@Override
		protected void processModule(File projectDir, String module) throws IOException {
			processModuleCalls++;
			lastModuleProjectDir = projectDir;
			lastModule = module;
			// Do not recurse into scanFolder; isolate this unit.
		}

		@Override
		public void processFolder(ProjectLayout processor) {
			processFolderCalls++;
		}
	}

	private static final class ModulesLayout extends ProjectLayout {
		private final List<String> modules;

		private ModulesLayout(List<String> modules) {
			this.modules = modules;
		}

		@Override
		public List<String> getModules() {
			return modules;
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
	}

	private static final class ThrowingFolderProcessor extends ProjectProcessor {
		private final ProjectLayout layout;
		private int processFolderCalls;

		private ThrowingFolderProcessor(ProjectLayout layout) {
			this.layout = layout;
		}

		@Override
		protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
			return layout.projectDir(projectDir);
		}

		@Override
		public void processFolder(ProjectLayout processor) {
			processFolderCalls++;
			throw new RuntimeException("boom");
		}
	}

	@Test
	void scanFolder_whenModulesPresent_processesEachModuleAndDoesNotProcessFolder(@TempDir Path tempDir)
			throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		Files.createDirectories(tempDir.resolve("m1"));
		Files.createDirectories(tempDir.resolve("m2"));

		ProjectLayout layout = new ModulesLayout(Arrays.asList("m1", "m2"));
		RecordingProcessor processor = new RecordingProcessor(layout);

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(2, processor.processModuleCalls);
		assertEquals(0, processor.processFolderCalls);
		assertEquals(projectDir, processor.lastModuleProjectDir);
		assertEquals("m2", processor.lastModule);
	}

	@Test
	void scanFolder_whenNoModules_processesFolderAndSwallowsExceptions(@TempDir Path tempDir) throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		ProjectLayout layout = new ModulesLayout(null);
		ThrowingFolderProcessor processor = new ThrowingFolderProcessor(layout);

		// Act + Assert
		assertDoesNotThrow(() -> processor.scanFolder(projectDir));
		assertEquals(1, processor.processFolderCalls);
	}

	@Test
	void getProjectLayout_whenProjectDirDoesNotExist_throwsFileNotFoundException() {
		// Arrange
		ProjectProcessor processor = new ProjectProcessor() {
			@Override
			public void processFolder(ProjectLayout processor) {
				// not used
			}
		};
		File missing = new File("target/definitely-missing-dir-" + System.nanoTime());

		// Act + Assert
		assertThrows(FileNotFoundException.class, () -> processor.getProjectLayout(missing));
	}

	@Test
	void constant_machaiTempDir_hasExpectedValue() {
		// Arrange
		String expected = ".machai";

		// Act
		String actual = ProjectProcessor.MACHAI_TEMP_DIR;

		// Assert
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
