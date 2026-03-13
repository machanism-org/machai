package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
		AIFileProcessor p = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		assertEquals("", p.parseLines(null));
	}

	@Test
	void parseLines_whenBlankLines_thenPreservesNewlinesAndTrimsWhitespaceLines() {
		AIFileProcessor p = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		String out = p.parseLines("a\n\n b \n");
		assertEquals("a\n\n" + "b\n", out);
	}

	@Test
	void tryToGetInstructionsFromReference_whenPlainText_thenReturnsOriginal() throws Exception {
		AIFileProcessor p = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		assertEquals("hello", p.tryToGetInstructionsFromReference("hello"));
	}

	@Test
	void tryToGetInstructionsFromReference_whenFileReference_thenLoadsAndParses() throws Exception {
		File f = tempDir.resolve("i.txt").toFile();
		Files.write(f.toPath(), java.util.Arrays.asList("x", "", "y"), StandardCharsets.UTF_8);

		AIFileProcessor p = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		String out = p.tryToGetInstructionsFromReference("file:" + f.getAbsolutePath());
		assertEquals("x\n\n" + "y\n", out);
	}

	@Test
	void readFromFilePath_whenRelative_thenResolvesAgainstRootDir() {
		AIFileProcessor p = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "Any:Model");
		assertThrows(IllegalArgumentException.class, () -> p.readFromFilePath("missing.txt"));
	}

	@Test
	void readFromHttpUrl_whenValid_thenReturnsContent() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/x", new FixedHandler("hello"));
		server.start();
		try {
			int port = server.getAddress().getPort();
			String url = "http://localhost:" + port + "/x";
			String body = AIFileProcessor.readFromHttpUrl(url);
			assertEquals("hello", body);
		} finally {
			server.stop(0);
		}
	}

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
