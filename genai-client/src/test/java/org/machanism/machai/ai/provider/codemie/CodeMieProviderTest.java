package org.machanism.machai.ai.provider.codemie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class CodeMieProviderTest {

	private HttpServer server;

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void getToken_passwordGrant_success_parsesAccessToken() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/token", new TokenHandler(200, "{\"access_token\":\"tok123\",\"expires_in\":3600}"));
		server.start();

		String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/token";

		String token = CodeMieProvider.getToken(url, "user@example.com", "p@ss word");

		assertEquals("tok123", token);
	}

	@Test
	void getToken_clientCredentials_success_parsesAccessToken() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/token", new TokenHandler(200, "{\"access_token\":\"svcTok\",\"token_type\":\"bearer\"}"));
		server.start();

		String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/token";

		String token = CodeMieProvider.getToken(url, "client-id", "client-secret");

		assertEquals("svcTok", token);
	}

	@Test
	void getToken_success_whenJsonSpansMultipleLines() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/token", new TokenHandler(200, "{\n  \"access_token\":\"abc\",\n  \"expires_in\":3600\n}"));
		server.start();

		String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/token";

		String token = CodeMieProvider.getToken(url, "user@example.com", "pw");

		assertEquals("abc", token);
	}

	@Test
	void getToken_success_returnsNullWhenTokenNotFound() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/token", new TokenHandler(200, "{\"no_access_token\":\"x\"}"));
		server.start();

		String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/token";

		String token = CodeMieProvider.getToken(url, "user@example.com", "pw");

		assertNull(token);
	}

	@Test
	void getToken_non200_throwsIOException_withStatusCode() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/token", new TokenHandler(401, "{\"error\":\"unauthorized\"}"));
		server.start();

		String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/token";

		IOException ex = assertThrows(IOException.class, () -> CodeMieProvider.getToken(url, "user@example.com", "pw"));
		assertEquals("Failed to obtain token: received HTTP response code 401", ex.getMessage());
	}

	@Test
	void getToken_invalidUrl_throwsIllegalArgumentExceptionFromUriCreate() {
		assertThrows(IllegalArgumentException.class, () -> CodeMieProvider.getToken("http://[invalid", "u", "p"));
	}

	private static byte[] readAllBytesCompat(InputStream in) throws IOException {
		byte[] buf = new byte[4096];
		int read;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((read = in.read(buf)) != -1) {
			baos.write(buf, 0, read);
		}
		return baos.toByteArray();
	}

	private static final class TokenHandler implements HttpHandler {
		private final int status;
		private final String body;

		private TokenHandler(int status, String body) {
			this.status = status;
			this.body = body;
		}

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			try (InputStream is = exchange.getRequestBody()) {
				readAllBytesCompat(is);
			}

			exchange.getResponseHeaders().add("Content-Type", "application/json");
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(status, bytes.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(bytes);
			}
		}
	}
}
