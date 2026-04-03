package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.reviewer.Reviewer;

class GuidanceProcessorAdditionalTest {

	@TempDir
	Path tempDir;

	@Test
	void getReviewerForExtension_whenRegisteredWithLeadingDotAndUpperCase_thenNormalizesLookup() throws Exception {
		GuidanceProcessor processor = new GuidanceProcessor(tempDir.toFile(), "Any:Model", new PropertiesConfigurator()) {
			@Override
			void loadReviewers() {
				// prevent ServiceLoader dependency
			}
		};
		Reviewer reviewer = new Reviewer() {
			@Override
			public String perform(File projectDir, File file) {
				return "ok";
			}

			@Override
			public String[] getSupportedFileExtensions() {
				return new String[] { "txt" };
			}
		};
		Field mapField = GuidanceProcessor.class.getDeclaredField("reviewerMap");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Reviewer> reviewerMap = (Map<String, Reviewer>) mapField.get(processor);
		reviewerMap.put("txt", reviewer);

		assertEquals(reviewer, processor.getReviewerForExtension(".TXT"));
	}

	@Test
	void deleteTempFiles_whenDirectoryExists_thenDeletesRecursively() throws IOException {
		Path tempRoot = tempDir.resolve(".machai").resolve(AIFileProcessor.GW_TEMP_DIR);
		Files.createDirectories(tempRoot);
		Files.write(tempRoot.resolve("input.log"), java.util.Collections.singletonList("x"), StandardCharsets.UTF_8);

		boolean deleted = GuidanceProcessor.deleteTempFiles(tempDir.toFile());

		assertTrue(deleted);
		assertTrue(Files.notExists(tempRoot));
	}

	@Test
	void parseFile_whenExtensionBlank_thenReturnsNull() throws Exception {
		GuidanceProcessor processor = new GuidanceProcessor(tempDir.toFile(), "Any:Model", new PropertiesConfigurator()) {
			@Override
			void loadReviewers() {
				// no-op
			}
		};
		File file = tempDir.resolve("README").toFile();
		Files.write(file.toPath(), java.util.Collections.singletonList("content"), StandardCharsets.UTF_8);

		assertNull(processor.parseFile(tempDir.toFile(), file));
	}
}
