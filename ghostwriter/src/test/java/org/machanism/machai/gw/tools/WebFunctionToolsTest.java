package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class WebFunctionToolsTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void getWebContentAppliesSelectorAndTextRendering() throws Exception {
        // Arrange
        startServer(exchange -> respond(exchange, 200, "<html><body><p class='wanted'>Hello <b>world</b></p><p>ignored</p></body></html>"));
        WebFunctionTools tools = new WebFunctionTools();

        // Act
        String html = tools.getWebContent(url("/page"), null, 1000, "UTF-8", false, ".wanted", null, null);
        String text = tools.getWebContent(url("/page"), null, 1000, "UTF-8", true, ".wanted", null, null);

        // Assert
        assertEquals("<p class=\"wanted\">Hello <b>world</b></p>", html);
        assertTrue(text.contains("Hello world"));
        assertTrue(!text.contains("ignored"));
    }

    @Test
    void callRestApiSendsBodyHeadersAndReadsErrorResponse() throws Exception {
        // Arrange
        startServer(exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertEquals("present", exchange.getRequestHeaders().getFirst("X-Test"));
            assertEquals("payload", new String(org.apache.commons.io.IOUtils.toByteArray(exchange.getRequestBody()), StandardCharsets.UTF_8));
            respond(exchange, 201, "created");
        });
        WebFunctionTools tools = new WebFunctionTools();
        Map<String, String> headers = Collections.singletonMap("X-Test", "present");

        // Act
        String result = tools.callRestApi(url("/post"), "POST", headers, "payload", 1000, "UTF-8", null, null);

        // Assert
        assertTrue(result.contains("HTTP 201"));
        assertTrue(result.contains("created"));
    }

    @Test
    void getWebPageReadsErrorStreamAndConfiguresTimeout() throws Exception {
        // Arrange
        WebFunctionTools tools = new WebFunctionTools();
        FakeConnection connection = new FakeConnection(404, "Missing", "error-body");

        // Act
        String result = tools.getWebPage(connection, 123, "UTF-8");

        // Assert
        assertTrue(result.contains("HTTP 404 Missing"));
        assertTrue(result.contains("error-body"));
        assertEquals(123, connection.getConnectTimeout());
        assertEquals(123, connection.getReadTimeout());
    }

    @Test
    void fillHeaderAndSelectorHandleNullAndBlankInputs() throws Exception {
        // Arrange
        WebFunctionTools tools = new WebFunctionTools();
        FakeConnection connection = new FakeConnection(200, "OK", "");

        // Act
        tools.fillHeader(null, connection, null);
        tools.fillHeader(new LinkedHashMap<String, String>() {{ put("X-Header", "value"); }}, connection, null);

        // Assert
        assertEquals("value", connection.properties.get("X-Header"));
        assertEquals("plain", tools.applySelectorIfPresent(" ", "plain"));
    }

    private void startServer(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/page", handler);
        server.createContext("/post", handler);
        server.start();
    }

    private String url(String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static final class FakeConnection extends HttpURLConnection {
        private final int code;
        private final String message;
        private final byte[] body;
        private final Map<String, String> properties = new LinkedHashMap<>();

        FakeConnection(int code, String message, String body) throws Exception {
            super(new URI("http://localhost/fake").toURL());
            this.code = code;
            this.message = message;
            this.body = body.getBytes(StandardCharsets.UTF_8);
        }
        @Override public void disconnect() { }
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() { }
        @Override public int getResponseCode() { return code; }
        @Override public String getResponseMessage() { return message; }
        @Override public java.io.InputStream getInputStream() { return new ByteArrayInputStream(body); }
        @Override public java.io.InputStream getErrorStream() { return new ByteArrayInputStream(body); }
        @Override public void setRequestProperty(String key, String value) { properties.put(key, value); }
    }
}
