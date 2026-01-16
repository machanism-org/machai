package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

class BIndexProjectProcessorTest {

	@TempDir
	Path tempDir;

	static class TestProcessor extends BindexProjectProcessor {

		@Override
		public void processFolder(ProjectLayout processor) {
			// TODO Auto-generated method stub
			
		}
		// no-op
	}

	@Test
	void getBindexFile_returnsFileInProjectDirWithExpectedName() {
		// Arrange
		TestProcessor processor = new TestProcessor();
		File projectDir = tempDir.toFile();

		// Act
		File file = processor.getBindexFile(projectDir);

		// Assert
		assertEquals(new File(projectDir, BindexProjectProcessor.BINDEX_FILE_NAME).getAbsolutePath(),
				file.getAbsolutePath());
	}

	@Test
	void getBindex_returnsNullWhenFileDoesNotExist() {
		// Arrange
		TestProcessor processor = new TestProcessor();
		File projectDir = tempDir.toFile();

		// Act
		Bindex bindex = processor.getBindex(projectDir);

		// Assert
		assertNull(bindex);
	}

	@Test
	void getBindex_parsesValidJson() throws Exception {
		// Arrange
		TestProcessor processor = new TestProcessor();
		Path bindexPath = tempDir.resolve(BindexProjectProcessor.BINDEX_FILE_NAME);
		Files.write(bindexPath,
				("{\"id\":\"lib:1\",\"name\":\"Lib\",\"version\":\"1\","
						+ "\"description\":\"d\",\"dependencies\":[]}").getBytes(StandardCharsets.UTF_8));

		// Act
		Bindex bindex = processor.getBindex(tempDir.toFile());

		// Assert
		assertNotNull(bindex);
		assertEquals("lib:1", bindex.getId());
	}

	@Test
	void getBindex_throwsIllegalArgumentExceptionOnInvalidJson() throws Exception {
		// Arrange
		TestProcessor processor = new TestProcessor();
		Path bindexPath = tempDir.resolve(BindexProjectProcessor.BINDEX_FILE_NAME);
		Files.write(bindexPath, "{invalid".getBytes(StandardCharsets.UTF_8));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> processor.getBindex(tempDir.toFile()));
	}
}
