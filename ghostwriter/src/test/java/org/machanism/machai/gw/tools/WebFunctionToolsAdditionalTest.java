package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;

class WebFunctionToolsAdditionalTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void getWebContentShouldReadFileUriAndApplySelectorAndTextOnly() throws Exception {
        WebFunctionTools tools = new WebFunctionTools();
        Path htmlFile = tempDir.resolve("page.html");
        Files.write(htmlFile, "<html><body><div class='x'>hello</div><p>other</p></body></html>".getBytes(StandardCharsets.UTF_8));

        ObjectNode props = MAPPER.createObjectNode();
        props.put("url", htmlFile.toUri().toString());
        props.put("selector", ".x");
        props.put("textOnly", true);

        String result = tools.getWebContent(props, tempDir.toFile());

        assertTrue(result.contains("hello"));
    }

    @Test
    void getConnectionAndGetWebPageShouldSupportBasicAuthAndErrorStream() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ok", exchange -> {
            byte[] response = "<html><body>ok</body></html>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/bad", exchange -> {
            byte[] response = "failure".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            WebFunctionTools tools = new WebFunctionTools();
            int port = server.getAddress().getPort();

            HttpURLConnection connection = tools.getConnection(new URI("http://user:pass@localhost:" + port + "/ok"), null);
            String okResult = tools.getWebPage(connection, 5000, "UTF-8");

            HttpURLConnection errorConnection = tools.getConnection(new URI("http://localhost:" + port + "/bad"), null);
            String errorResult = tools.getWebPage(errorConnection, 5000, "UTF-8");

            assertTrue(connection.getURL().toString().contains("/ok"));
            assertTrue(okResult.contains("HTTP 200"));
            assertTrue(okResult.contains("ok"));
            assertTrue(errorResult.contains("HTTP 500"));
            assertTrue(errorResult.contains("failure"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void callRestApiShouldSendBodyAndHeaders() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api", exchange -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int len;
            while ((len = exchange.getRequestBody().read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            String body = new String(out.toByteArray(), StandardCharsets.UTF_8);
            String header = exchange.getRequestHeaders().getFirst("X-Test");
            byte[] response = (exchange.getRequestMethod() + ":" + header + ":" + body).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            WebFunctionTools tools = new WebFunctionTools();
            ObjectNode props = MAPPER.createObjectNode();
            props.put("url", "http://localhost:" + server.getAddress().getPort() + "/api");
            props.put("method", "POST");
            props.put("headers", "X-Test=header-value");
            props.put("body", "payload");

            String result = tools.callRestApi(props, new File("."));

            assertTrue(result.contains("HTTP 200"));
            assertTrue(result.contains("POST:header-value:payload"));
        } finally {
            server.stop(0);
        }
    }
}
