package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.reviewer.Reviewer;

class GuidanceProcessorTest {

	@TempDir
	Path tempDir;

	@Test
	void normalizeExtensionKey_whenNullOrBlank_thenNull() {
		assertNull(GuidanceProcessor.normalizeExtensionKey(null));
		assertNull(GuidanceProcessor.normalizeExtensionKey(""));
		assertNull(GuidanceProcessor.normalizeExtensionKey("  "));
	}

	@Test
	void normalizeExtensionKey_whenHasLeadingDot_thenLowercaseNoDot() {
		assertEquals("java", GuidanceProcessor.normalizeExtensionKey(".JAVA"));
		assertEquals("txt", GuidanceProcessor.normalizeExtensionKey("txt"));
	}

	@Test
	void match_whenNoPathMatcherAndNoDefaultPrompt_thenMatchesAnyFile() {
		PropertiesConfigurator config = new PropertiesConfigurator();
		GuidanceProcessor p = new GuidanceProcessor(tempDir.toFile(), "Any:Model", config);
		p.setDefaultPrompt(null);
		p.setPathMatcher(null);

		File projectDir = tempDir.toFile();
		assertTrue(p.match(projectDir, projectDir));
		assertTrue(p.match(new File(projectDir, "child.txt"), projectDir));
	}

	@Test
	void match_whenNoPathMatcherAndDefaultPromptPresent_thenOnlyMatchesProjectDir() {
		PropertiesConfigurator config = new PropertiesConfigurator();
		GuidanceProcessor p = new GuidanceProcessor(tempDir.toFile(), "Any:Model", config);
		p.setDefaultPrompt("default");
		p.setPathMatcher(null);

		File projectDir = tempDir.toFile();
		assertTrue(p.match(projectDir, projectDir));
		assertFalse(p.match(new File(projectDir, "child.txt"), projectDir));
	}

	@Test
	void getReviewerForExtension_whenNoReviewersLoaded_thenNull() {
		PropertiesConfigurator config = new PropertiesConfigurator();
		GuidanceProcessor p = new GuidanceProcessor(tempDir.toFile(), "Any:Model", config) {
			@Override
			void loadReviewers() {
				// force empty map
			}
		};

		assertNull(p.getReviewerForExtension("java"));
		assertNull(p.getReviewerForExtension(".java"));
		assertNull(p.getReviewerForExtension(null));
	}

	@Test
	void parseFile_whenNotFile_thenNull() throws Exception {
		PropertiesConfigurator config = new PropertiesConfigurator();
		GuidanceProcessor p = new GuidanceProcessor(tempDir.toFile(), "Any:Model", config) {
			@Override
			void loadReviewers() {
				// no reviewers
			}
		};

		File dir = tempDir.resolve("dir").toFile();
		assertTrue(dir.mkdirs());
		assertNull(p.parseFile(tempDir.toFile(), dir));
	}

	@Test
	void parseFile_whenNoReviewerForExtension_thenNull() throws Exception {
		PropertiesConfigurator config = new PropertiesConfigurator();
		GuidanceProcessor p = new GuidanceProcessor(tempDir.toFile(), "Any:Model", config) {
			@Override
			void loadReviewers() {
				// no reviewers
			}
		};

		File file = tempDir.resolve("a.unknown").toFile();
		Files.write(file.toPath(), java.util.Arrays.asList("x"), StandardCharsets.UTF_8);
		assertNull(p.parseFile(tempDir.toFile(), file));
	}

	@Test
	void parseFile_whenReviewerAvailable_thenDelegates() throws Exception {
		PropertiesConfigurator config = new PropertiesConfigurator();
		GuidanceProcessor p = new GuidanceProcessor(tempDir.toFile(), "Any:Model", config) {
			@Override
			void loadReviewers() {
				// don't use ServiceLoader
			}
		};

		java.lang.reflect.Field mapField = GuidanceProcessor.class.getDeclaredField("reviewerMap");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Reviewer> map = (java.util.Map<String, Reviewer>) mapField.get(p);
		map.put("txt", new Reviewer() {
			@Override
			public String[] getSupportedFileExtensions() {
				return new String[] { "txt" };
			}

			@Override
			public String perform(File projectDir, File file) throws IOException {
				return "GUIDANCE";
			}
		});

		File file = tempDir.resolve("a.txt").toFile();
		Files.write(file.toPath(), java.util.Arrays.asList("hello"), StandardCharsets.UTF_8);

		assertEquals("GUIDANCE", p.parseFile(tempDir.toFile(), file));
	}
}
