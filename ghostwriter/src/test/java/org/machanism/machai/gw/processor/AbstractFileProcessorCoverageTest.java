package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Additional coverage tests for {@link AbstractFileProcessor}.
 */
class AbstractFileProcessorCoverageTest {

	@TempDir
	Path tempDir;

	private static AbstractFileProcessor newProcessor(File projectDir) {
		return new AbstractFileProcessor(projectDir, new PropertiesConfigurator()) {
			@Override
			protected void processParentFiles(ProjectLayout projectLayout) {
				// no-op
			}
		};
	}

	@Test
	void isModuleDir_whenModulesNullOrDirNull_returnsFalse() {
		// Arrange
		ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

		// Act + Assert
		assertFalse(AbstractFileProcessor.isModuleDir(layout, tempDir.toFile()));
		assertFalse(AbstractFileProcessor.isModuleDir(layout, null));
	}

	@Test
	void shutdownExecutor_whenAwaitTerminationInterrupted_preservesInterruptAndShutdownNow() {
		// Arrange
		AbstractFileProcessor processor = newProcessor(tempDir.toFile());
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Thread.currentThread().interrupt();

		// Act
		processor.shutdownExecutor(executor);

		// Assert
		assertTrue(Thread.currentThread().isInterrupted(), "Interrupt flag should be preserved");
		Thread.interrupted();
	}

	@Test
	void findFiles_whenListFilesReturnsNull_throwsIOException() throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		AbstractFileProcessor processor = newProcessor(projectDir);
		File notADir = Files.createTempFile(tempDir, "f", ".txt").toFile();

		// Act
		assertEquals(Collections.emptyList(), processor.findFiles(notADir), "Non-directory should yield empty result");

		File trickyDir = new File(projectDir, "tricky") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isDirectory() {
				return true;
			}

			@Override
			public File[] listFiles() {
				return null;
			}
		};

		// Assert
		IOException ex = assertThrows(IOException.class, () -> processor.findFiles(trickyDir));
		assertTrue(ex.getMessage().contains("Unable to list files"));
	}

	@Test
	void shouldExcludePath_whenNullOrNoExcludes_returnsFalse() {
		// Arrange
		AbstractFileProcessor processor = newProcessor(tempDir.toFile());

		// Act + Assert
		assertFalse(processor.shouldExcludePath(null));
		assertFalse(processor.shouldExcludePath(new File("x").toPath()));
	}

	@Test
	void shouldExcludePath_whenExactPathOrFileName_matches() {
		// Arrange
		AbstractFileProcessor processor = newProcessor(tempDir.toFile());
		processor.setExcludes(new String[] { "a\\b\\c.txt", "d.txt" });

		// Act + Assert
		assertTrue(processor.shouldExcludePath(new File("a\\b\\c.txt").toPath()));
		assertTrue(processor.shouldExcludePath(new File("x\\d.txt").toPath()), "Exclude should match by file name as well");
	}

	@Test
	void shouldExcludePath_whenGlobPattern_matches() {
		// Arrange
		AbstractFileProcessor processor = newProcessor(tempDir.toFile());
		processor.setExcludes(new String[] { "glob:**/*.md" });

		// Act + Assert
		assertTrue(processor.shouldExcludePath(new File("docs/readme.md").toPath()));
		assertFalse(processor.shouldExcludePath(new File("docs/readme.txt").toPath()));
	}

	@Test
	void getPatternPath_whenBlankOrNotPattern_returnsNull() {
		// Arrange + Act + Assert
		assertNull(AbstractFileProcessor.getPatternPath(null));
		assertNull(AbstractFileProcessor.getPatternPath(""));
		assertNull(AbstractFileProcessor.getPatternPath("src"));
	}

	@Test
	void pathDepth_whenBlank_returnsZero_andCountsSegments() {
		// Arrange + Act + Assert
		assertEquals(0, AbstractFileProcessor.pathDepth(null));
		assertEquals(0, AbstractFileProcessor.pathDepth(" "));
		assertEquals(3, AbstractFileProcessor.pathDepth("a/b/c"));
		assertEquals(3, AbstractFileProcessor.pathDepth("a\\b\\c"), "Windows separators should be normalized");
	}

	@Test
	void matchPath_whenPathMatcherNull_matchesOnlyExactScanDir() {
		// Arrange
		File projectDir = tempDir.toFile();
		AbstractFileProcessor processor = newProcessor(projectDir);
		File scanDir = new File(projectDir, "src");
		processor.setScanDir(scanDir);

		File file = new File(projectDir, "src");

		// Act
		boolean match = processor.matchPath(projectDir, file, "", ".");

		// Assert
		assertTrue(match, "When no PathMatcher is set, exact scanDir match should be accepted");
	}

	@Test
	void matchPath_whenRelativePathFromScanDirIsNull_returnsFalse() {
		// Arrange
		File projectDir = tempDir.toFile();
		AbstractFileProcessor processor = newProcessor(projectDir);
		processor.setScanDir(projectDir);
		processor.setPathMatcher(java.nio.file.FileSystems.getDefault().getPathMatcher("glob:**/*.java"));

		File outside = new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile();
		if (outside.getAbsolutePath().startsWith(projectDir.getAbsolutePath())) {
			outside = new File(outside, "outside-" + System.nanoTime());
		}

		// Act
		boolean match = processor.matchPath(projectDir, outside, "", "outside.txt");

		// Assert
		assertFalse(match);
	}

	@Test
	void findFilesWithPattern_whenRelativeDirDoesNotExist_returnsEmpty() throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		AbstractFileProcessor processor = newProcessor(projectDir);

		// Act
		assertEquals(Collections.emptyList(), processor.findFiles(projectDir, "missing"));
	}

	@Test
	void findFilesWithPattern_whenGlobPattern_includesMatchingFiles() throws Exception {
		// Arrange
		File projectDir = tempDir.toFile();
		Files.createDirectories(tempDir.resolve("a"));
		Files.write(tempDir.resolve("a/x.md"), Arrays.asList("x"));
		Files.write(tempDir.resolve("a/y.txt"), Arrays.asList("y"));

		AbstractFileProcessor processor = newProcessor(projectDir);
		processor.setExcludes(new String[] { "glob:**/*.txt" });

		// Act
		java.util.List<File> files = processor.findFiles(projectDir, "glob:**/*.*");

		// Assert
		assertTrue(files.stream().anyMatch(f -> f.getName().equals("x.md")));
		assertFalse(files.stream().anyMatch(f -> f.getName().equals("y.txt")), "Excluded txt should not be present");
	}

	@Test
	void processModulesMultiThreaded_whenFutureInterrupted_throwsAndPreservesInterrupt() {
		// Arrange
		File projectDir = tempDir.toFile();
		AbstractFileProcessor processor = newProcessor(projectDir);
		processor.setDegreeOfConcurrency(2);

		// Act + Assert (Sonar java:S5778: isolate the single invocation which may throw)
		java.util.concurrent.Callable<Void> call = () -> {
			Thread.currentThread().interrupt();
			processor.processModulesMultiThreaded(projectDir, Collections.singletonList("m"));
			return null;
		};

		IllegalStateException ex = assertThrows(IllegalStateException.class, call::call);
		assertTrue(ex.getMessage().contains("interrupted"));
		assertTrue(Thread.currentThread().isInterrupted());
		Thread.interrupted();
	}

	@Test
	void setModuleThreadTimeoutMinutes_whenNonPositive_throwsIllegalArgumentException() {
		// Arrange
		AbstractFileProcessor processor = newProcessor(tempDir.toFile());

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> processor.setModuleThreadTimeoutMinutes(0));
		assertThrows(IllegalArgumentException.class, () -> processor.setModuleThreadTimeoutMinutes(-1));
	}
}
