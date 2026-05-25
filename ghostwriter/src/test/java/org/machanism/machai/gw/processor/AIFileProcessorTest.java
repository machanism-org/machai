package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class AIFileProcessorTest {

	@TempDir
	Path tempDir;

	@Test
	void parseLines_whenNull_thenEmpty() {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act + Assert
		assertEquals("", processor.parseLines(null));
	}

	@Test
	void parseLines_whenBlankLines_thenPreservesNewlinesAndTrimsWhitespaceLines() {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act
		String out = processor.parseLines("a\n\n b \n");

		// Assert
		assertEquals("a\n\n" + "b\n", out);
	}

	@Test
	void tryToGetInstructionsFromReference_whenPlainText_thenReturnsOriginal() throws Exception {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act + Assert
		assertEquals("hello", processor.tryToGetInstructionsFromReference("hello"));
	}

	@Test
	void tryToGetInstructionsFromReference_whenFileReference_thenLoadsAndParses() throws Exception {
		// Arrange
		File file = tempDir.resolve("i.txt").toFile();
		Files.write(file.toPath(), Arrays.asList("x", "", "y"), StandardCharsets.UTF_8);
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act
		String out = processor.tryToGetInstructionsFromReference(
				AIFileProcessor.FILE_INCLUDED_MARKER + "file:" + file.getAbsolutePath());

		// Assert
		assertEquals("x\n\n" + "y\n", out);
	}

	@Test
	void readFromFilePath_whenRelative_thenResolvesAgainstRootDir() {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> processor.readFromFilePath("missing.txt"));
	}

	@Test
	void scanDocuments_whenArgumentsInvalid_thenThrowsIllegalArgumentException() {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> processor.scanDocuments(null, "."));
		assertThrows(IllegalArgumentException.class, () -> processor.scanDocuments(tempDir.toFile(), "   "));
	}

	@Test
	void parseScanDir_whenRootAndNestedPathsProvided_buildsExpectedGlobPatterns() throws Exception {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act
		processor.setDefaultPrompt(null);
		String recursivePattern = processor.parseScanDir(tempDir.toFile(), ".");
		File rootScanDir = processor.getScanDir();
		processor.setDefaultPrompt("default prompt");
		Path nested = Files.createDirectories(tempDir.resolve("src").resolve("main"));
		String directPattern = processor.parseScanDir(tempDir.toFile(), nested.toString());

		// Assert
		assertEquals("glob:.{,/**}", recursivePattern);
		assertEquals(tempDir.toFile(), rootScanDir);
		assertEquals(nested.toFile(), processor.getScanDir());
		assertEquals("glob:src/main", directPattern);
	}

	@Test
	void gettersAndProjectStructureDescription_whenConfigured_returnExpectedValues() throws Exception {
		// Arrange
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), configurator, "Provider:Model");
		DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());
		Files.createDirectories(tempDir.resolve("existing"));

		// Act
		processor.setInstructions("instructions");
		processor.setDefaultPrompt("default");
		processor.setModel("Other:Model");
		processor.setInteractive(true);
		processor.setLogInputs(true);
		String dirInfoDefined = processor.getDirInfoLine(Arrays.asList("existing", "missing"), tempDir.toFile());
		String dirInfoEmpty = processor.getDirInfoLine(Collections.emptyList(), tempDir.toFile());
		String dirInfoNull = processor.getDirInfoLine(null, tempDir.toFile());
		String description = processor.getProjectStructureDescription(layout, tempDir.toFile());

		// Assert
		assertEquals("instructions\n", processor.getInstructions());
		assertEquals("default", processor.getDefaultPrompt());
		assertEquals("Other:Model", processor.getModel());
		assertTrue(processor.isInteractive());
		assertTrue(processor.isLogInputs());
		assertEquals("<EMPTY>", getEmptyValue());
		assertEquals("<NOT_DEFINED_VALUE>", AIFileProcessor.NOT_DEFINED_VALUE);
		assertEquals("docs-inputs", AIFileProcessor.GW_TEMP_DIR);
		assertEquals(">>>", AIFileProcessor.FILE_INCLUDED_MARKER);
		assertEquals(">", AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND);
		assertEquals(".", AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND);
		assertEquals("`existing`", dirInfoDefined);
		assertEquals("<EMPTY>", dirInfoEmpty);
		assertEquals("<NOT_DEFINED_VALUE>", dirInfoNull);
		assertNotNull(description);
		assertTrue(description.contains(tempDir.toFile().getName()));
	}

	@SuppressWarnings("restriction")
	@Test
	void readFromHttpUrl_whenValid_thenReturnsContent() throws Exception {
		// Arrange
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/x", new FixedHandler("hello"));
		server.start();

		// Act + Assert
		try {
			int port = server.getAddress().getPort();
			String url = "http://localhost:" + port + "/x";
			String body = AIFileProcessor.readFromHttpUrl(url);
			assertEquals("hello", body);
		} finally {
			server.stop(0);
		}
	}

	private static Object getEmptyValue() throws Exception {
		java.lang.reflect.Field field = AIFileProcessor.class.getDeclaredField("EMPTY_VALUE");
		field.setAccessible(true);
		return field.get(null);
	}

	@SuppressWarnings("restriction")
	static class FixedHandler implements HttpHandler {
		private final String response;

		FixedHandler(String response) {
			this.response = response;
		}

		@Override
		public void handle(HttpExchange exchange) throws java.io.IOException {
			byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, bytes.length);
			try (java.io.OutputStream os = exchange.getResponseBody()) {
				os.write(bytes);
			}
		}
	}
}
