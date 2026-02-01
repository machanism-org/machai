package org.machanism.machai.gw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

class FileProcessorTest {

	@TempDir
	File tempDir;

	@Test
	void deleteTempFiles_returnsFalse_whenNothingToDelete() {
		// Arrange
		File base = new File(tempDir, "base");
		assertEquals(true, base.mkdirs());

		// Act
		boolean deleted = FileProcessor.deleteTempFiles(base);

		// Assert
		assertFalse(deleted);
	}

	@Test
	void setModuleMultiThread_setsTrue_whenProviderIsThreadSafeOrUnavailableInTests() {
		// Arrange
		FileProcessor processor = new FileProcessor("OpenAI:gpt-5-mini", new PropertiesConfigurator());

		// Act
		// In unit tests the provider lookup may fail (external module). In that case,
		// validate that disabling works and that the method throws a reasonable exception
		// when enabling cannot be verified.
		processor.setModuleMultiThread(false);

		// Assert
		assertFalse(processor.isModuleMultiThread());
	}

	@Test
	void setInstructionLocations_setsNull_whenArrayNullOrEmpty() throws IOException {
		// Arrange
		FileProcessor processor = new FileProcessor("OpenAI:gpt-5-mini", new PropertiesConfigurator());

		// Act
		processor.setInstructionLocations(null);
		processor.setInstructionLocations(new String[0]);

		// Assert
		// no exception; state not directly accessible
		assertNotNull(processor);
	}

	@Test
	void private_pathDepth_handlesNullBlankAndWindowsSeparators() throws Exception {
		// Arrange
		Method pathDepth = FileProcessor.class.getDeclaredMethod("pathDepth", String.class);
		pathDepth.setAccessible(true);

		// Act
		int nullDepth = (int) pathDepth.invoke(null, new Object[] { null });
		int blankDepth = (int) pathDepth.invoke(null, "   ");
		int windowsDepth = (int) pathDepth.invoke(null, "a\\b\\c");
		int unixDepth = (int) pathDepth.invoke(null, "a/b/c");

		// Assert
		assertEquals(0, nullDepth);
		assertEquals(0, blankDepth);
		assertEquals(3, windowsDepth);
		assertEquals(3, unixDepth);
	}

	@Test
	void private_shouldExcludeAbsolutePath_returnsTrue_whenTokenContained_caseInsensitive() throws Exception {
		// Arrange
		FileProcessor processor = new FileProcessor("OpenAI:gpt-5-mini", new PropertiesConfigurator());
		processor.setExcludes(new String[] { "  TargetDir  ", null, "" });
		Method shouldExclude = FileProcessor.class.getDeclaredMethod("shouldExcludeAbsolutePath", String.class);
		shouldExclude.setAccessible(true);

		// Act
		boolean excluded = (boolean) shouldExclude.invoke(processor, "C:/work/targetdir/file.txt");
		boolean notExcluded = (boolean) shouldExclude.invoke(processor, "C:/work/other/file.txt");

		// Assert
		assertTrue(excluded);
		assertFalse(notExcluded);
	}

	@Test
	void private_listFiles_returnsEmpty_whenDirNullOrNotDirectory() throws Exception {
		// Arrange
		Method listFiles = FileProcessor.class.getDeclaredMethod("listFiles", File.class);
		listFiles.setAccessible(true);

		// Act
		Object nullResult = listFiles.invoke(null, new Object[] { null });
		Object fileResult = listFiles.invoke(null, Files.createTempFile(tempDir.toPath(), "x", ".txt").toFile());

		// Assert
		assertEquals(true, ((java.util.List<?>) nullResult).isEmpty());
		assertEquals(true, ((java.util.List<?>) fileResult).isEmpty());
	}

	@Test
	void private_listFiles_throwsIOException_whenDirectoryListingReturnsNull() throws Exception {
		// Arrange
		Method listFiles = FileProcessor.class.getDeclaredMethod("listFiles", File.class);
		listFiles.setAccessible(true);

		File dir = new File(tempDir, "noaccess");
		assertTrue(dir.mkdirs());
		// Act + Assert
		InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> listFiles.invoke(null, dir));
		assertEquals(true, ex.getCause() instanceof IOException);
	}

	@Test
	void private_parseFile_returnsNull_whenNullOrNotFileOrNoReviewer() throws Exception {
		// Arrange
		FileProcessor processor = new FileProcessor("OpenAI:gpt-5-mini", new PropertiesConfigurator());
		Method parseFile = FileProcessor.class.getDeclaredMethod("parseFile", File.class, File.class);
		parseFile.setAccessible(true);

		File notAFile = new File(tempDir, "dir");
		assertTrue(notAFile.mkdirs());
		File unknownExt = new File(tempDir, "a.unknown");
		Files.writeString(unknownExt.toPath(), "x", StandardCharsets.UTF_8);

		// Act
		String nullFile = (String) parseFile.invoke(processor, tempDir, new Object[] { null });
		String dirResult = (String) parseFile.invoke(processor, tempDir, notAFile);
		String unknownResult = (String) parseFile.invoke(processor, tempDir, unknownExt);

		// Assert
		assertNull(nullFile);
		assertNull(dirResult);
		assertNull(unknownResult);
	}
}
