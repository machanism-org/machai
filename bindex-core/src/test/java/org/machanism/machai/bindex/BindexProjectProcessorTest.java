package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BindexProjectProcessorTest {

	@TempDir
	File tempDir;

	static class TestProcessor extends BindexProjectProcessor {
		@Override
		public void processFolder(ProjectLayout processor) {
			// not used
		}
	}

	@Test
	void getBindexFile_returnsBindexJsonPathUnderProjectDir() {
		// Arrange
		TestProcessor processor = new TestProcessor();

		// Act
		File bindexFile = processor.getBindexFile(tempDir);

		// Assert
		assertEquals(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).getAbsolutePath(),
				bindexFile.getAbsolutePath());
	}

	@Test
	void getBindex_whenFileDoesNotExist_returnsNull() {
		// Arrange
		TestProcessor processor = new TestProcessor();

		// Act
		Bindex bindex = processor.getBindex(tempDir);

		// Assert
		assertNull(bindex);
	}

	@Test
	void getBindex_whenValidJsonExists_readsBindex() throws IOException {
		// Arrange
		TestProcessor processor = new TestProcessor();
		File bindexFile = processor.getBindexFile(tempDir);

		String json = "{\"id\":\"g:a:1\",\"name\":\"a\",\"version\":\"1\"}";
		Files.write(bindexFile.toPath(), json.getBytes(StandardCharsets.UTF_8));

		// Act
		Bindex bindex = processor.getBindex(tempDir);

		// Assert
		assertNotNull(bindex);
		assertEquals("g:a:1", bindex.getId());
		assertEquals("a", bindex.getName());
		assertEquals("1", bindex.getVersion());
	}

	@Test
	void getBindex_whenInvalidJson_throwsIllegalArgumentException() throws IOException {
		// Arrange
		TestProcessor processor = new TestProcessor();
		File bindexFile = processor.getBindexFile(tempDir);
		Files.writeString(bindexFile.toPath(), "{not-json", StandardCharsets.UTF_8);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> processor.getBindex(tempDir));
	}
}
