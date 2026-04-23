package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class FileFunctionToolsDeepCoverageTest {

	private final FileFunctionTools tools = new FileFunctionTools();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@TempDir
	File tempDir;

	@Test
	void readFile_whenFileMissing_returnsNotFoundMessage() throws Exception {
		ObjectNode props = objectMapper.createObjectNode();
		props.put("file_path", "missing.txt");

		Object result = invoke("readFile", new Class[] { Object[].class }, new Object[] { new Object[] { props, tempDir } });

		assertEquals("File not found.", result);
	}

	@Test
	void writeFile_andReadFile_supportCustomCharset() throws Exception {
		ObjectNode writeProps = objectMapper.createObjectNode();
		writeProps.put("file_path", "nested/data.txt");
		writeProps.put("text", "Žluťoučký kůň");
		writeProps.put("charsetName", "UTF-8");

		Object writeResult = invoke("writeFile", new Class[] { Object[].class },
				new Object[] { new Object[] { writeProps, tempDir } });

		ObjectNode readProps = objectMapper.createObjectNode();
		readProps.put("file_path", "nested/data.txt");
		readProps.put("charsetName", "UTF-8");
		Object readResult = invoke("readFile", new Class[] { Object[].class },
				new Object[] { new Object[] { readProps, tempDir } });

		assertEquals("File written successfully: nested/data.txt", writeResult);
		assertEquals("Žluťoučký kůň", readResult);
	}

	@Test
	void writeFile_whenFileExists_updatesContent() throws Exception {
		File existing = new File(tempDir, "existing.txt");
		Files.write(existing.toPath(), "old".getBytes(StandardCharsets.UTF_8));
		ObjectNode props = objectMapper.createObjectNode();
		props.put("file_path", "existing.txt");
		props.put("text", "new-content");

		Object result = invoke("writeFile", new Class[] { Object[].class }, new Object[] { new Object[] { props, tempDir } });

		assertEquals("File updated successfully: existing.txt", result);
		assertEquals("new-content", new String(Files.readAllBytes(existing.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	void listFiles_whenDirectoryMissing_returnsNoFilesMessage() throws Exception {
		ObjectNode props = objectMapper.createObjectNode();
		props.put("dir_path", "does-not-exist");

		Object result = invoke("listFiles", new Class[] { Object[].class }, new Object[] { new Object[] { props, tempDir } });

		assertEquals("No files found in directory.", result);
	}

	@Test
	void getRecursiveFiles_whenDirectoryContainsOnlyExcludedFolder_returnsNoFilesMessage() throws Exception {
		File excluded = new File(tempDir, "target");
		assertTrue(excluded.mkdirs());
		Files.write(new File(excluded, "ignored.txt").toPath(), "x".getBytes(StandardCharsets.UTF_8));
		ObjectNode props = objectMapper.createObjectNode();
		props.put("dir_path", "target");

		Object result = invoke("getRecursiveFiles", new Class[] { Object[].class },
				new Object[] { new Object[] { props, tempDir } });

		assertEquals("No files found in directory.", result);
	}

	@Test
	void listFilesRecursively_returnsFilesFromNonExcludedDirectories() throws Exception {
		File includeDir = new File(tempDir, "src");
		File nestedDir = new File(includeDir, "nested");
		assertTrue(nestedDir.mkdirs());
		File includedOne = new File(includeDir, "file.txt");
		File includedTwo = new File(nestedDir, "second.txt");
		Files.write(includedOne.toPath(), "ok".getBytes(StandardCharsets.UTF_8));
		Files.write(includedTwo.toPath(), "ok2".getBytes(StandardCharsets.UTF_8));

		@SuppressWarnings("unchecked")
		List<File> result = (List<File>) invoke("listFilesRecursively", new Class[] { File.class }, includeDir);

		assertEquals(2, result.size());
		assertTrue(result.contains(includedOne));
		assertTrue(result.contains(includedTwo));
	}

	@Test
	void getRelativePath_whenFileOutsideBase_stillReturnsRelativePath() {
		File dir = new File(tempDir, "base");
		File file = new File(tempDir.getParentFile(), "other/place.txt");

		String result = FileFunctionTools.getRelativePath(dir, file, true);

		assertNotNull(result);
	}

	@Test
	void toStringFields_formatsOnlyRequestedFields() throws Exception {
		ObjectNode props = objectMapper.createObjectNode();
		props.put("file_path", "a.txt");
		props.put("text", "hello");
		props.put("number", 5);

		String result = (String) invoke("toStringFields",
				new Class[] { com.fasterxml.jackson.databind.JsonNode.class, String[].class }, props,
				new String[] { "file_path", "number" });

		assertTrue(result.contains("\"file_path\":\"a.txt\""));
		assertTrue(result.contains("\"number\":5"));
		assertTrue(!result.contains("text"));
	}

	@Test
	void writeNewFile_createsParentDirectories() throws Exception {
		File file = new File(tempDir, "deep/path/file.txt");

		Object result = invoke("writeNewFile", new Class[] { File.class, String.class, String.class, String.class }, file,
				"content", "UTF-8", "deep/path/file.txt");

		assertEquals("File written successfully: deep/path/file.txt", result);
		assertTrue(file.isFile());
	}

	@Test
	void readFile_whenTargetIsDirectory_returnsReadableContentOrMessage() throws Exception {
		File dir = new File(tempDir, "folder");
		assertTrue(dir.mkdirs());
		ObjectNode props = objectMapper.createObjectNode();
		props.put("file_path", "folder");

		Object result = invoke("readFile", new Class[] { Object[].class }, new Object[] { new Object[] { props, tempDir } });

		assertNotNull(result);
	}

	private Object invoke(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
		Method method = FileFunctionTools.class.getDeclaredMethod(methodName, parameterTypes);
		method.setAccessible(true);
		return method.invoke(tools, args);
	}
}
