package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class AbstractFileProcessorTest {

	@TempDir
	Path tempDir;

	private TestProcessor processor;
	private PropertiesConfigurator configurator;

	@BeforeEach
	void setUp() {
		configurator = new PropertiesConfigurator();
		processor = new TestProcessor(tempDir.toFile(), configurator);
	}

	@Test
	void isPathPattern_whenGlobOrRegex_thenTrue() {
		assertTrue(AbstractFileProcessor.isPathPattern("glob:**/*.java"));
		assertTrue(AbstractFileProcessor.isPathPattern("regex:.*"));
	}

	@Test
	void isPathPattern_whenOther_thenFalse() {
		assertFalse(AbstractFileProcessor.isPathPattern("src/main/java"));
		assertFalse(AbstractFileProcessor.isPathPattern(""));
		assertFalse(AbstractFileProcessor.isPathPattern("  glob:**/*.java"));
	}

	@Test
	void getPatternPath_whenBlankOrNotPattern_thenNull() {
		assertNull(AbstractFileProcessor.getPatternPath(null));
		assertNull(AbstractFileProcessor.getPatternPath(""));
		assertNull(AbstractFileProcessor.getPatternPath("src/main/java"));
	}

	@Test
	void getPatternPath_whenPattern_thenReturnsMatcher() {
		PathMatcher matcher = AbstractFileProcessor.getPatternPath("glob:**/*.java");
		assertNotNull(matcher);
		assertTrue(matcher.matches(new File("a/b/C.java").toPath()));
		assertFalse(matcher.matches(new File("a/b/C.txt").toPath()));
	}

	@Test
	void pathDepth_whenBlank_thenZero() {
		assertEquals(0, AbstractFileProcessor.pathDepth(null));
		assertEquals(0, AbstractFileProcessor.pathDepth(""));
		assertEquals(0, AbstractFileProcessor.pathDepth("   "));
	}

	@Test
	void pathDepth_whenWindowsSeparators_thenNormalizes() {
		assertEquals(3, AbstractFileProcessor.pathDepth("a\\b\\c"));
		assertEquals(1, AbstractFileProcessor.pathDepth("one"));
	}

	@Test
	void shutdownExecutor_whenNull_thenNoop() {
		assertDoesNotThrow(() -> processor.shutdownExecutor(null));
	}

	@Test
	void setModuleThreadTimeoutMinutes_whenNonPositive_thenThrows() {
		assertThrows(IllegalArgumentException.class, () -> processor.setModuleThreadTimeoutMinutes(0));
		assertThrows(IllegalArgumentException.class, () -> processor.setModuleThreadTimeoutMinutes(-1));
	}

	@Test
	void findFiles_whenNullOrNotDirectory_thenEmpty() throws Exception {
		assertEquals(Collections.emptyList(), processor.findFiles(null));
		File notDir = tempDir.resolve("file.txt").toFile();
		Files.write(notDir.toPath(), Arrays.asList("x"), StandardCharsets.UTF_8);
		assertEquals(Collections.emptyList(), processor.findFiles(notDir));
	}

	@Test
	void findFiles_whenDirectoryListFilesReturnsNull_thenThrowsIOException() {
		File dir = tempDir.resolve("dir").toFile();
		assertTrue(dir.mkdirs());

		TestProcessor p = new TestProcessor(tempDir.toFile(), configurator) {
			@Override
			List<File> findFiles(File projectDir) throws IOException {
				File fake = new File(projectDir, "fake") {
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
				return super.findFiles(fake);
			}
		};

		IOException ex = assertThrows(IOException.class, () -> p.findFiles(dir));
		assertTrue(ex.getMessage().contains("Unable to list files"));
	}

	@Test
	void shouldExcludePath_whenMatcherMatches_thenTrue() {
		processor.setExcludes(new String[] { "glob:**/*.tmp" });

		assertTrue(processor.shouldExcludePath(new File("a/b/c.tmp").toPath()));
		assertFalse(processor.shouldExcludePath(new File("a/b/c.txt").toPath()));
	}

	@Test
	void shouldExcludePath_whenExactPathOrFileNameMatches_thenTrue() {
		processor.setExcludes(new String[] { "a\\b\\c.txt", "ignored.txt" });

		assertTrue(processor.shouldExcludePath(new File("a\\b\\c.txt").toPath()));
		assertTrue(processor.shouldExcludePath(new File("x/y/ignored.txt").toPath()));
		assertFalse(processor.shouldExcludePath(new File("x/y/other.txt").toPath()));
	}

	@Test
	void match_whenNullFile_thenFalse() {
		assertFalse(processor.match(null, tempDir.toFile()));
	}

	@Test
	void matchPath_whenNoMatcher_thenOnlyMatchesExactScanDir() throws Exception {
		File projectDir = tempDir.resolve("project").toFile();
		assertTrue(projectDir.mkdirs());

		File file = new File(projectDir, "a.txt");
		Files.write(file.toPath(), Arrays.asList("x"), StandardCharsets.UTF_8);

		processor.setPathMatcher(null);
		processor.setScanDir(file);

		assertTrue(processor.matchPath(projectDir, file, "", "a.txt"));
		assertFalse(processor.matchPath(projectDir, new File(projectDir, "b.txt"), "", "b.txt"));
	}

	@Test
	void matchPath_whenMatcherAndDirectMatch_thenTrue() throws Exception {
		File projectDir = tempDir.resolve("project").toFile();
		assertTrue(projectDir.mkdirs());
		File file = new File(projectDir, "src/Main.java");
		assertTrue(file.getParentFile().mkdirs());
		Files.write(file.toPath(), Arrays.asList("class Main {}"), StandardCharsets.UTF_8);

		processor.setScanDir(null);
		processor.setPathMatcher(FileSystems.getDefault().getPathMatcher("glob:**/*.java"));

		assertTrue(processor.matchPath(projectDir, file, "", "src" + File.separator + "Main.java"));
	}

	@Test
	void matchPath_whenMatcherNoMatchAndScanDirProvidesRelatedToRoot_thenMatches() throws Exception {
		File projectDir = tempDir.resolve("project").toFile();
		assertTrue(projectDir.mkdirs());

		File scanDir = new File(projectDir, "module");
		assertTrue(scanDir.mkdirs());

		File file = new File(scanDir, "src/Main.java");
		assertTrue(file.getParentFile().mkdirs());
		Files.write(file.toPath(), Arrays.asList("class Main {}"), StandardCharsets.UTF_8);

		processor.setScanDir(scanDir);
		processor.setPathMatcher(FileSystems.getDefault().getPathMatcher("glob:module/src/*.java"));

		assertTrue(processor.matchPath(projectDir, file, "", "other" + File.separator + "Main.java"));
	}

	@Test
	void matchPath_whenMatcherNoMatchAndRelativeToScanDirNull_thenFalse() throws Exception {
		File projectDir = tempDir.resolve("project").toFile();
		assertTrue(projectDir.mkdirs());

		File scanDir = new File(projectDir, "scan");
		assertTrue(scanDir.mkdirs());

		File file = new File(projectDir, "outside.txt");
		Files.write(file.toPath(), Arrays.asList("x"), StandardCharsets.UTF_8);

		processor.setScanDir(scanDir);
		processor.setPathMatcher(FileSystems.getDefault().getPathMatcher("glob:scan/*.java"));

		assertFalse(processor.matchPath(projectDir, file, "", "outside.txt"));
	}

	static class TestProcessor extends AbstractFileProcessor {
		TestProcessor(File rootDir, PropertiesConfigurator configurator) {
			super(rootDir, configurator);
		}
	}
}
