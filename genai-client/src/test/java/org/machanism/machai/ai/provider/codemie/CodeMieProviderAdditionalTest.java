package org.machanism.machai.ai.provider.codemie;

import static org.junit.jupiter.api.Assertions.*;

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

/**
 * Additional tests for {@link CodeMieProvider#getToken(String, String, String)}.
 *
 * <p>
 * We use a local in-memory HTTP server (no external network dependency).
 * </p>
 */
class CodeMieProviderAdditionalTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void getToken_passwordGrant_sendsFormBodyWithUrlEncoding() throws Exception {
        // Arrange
        CapturingTokenHandler handler = new CapturingTokenHandler(200,
                "{\"access_token\":\"tok\",\"expires_in\":3600}");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/token", handler);
        server.start();

        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/token";

        // Act
        String token = CodeMieProvider.getToken(url, "user@example.com", "p@ss word");

        // Assert
        assertEquals("tok", token);
        assertEquals("POST", handler.method);
        assertEquals("application/x-www-form-urlencoded", handler.contentType);
        assertNotNull(handler.requestBody);
        assertTrue(handler.requestBody.startsWith("grant_type=password"));
        assertTrue(handler.requestBody.contains("client_id=codemie-sdk"));
        assertTrue(handler.requestBody.contains("username=user%40example.com"));
        assertTrue(handler.requestBody.contains("password=p%40ss+word"));
    }

    @Test
    void getToken_clientCredentials_sendsExpectedBody() throws Exception {
        // Arrange
        CapturingTokenHandler handler = new CapturingTokenHandler(200,
                "{\"access_token\":\"svc\",\"token_type\":\"bearer\"}");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/token", handler);
        server.start();

        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/token";

        // Act
        String token = CodeMieProvider.getToken(url, "client", "secret");

        // Assert
        assertEquals("svc", token);
        assertTrue(handler.requestBody.startsWith("grant_type=client_credentials"));
        assertTrue(handler.requestBody.contains("client_id=client"));
        assertTrue(handler.requestBody.contains("client_secret=secret"));
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

    private static final class CapturingTokenHandler implements HttpHandler {
        private final int status;
        private final String responseBody;

        private String method;
        private String contentType;
        private String requestBody;

        private CapturingTokenHandler(int status, String responseBody) {
            this.status = status;
            this.responseBody = responseBody;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            this.method = exchange.getRequestMethod();
            this.contentType = exchange.getRequestHeaders().getFirst("Content-Type");

            try (InputStream is = exchange.getRequestBody()) {
                this.requestBody = new String(readAllBytesCompat(is), StandardCharsets.UTF_8);
            }

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
