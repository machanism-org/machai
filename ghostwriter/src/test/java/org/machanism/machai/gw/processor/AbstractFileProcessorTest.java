package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

class AbstractFileProcessorTest {

	@TempDir
	File tempDir;

	private TestProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new TestProcessor(tempDir, new PropertiesConfigurator());
	}

	@Test
	void isPathPattern_shouldReturnTrueForGlobAndRegexIgnoringCase() {
		assertTrue(AbstractFileProcessor.isPathPattern("glob:**/*.java"));
		assertTrue(AbstractFileProcessor.isPathPattern("regex:.*"));
		assertTrue(AbstractFileProcessor.isPathPattern("GLOB:**/*.java"));
		assertTrue(AbstractFileProcessor.isPathPattern("ReGeX:.*"));
		assertFalse(AbstractFileProcessor.isPathPattern("src/main/java"));
		assertFalse(AbstractFileProcessor.isPathPattern(null));
	}

	@Test
	void getPatternPath_shouldReturnMatcherOnlyForPatterns() {
		assertNull(AbstractFileProcessor.getPatternPath(null));
		assertNull(AbstractFileProcessor.getPatternPath(""));
		assertNull(AbstractFileProcessor.getPatternPath("src/main/java"));

		assertNotNull(AbstractFileProcessor.getPatternPath("glob:**/*.md"));
	}

	@Test
	void pathDepth_shouldHandleBlankAndMixedSeparators() {
		assertEquals(0, AbstractFileProcessor.pathDepth(null));
		assertEquals(0, AbstractFileProcessor.pathDepth(""));
		assertEquals(0, AbstractFileProcessor.pathDepth("  "));
		assertEquals(1, AbstractFileProcessor.pathDepth("a"));
		assertEquals(2, AbstractFileProcessor.pathDepth("a/b"));
		assertEquals(3, AbstractFileProcessor.pathDepth("a\\b\\c"));
	}

	@Test
	void shouldExcludePath_shouldMatchExactPathAndFileName() {
		processor.setExcludes(new String[] { "foo" + File.separator + "bar.txt", "baz.txt" });

		assertTrue(processor.shouldExcludePath(new File("foo" + File.separator + "bar.txt").toPath()));
		assertTrue(processor.shouldExcludePath(new File("x" + File.separator + "y" + File.separator + "baz.txt").toPath()));
		assertFalse(processor.shouldExcludePath(new File("x" + File.separator + "y" + File.separator + "ok.md").toPath()));
		assertFalse(processor.shouldExcludePath(null));
	}

	@Test
	void match_shouldReturnFalseWhenFileNull() {
		assertFalse(processor.match(null, tempDir));
	}

	@Test
	void matchPath_whenNoMatcher_shouldMatchOnlyExactScanDir() throws IOException {
		File projectDir = new File(tempDir, "project");
		File scanFile = new File(projectDir, "a.txt");
		assertTrue(projectDir.mkdirs());
		assertTrue(scanFile.createNewFile());

		processor.setScanDir(scanFile);
		processor.setPathMatcher(null);

		assertTrue(processor.matchPath(projectDir, scanFile, "", "a.txt"));
		assertFalse(processor.matchPath(projectDir, new File(projectDir, "b.txt"), "", "b.txt"));
	}

	@Test
	void matchPath_shouldSupportPrimaryMatching() throws IOException {
		File projectDir = new File(tempDir, "project");
		File scanRoot = new File(projectDir, "docs");
		File nested = new File(scanRoot, "sub/file.md");
		assertTrue(nested.getParentFile().mkdirs());
		assertTrue(nested.createNewFile());

		processor.setScanDir(scanRoot);
		processor.setPathMatcher(FileSystems.getDefault().getPathMatcher("glob:**/*.md"));

		assertTrue(processor.matchPath(projectDir, nested, "", "docs/sub/file.md"));
	}

	private static final class TestProcessor extends AbstractFileProcessor {
		TestProcessor(File rootDir, Configurator configurator) {
			super(rootDir, configurator);
		}

		@Override
		public ProjectLayout getProjectLayout(File projectDir) {
			return new DefaultProjectLayout().projectDir(projectDir);
		}
	}
}
