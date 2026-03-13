package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

class BindexProjectProcessorTest {

	private static final class TestProcessor extends BindexProjectProcessor {
		@Override
		public void processFolder(ProjectLayout projectLayout) {
			// no-op for tests
		}
	}

	@Test
	void getBindexFile_throwsOnNullProjectDir() {
		// Arrange
		TestProcessor processor = new TestProcessor();

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.getBindexFile(null));

		// Assert
		assertEquals("projectDir must not be null", ex.getMessage());
	}

	@Test
	void getBindexFile_returnsBindexJsonUnderProjectDir(@TempDir File tempDir) {
		// Arrange
		TestProcessor processor = new TestProcessor();

		// Act
		File file = processor.getBindexFile(tempDir);

		// Assert
		assertNotNull(file);
		assertEquals(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).getPath(), file.getPath());
	}

	@Test
	void getBindex_returnsNullWhenFileDoesNotExist(@TempDir File tempDir) {
		// Arrange
		TestProcessor processor = new TestProcessor();

		// Act
		Object bindex = processor.getBindex(tempDir);

		// Assert
		assertNull(bindex);
	}

	@Test
	void getBindex_throwsWhenJsonInvalid(@TempDir File tempDir) throws Exception {
		// Arrange
		TestProcessor processor = new TestProcessor();
		File bindexFile = processor.getBindexFile(tempDir);
		Files.write(bindexFile.toPath(), "{not-valid-json".getBytes(StandardCharsets.UTF_8));

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.getBindex(tempDir));

		// Assert
		assertNotNull(ex.getCause());
	}

	@Test
	void getBindex_readsMinimalValidBindex(@TempDir File tempDir) throws Exception {
		// Arrange
		TestProcessor processor = new TestProcessor();
		File bindexFile = processor.getBindexFile(tempDir);
		String json = "{\n" + "  \"id\": \"lib:1.0\",\n" + "  \"name\": \"lib\",\n" + "  \"version\": \"1.0\"\n" + "}";
		Files.write(bindexFile.toPath(), json.getBytes(StandardCharsets.UTF_8));

		// Act
		org.machanism.machai.schema.Bindex bindex = processor.getBindex(tempDir);

		// Assert
		assertNotNull(bindex);
		assertEquals("lib:1.0", bindex.getId());
		assertEquals("lib", bindex.getName());
		assertEquals("1.0", bindex.getVersion());
	}
}
