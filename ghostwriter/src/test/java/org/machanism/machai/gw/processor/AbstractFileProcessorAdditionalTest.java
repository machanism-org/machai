package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.ProjectLayout;

class AbstractFileProcessorAdditionalTest {

	@TempDir
	Path tempDir;

	private Processor processor;

	@BeforeEach
	void setUp() {
		processor = new Processor(tempDir.toFile(), new PropertiesConfigurator());
	}

	@Test
	void scanFolder_whenNonRecursive_skipsModuleTraversal_butProcessesParentFiles() throws Exception {
		// Arrange
		File projectDir = tempDir.resolve("project").toFile();
		assertTrue(projectDir.mkdirs());

		processor.setNonRecursive(true);
		processor.layout = new ProjectLayoutStub().projectDir(projectDir);

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(1, processor.parentFilesInvocations);
		assertEquals(0, processor.moduleInvocations);
	}

	@Test
	void scanFolder_whenConcurrencyGreaterThanOne_usesMultiThreadedModuleProcessing() throws Exception {
		// Arrange
		File projectDir = tempDir.resolve("project").toFile();
		assertTrue(projectDir.mkdirs());

		processor.setNonRecursive(false);
		processor.setDegreeOfConcurrency(2);

		ProjectLayoutStub layout = (ProjectLayoutStub) new ProjectLayoutStub().projectDir(projectDir);
		layout.modules = Arrays.asList("m1", "m2");
		processor.layout = layout;

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(1, processor.multiThreadedInvocations);
		assertEquals(0, processor.moduleInvocations);
		assertEquals(1, processor.parentFilesInvocations);
	}

	@Test
	void findFiles_sortsByPathDepthDescending_andExcludesExcludeDirs() throws Exception {
		// Arrange
		File projectDir = tempDir.resolve("project").toFile();
		assertTrue(projectDir.mkdirs());

		File targetDir = new File(projectDir, "target");
		assertTrue(targetDir.mkdirs());
		Files.write(new File(targetDir, "ignored.txt").toPath(), Arrays.asList("x"), StandardCharsets.UTF_8);

		File deepDir = new File(projectDir, "a/b/c");
		assertTrue(deepDir.mkdirs());
		File deepFile = new File(deepDir, "deep.txt");
		Files.write(deepFile.toPath(), Arrays.asList("x"), StandardCharsets.UTF_8);

		File shallowFile = new File(projectDir, "root.txt");
		Files.write(shallowFile.toPath(), Arrays.asList("x"), StandardCharsets.UTF_8);

		// Act
		List<File> files = processor.findFiles(projectDir);

		// Assert
		assertFalse(files.stream().anyMatch(f -> f.getPath().contains("target")), "Excluded dirs must be omitted");
		assertTrue(files.contains(deepFile));
		assertTrue(files.contains(shallowFile));
		assertTrue(files.indexOf(deepFile) < files.indexOf(shallowFile));
	}

	@Test
	void shouldExcludePath_whenExcludesContainsNull_entries_areIgnored() {
		// Arrange
		processor.setExcludes(new String[] { null, "glob:**/*.tmp" });

		// Act + Assert
		assertTrue(processor.shouldExcludePath(new File("a/b/c.tmp").toPath()));
		assertFalse(processor.shouldExcludePath(new File("a/b/c.txt").toPath()));
	}

	private static final class Processor extends AbstractFileProcessor {
		private int parentFilesInvocations;
		private int moduleInvocations;
		private int multiThreadedInvocations;
		private ProjectLayout layout;

		private Processor(File rootDir, PropertiesConfigurator configurator) {
			super(rootDir, configurator);
		}

		@Override
		public ProjectLayout getProjectLayout(File projectDir) {
			return layout != null ? layout : new ProjectLayoutStub().projectDir(projectDir);
		}

		@Override
		protected void processParentFiles(ProjectLayout projectLayout) throws IOException {
			parentFilesInvocations++;
		}

		@Override
		protected void processModule(File projectDir, String module) throws IOException {
			moduleInvocations++;
		}

		@Override
		void processModulesMultiThreaded(File projectDir, List<String> modules) {
			multiThreadedInvocations++;
		}
	}

	private static final class ProjectLayoutStub extends ProjectLayout {
		private List<String> modules = Collections.emptyList();

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
}
