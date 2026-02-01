package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorTest {

	private static final class RecordingProjectLayout extends ProjectLayout {
		private final List<String> modules;

		private RecordingProjectLayout(List<String> modules) {
			this.modules = modules;
		}

		@Override
		public List<String> getModules() {
			return modules;
		}

		@Override
		public List<String> getSources() {
			return Collections.emptyList();
		}

		@Override
		public List<String> getDocuments() {
			return Collections.emptyList();
		}

		@Override
		public List<String> getTests() {
			return Collections.emptyList();
		}
	}

	private static final class TestProcessor extends ProjectProcessor {
		private final RecordingProjectLayout layout;
		private int processFolderCalls;
		private int processModuleCalls;
		private File lastProjectDir;
		private String lastModule;
		private ProjectLayout lastLayout;
		private boolean throwFromProcessFolder;

		private TestProcessor(RecordingProjectLayout layout) {
			this.layout = layout;
		}

		@Override
		protected ProjectLayout getProjectLayout(File projectDir) {
			lastProjectDir = projectDir;
			return layout.projectDir(projectDir);
		}

		@Override
		protected void processModule(File projectDir, String module) throws IOException {
			processModuleCalls++;
			lastProjectDir = projectDir;
			lastModule = module;
			// Do not recurse into scanFolder to keep the test isolated.
		}

		@Override
		public void processFolder(ProjectLayout processor) {
			processFolderCalls++;
			lastLayout = processor;
			if (throwFromProcessFolder) {
				throw new RuntimeException("boom");
			}
		}
	}

	@Test
	void scanFolder_whenModulesPresent_shouldProcessEachModule() throws Exception {
		// Arrange
		RecordingProjectLayout layout = new RecordingProjectLayout(Arrays.asList("m1", "m2"));
		TestProcessor processor = new TestProcessor(layout);
		File projectDir = new File("target/test-tmp/processor-modules");

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(0, processor.processFolderCalls);
		assertEquals(2, processor.processModuleCalls);
		assertEquals("m2", processor.lastModule);
		assertSame(projectDir, processor.lastProjectDir);
	}

	@Test
	void scanFolder_whenNoModules_shouldInvokeProcessFolderWithDetectedLayout() throws Exception {
		// Arrange
		RecordingProjectLayout layout = new RecordingProjectLayout(null);
		TestProcessor processor = new TestProcessor(layout);
		File projectDir = new File("target/test-tmp/processor-no-modules");

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(1, processor.processFolderCalls);
		assertEquals(0, processor.processModuleCalls);
		assertNotNull(processor.lastLayout);
		assertSame(projectDir, processor.lastLayout.getProjectDir());
	}

	@Test
	void scanFolder_whenProcessFolderThrows_shouldCatchAndNotPropagate() throws Exception {
		// Arrange
		RecordingProjectLayout layout = new RecordingProjectLayout(null);
		TestProcessor processor = new TestProcessor(layout);
		processor.throwFromProcessFolder = true;
		File projectDir = new File("target/test-tmp/processor-processfolder-throws");

		// Act / Assert
		assertDoesNotThrow(() -> processor.scanFolder(projectDir));
		assertEquals(1, processor.processFolderCalls);
	}
}
