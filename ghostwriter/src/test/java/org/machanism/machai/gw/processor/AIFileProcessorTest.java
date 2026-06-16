package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

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
				AIFileProcessor.FILE_INCLUDED_MARKER + "file://" + file.getAbsolutePath());

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
	void parsePath_whenRootAndNestedPathProvided_buildsExpectedGlobPatterns() throws Exception {
		// Arrange
		AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");

		// Act
		processor.setDefaultPrompt(null);
		String recursivePattern = processor.parsePath(tempDir.toFile(), ".");
		File rootPath = processor.getPath();
		processor.setDefaultPrompt("default prompt");
		Path nested = Files.createDirectories(tempDir.resolve("src").resolve("main"));
		String directPattern = processor.parsePath(tempDir.toFile(), nested.toString());

		// Assert
		assertEquals("glob:.{,/**}", recursivePattern);
		assertEquals(tempDir.toFile(), rootPath);
		assertEquals(nested.toFile(), processor.getPath());
		assertEquals("glob:src/main", directPattern);
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
