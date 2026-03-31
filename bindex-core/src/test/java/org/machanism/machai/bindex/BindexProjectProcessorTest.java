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

import com.fasterxml.jackson.databind.ObjectMapper;

class BindexProjectProcessorTest {

	private static final String BINDEX_FILE_NAME = "bindex.json";

	private static File getBindexFile(File projectDir) {
		if (projectDir == null) {
			throw new IllegalArgumentException("projectDir must not be null");
		}
		return new File(projectDir, BINDEX_FILE_NAME);
	}

	private static org.machanism.machai.schema.Bindex getBindex(File projectDir) {
		File file = getBindexFile(projectDir);
		if (!file.exists()) {
			return null;
		}
		try {
			return new ObjectMapper().readValue(file, org.machanism.machai.schema.Bindex.class);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Test
	void getBindexFile_throwsOnNullProjectDir() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> getBindexFile(null));
		assertEquals("projectDir must not be null", ex.getMessage());
	}

	@Test
	void getBindexFile_returnsBindexJsonUnderProjectDir(@TempDir File tempDir) {
		File file = getBindexFile(tempDir);
		assertNotNull(file);
		assertEquals(new File(tempDir, BINDEX_FILE_NAME).getPath(), file.getPath());
	}

	@Test
	void getBindex_returnsNullWhenFileDoesNotExist(@TempDir File tempDir) {
		Object bindex = getBindex(tempDir);
		assertNull(bindex);
	}

	@Test
	void getBindex_throwsWhenJsonInvalid(@TempDir File tempDir) throws Exception {
		File bindexFile = getBindexFile(tempDir);
		Files.write(bindexFile.toPath(), "{not-valid-json".getBytes(StandardCharsets.UTF_8));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> getBindex(tempDir));
		assertNotNull(ex.getCause());
	}

	@Test
	void getBindex_readsMinimalValidBindex(@TempDir File tempDir) throws Exception {
		File bindexFile = getBindexFile(tempDir);
		String json = "{\n" + "  \"id\": \"lib:1.0\",\n" + "  \"name\": \"lib\",\n" + "  \"version\": \"1.0\"\n" + "}";
		Files.write(bindexFile.toPath(), json.getBytes(StandardCharsets.UTF_8));

		org.machanism.machai.schema.Bindex bindex = getBindex(tempDir);

		assertNotNull(bindex);
		assertEquals("lib:1.0", bindex.getId());
		assertEquals("lib", bindex.getName());
		assertEquals("1.0", bindex.getVersion());
	}
}
