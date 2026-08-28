package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

/** Tests CodeMie's HTTP token parsing and embedding delegation boundaries. */
class CodeMieProviderTest {

    @Test
    void obtainsPasswordGrantTokenAndEncodesCredentials() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = server(200, "{\"access_token\":\"token-123\",\"expires_in\":60}", requestBody);
        try {
            String token = CodeMieProvider.getToken(url(server), "person@example.com", "a secret&value");

            assertEquals("token-123", token);
            assertEquals("grant_type=password&client_id=codemie-sdk&username=person%40example.com&password=a+secret%26value", requestBody.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void obtainsClientCredentialsTokenForNonEmailUsername() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = server(200, "{\"access_token\":\"service-token\",}", requestBody);
        try {
            assertEquals("service-token", CodeMieProvider.getToken(url(server), "client id", "secret"));
            assertEquals("grant_type=client_credentials&client_id=client+id&client_secret=secret", requestBody.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsNonSuccessfulTokenResponse() throws Exception {
        HttpServer server = server(401, "denied", new AtomicReference<String>());
        try {
            IOException exception = assertThrows(IOException.class,
                    () -> CodeMieProvider.getToken(url(server), "client", "secret"));
            assertEquals("Failed to obtain token: received HTTP response code 401", exception.getMessage());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void embeddingRequiresAnEmbeddingCapableDelegate() {
        CodeMieProvider provider = new CodeMieProvider();

        assertThrows(NullPointerException.class, () -> provider.embedding("text", 3));
    }

    private static HttpServer server(int status, String response, AtomicReference<String> body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/token", exchange -> {
            java.io.ByteArrayOutputStream request = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int read;
            while ((read = exchange.getRequestBody().read(buffer)) != -1) {
                request.write(buffer, 0, read);
            }
            body.set(new String(request.toByteArray(), StandardCharsets.UTF_8));
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server;
    }

    private static String url(HttpServer server) {
        return "http://localhost:" + server.getAddress().getPort() + "/token";
    }
}
