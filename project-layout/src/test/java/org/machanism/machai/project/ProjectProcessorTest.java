package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorTest {

	@Test
	void scanFolder_whenModulesPresent_processesEachModuleAndDoesNotCallProcessFolderOnRootScan() throws Exception {
		// Arrange
		File projectDir = new File("src/test/resources/mockProjectDir");
		List<String> modules = Arrays.asList("moduleA", "moduleB");

		TestProcessor processor = new TestProcessor(new StubLayout(modules), new StubLayout(null));

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(Arrays.asList("moduleA", "moduleB"), processor.processedModules);
		assertEquals(3, processor.scanFolderInvocationCount);
		assertEquals(2, processor.processFolderCallCount);
	}

	@Test
	void scanFolder_whenModulesEmptyList_doesNotProcessModulesAndDoesNotCallProcessFolder() throws Exception {
		// Arrange
		File projectDir = new File("src/test/resources/mockProjectDir");
		TestProcessor processor = new TestProcessor(new StubLayout(Collections.<String>emptyList()), new StubLayout(null));

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(Collections.<String>emptyList(), processor.processedModules);
		assertEquals(1, processor.scanFolderInvocationCount);
		assertEquals(0, processor.processFolderCallCount);
	}

	@Test
	void scanFolder_whenNoModules_processesFolderOnce() throws Exception {
		// Arrange
		File projectDir = new File("src/test/resources/mockProjectDir");
		TestProcessor processor = new TestProcessor(new StubLayout(null), new StubLayout(null));

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(1, processor.processFolderCallCount);
		assertEquals(1, processor.scanFolderInvocationCount);
	}

	@Test
	void scanFolder_whenProcessFolderThrowsException_itIsCaughtAndDoesNotPropagate() throws Exception {
		// Arrange
		File projectDir = new File("src/test/resources/mockProjectDir");
		TestProcessor processor = new TestProcessor(new StubLayout(null), new StubLayout(null));
		processor.throwFromProcessFolder = true;

		// Act + Assert
		assertDoesNotThrow(() -> processor.scanFolder(projectDir));
		assertEquals(1, processor.processFolderCallCount);
	}

	@Test
	void scanFolder_whenGetProjectLayoutThrowsFileNotFoundException_propagates() {
		// Arrange
		File projectDir = new File("src/test/resources/mockProjectDir");
		TestProcessor processor = new TestProcessor(new StubLayout(null), new StubLayout(null));
		processor.throwFromGetProjectLayout = true;

		// Act + Assert
		assertThrows(FileNotFoundException.class, () -> processor.scanFolder(projectDir));
		assertEquals(1, processor.getProjectLayoutInvocationCount);
	}

	private static final class StubLayout extends ProjectLayout {
		private final List<String> modules;

		private StubLayout(List<String> modules) {
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

	private static final class TestProcessor extends ProjectProcessor {
		private final ProjectLayout firstLayout;
		private final ProjectLayout secondLayout;

		private int getProjectLayoutInvocationCount;
		private boolean throwFromGetProjectLayout;

		private int processFolderCallCount;
		private final java.util.ArrayList<String> processedModules = new java.util.ArrayList<>();
		private int scanFolderInvocationCount;
		private boolean throwFromProcessFolder;

		private TestProcessor(ProjectLayout firstLayout, ProjectLayout secondLayout) {
			this.firstLayout = firstLayout;
			this.secondLayout = secondLayout;
		}

		@Override
		protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
			getProjectLayoutInvocationCount++;
			if (throwFromGetProjectLayout) {
				throw new FileNotFoundException("missing");
			}

			ProjectLayout layout = (getProjectLayoutInvocationCount == 1) ? firstLayout : secondLayout;
			layout.projectDir(projectDir);
			return layout;
		}

		@Override
		public void processFolder(ProjectLayout processor) {
			processFolderCallCount++;
			if (throwFromProcessFolder) {
				throw new RuntimeException("boom");
			}
		}

		@Override
		public void scanFolder(File projectDir) throws IOException {
			scanFolderInvocationCount++;
			super.scanFolder(projectDir);
		}

		@Override
		protected void processModule(File projectDir, String module) throws IOException {
			processedModules.add(module);
			super.processModule(projectDir, module);
		}
	}
}
