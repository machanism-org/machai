package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class WebFunctionToolsTest {

	@TempDir
	File tempDir;

	private static final class MapConfigurator implements Configurator {
		private final Map<String, String> map;

		private MapConfigurator(Map<String, String> map) {
			this.map = map;
		}

		@Override
		public String get(String key) {
			return map.get(key);
		}

		@Override
		public String get(String key, String defaultValue) {
			String val = map.get(key);
			return val == null ? defaultValue : val;
		}

		@Override
		public int getInt(String key) {
			return Integer.parseInt(get(key));
		}

		@Override
		public Integer getInt(String key, Integer defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Integer.valueOf(val);
		}

		@Override
		public boolean getBoolean(String key) {
			return Boolean.parseBoolean(get(key));
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Boolean.valueOf(val);
		}

		@Override
		public long getLong(String key) {
			return Long.parseLong(get(key));
		}

		@Override
		public Long getLong(String key, Long defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Long.valueOf(val);
		}

		@Override
		public File getFile(String key) {
			String val = get(key);
			return val == null ? null : new File(val);
		}

		@Override
		public File getFile(String key, File defaultValue) {
			File val = getFile(key);
			return val == null ? defaultValue : val;
		}

		@Override
		public double getDouble(String key) {
			return Double.parseDouble(get(key));
		}

		@Override
		public Double getDouble(String key, Double defaultValue) {
			String val = get(key);
			return val == null ? defaultValue : Double.valueOf(val);
		}

		@Override
		public String getName() {
			return "map";
		}

		@Override
		public void set(String key, String value) {
			map.put(key, value);
		}
	}

	private static final class FakeHttpURLConnection extends HttpURLConnection {
		private final Map<String, String> requestProps = new HashMap<>();
		private final ByteArrayOutputStream requestBody = new ByteArrayOutputStream();

		private int code = 200;
		private String message = "OK";
		private ByteArrayInputStream input = new ByteArrayInputStream("body".getBytes(StandardCharsets.UTF_8));
		private ByteArrayInputStream error = new ByteArrayInputStream("err".getBytes(StandardCharsets.UTF_8));

		private String method;
		private boolean doOutput;
		private int connectTimeout;
		private int readTimeout;

		protected FakeHttpURLConnection(URL u) {
			super(u);
		}

		@Override
		public void disconnect() {
			// no-op
		}

		@Override
		public boolean usingProxy() {
			return false;
		}

		@Override
		public void connect() throws IOException {
			// no-op
		}

		@Override
		public void setRequestProperty(String key, String value) {
			requestProps.put(key, value);
		}

		@Override
		public String getRequestProperty(String key) {
			return requestProps.get(key);
		}

		@Override
		public void setRequestMethod(String method) throws ProtocolException {
			this.method = method;
		}

		@Override
		public String getRequestMethod() {
			return method;
		}

		@Override
		public void setDoOutput(boolean dooutput) {
			this.doOutput = dooutput;
		}

		@Override
		public boolean getDoOutput() {
			return doOutput;
		}

		@Override
		public void setConnectTimeout(int timeout) {
			this.connectTimeout = timeout;
		}

		@Override
		public int getConnectTimeout() {
			return connectTimeout;
		}

		@Override
		public void setReadTimeout(int timeout) {
			this.readTimeout = timeout;
		}

		@Override
		public int getReadTimeout() {
			return readTimeout;
		}

		@Override
		public int getResponseCode() {
			return code;
		}

		@Override
		public String getResponseMessage() {
			return message;
		}

		@Override
		public java.io.InputStream getInputStream() {
			return input;
		}

		@Override
		public java.io.InputStream getErrorStream() {
			return error;
		}

		@Override
		public OutputStream getOutputStream() {
			return requestBody;
		}

		private void setResponseCodeAndMessage(int code, String message) {
			this.code = code;
			this.message = message;
		}

		private void setInput(String body) {
			this.input = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
		}

		private void setError(String body) {
			this.error = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
		}

		private String getWrittenBodyUtf8() {
			return new String(requestBody.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	@Test
	void applySelectorIfPresent_whenBlankSelector_returnsOriginal() {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		String html = "<html><body><div>Hi</div></body></html>";

		// Act
		String result = tools.applySelectorIfPresent(" ", html);

		// Assert
		assertEquals(html, result);
	}

	@Test
	void applySelectorIfPresent_whenSelectorMatches_returnsOuterHtml() {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		String html = "<html><body><div class='x'>A</div><div class='x'>B</div></body></html>";

		// Act
		String result = tools.applySelectorIfPresent("div.x", html);

		// Assert
		assertTrue(result.contains("<div class=\"x\">A</div>"));
		assertTrue(result.contains("<div class=\"x\">B</div>"));
		assertFalse(result.contains("<body>"));
	}

	@Test
	void fillHeader_whenHeadersNull_doesNothing() throws IOException {
		// Sonar java:S1130 - remove redundant thrown exception type.
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());

		// Act
		tools.fillHeader(null, conn);

		// Assert
		assertNull(conn.getRequestProperty("X"));
	}

	@Test
	void fillHeader_whenHeaderLineMissingEquals_isIgnored() throws IOException {
		// Sonar java:S1130 - remove redundant thrown exception type.
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());

		// Act
		tools.fillHeader("NoEqualsHere\nX-Test=1", conn);

		// Assert
		assertNull(conn.getRequestProperty("NoEqualsHere"));
		assertEquals("1", conn.getRequestProperty("X-Test"));
	}

	@Test
	void fillHeader_whenHeadersContainPlaceholders_resolvesUsingConfigurator() throws IOException {
		// Sonar java:S1130 - remove redundant thrown exception type.
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		Map<String, String> map = new HashMap<>();
		map.put("token", "abc");
		tools.setConfigurator(new MapConfigurator(map));

		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());

		// Act
		tools.fillHeader("Authorization=Bearer ${token}\nX-Test=1", conn);

		// Assert
		assertEquals("Bearer abc", conn.getRequestProperty("Authorization"));
		assertEquals("1", conn.getRequestProperty("X-Test"));
	}

	@Test
	void getWebPage_whenErrorCode_usesErrorStreamAndPrefixesStatusLine() throws Exception {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());
		conn.setResponseCodeAndMessage(404, "Not Found");
		conn.setError("err-body");

		// Act
		String result = tools.getWebPage(conn, 1000, StandardCharsets.UTF_8.name());

		// Assert
		assertTrue(result.startsWith("HTTP 404 Not Found\n"));
		assertTrue(result.contains("err-body\n"));
	}

	@Test
	void getWebPage_whenSuccess_usesInputStreamAndPrefixesStatusLine() throws Exception {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());
		conn.setResponseCodeAndMessage(200, "OK");
		conn.setInput("ok-body");

		// Act
		String result = tools.getWebPage(conn, 1000, StandardCharsets.UTF_8.name());

		// Assert
		assertTrue(result.startsWith("HTTP 200 OK\n"));
		assertTrue(result.contains("ok-body\n"));
	}

	@Test
	void getConnection_whenNoUserInfo_opensConnection() throws Exception {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		URI uri = URI.create("http://example.com/path");

		// Act
		HttpURLConnection conn = tools.getConnection(uri, "X-Test=1");

		// Assert
		assertNotNull(conn);
		assertEquals("1", conn.getRequestProperty("X-Test"));
	}

	@Test
	void getConnection_whenUserInfoProvided_doesNotIncludeUserInfoInUrlString() throws Exception {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		URI uri = URI.create("http://user:pass@example.com/path");

		// Act
		HttpURLConnection connection = tools.getConnection(uri, "X-Test=1");

		// Assert
		assertNotNull(connection);
		assertFalse(connection.getURL().toString().contains("user:pass@"));
	}

	@Test
	void callRestApi_whenResponseStreamNull_returnsFallbackMessage()  {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				return new HttpURLConnection(urlFrom(uri)) {
					@Override
					public void disconnect() {
						// no-op
					}

					@Override
					public boolean usingProxy() {
						return false;
					}

					@Override
					public void connect() {
						// no-op
					}

					@Override
					public int getResponseCode() {
						return 204;
					}

					@Override
					public String getResponseMessage() {
						return "No Content";
					}

					@Override
					public String getRequestMethod() {
						return "GET";
					}

					@Override
					public java.io.InputStream getInputStream() {
						return null;
					}

					@Override
					public java.io.InputStream getErrorStream() {
						return null;
					}
				};
			}
		};

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode props = mapper.createObjectNode();
		props.put("url", "http://example.com/api");
		props.put("method", "GET");

		// Act
		String result = tools.callRestApi(new Object[] { props });

		// Assert
		assertTrue(result.startsWith("ResponseCode: 204 GET"));
	}

	@Test
	void callRestApi_whenResponseBodyPresent_returnsFullResponseText() throws Exception {
		// Arrange
		FakeHttpURLConnection fakeConn = new FakeHttpURLConnection(URI.create("http://example.com/api").toURL());
		fakeConn.setResponseCodeAndMessage(200, "OK");
		fakeConn.setInput("hello\nworld\n");

		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				return fakeConn;
			}
		};

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode props = mapper.createObjectNode();
		props.put("url", "http://example.com/api");
		props.put("method", "GET");

		// Act
		String result = tools.callRestApi(new Object[] { props });

		// Assert
		assertTrue(result.startsWith("HTTP 200 OK\n"));
		assertTrue(result.contains("hello\n"));
		assertTrue(result.contains("world\n"));
	}

	@Test
	void callRestApi_whenBodyWithPost_writesBodyAndEnablesOutput() throws Exception {
		// Arrange
		FakeHttpURLConnection fakeConn = new FakeHttpURLConnection(URI.create("http://example.com/api").toURL());
		fakeConn.setResponseCodeAndMessage(200, "OK");
		fakeConn.setInput("ok\n");

		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				return fakeConn;
			}
		};

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode props = mapper.createObjectNode();
		props.put("url", "http://example.com/api");
		props.put("method", "POST");
		props.put("body", "payload");

		// Act
		String result = tools.callRestApi(new Object[] { props });

		// Assert
		assertTrue(result.startsWith("HTTP 200 OK\n"));
		assertTrue(fakeConn.getDoOutput());
		assertEquals("payload", fakeConn.getWrittenBodyUtf8());
	}

	@Test
	void getWebContent_whenFileSchemeReadsRelativeToWorkingDir_andThenTextOnlyViaReflection() throws Exception {
		// Arrange
		File file = new File(tempDir, "page.html");
		java.nio.file.Files.write(file.toPath(),
				"<html><body><h1>Title</h1><p>Text</p></body></html>".getBytes(StandardCharsets.UTF_8));

		WebFunctionTools tools = new WebFunctionTools();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode props = mapper.createObjectNode();
		props.put("url", "file:page.html");
		props.put("charsetName", StandardCharsets.UTF_8.name());

		// Act
		String fileHtml = tools.getWebContent(new Object[] { props, tempDir });
		String plain = invokeRenderTextOnly(tools, true, fileHtml);

		// Assert
		assertNotNull(fileHtml);
		assertNotNull(plain);
		assertFalse(plain.contains("<h1"));
	}

	@Test
	void getWebContent_whenHttpSchemeFetchesAndReturnsTextOnly()  {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				try {
					FakeHttpURLConnection conn = new FakeHttpURLConnection(urlFrom(uri));
					conn.setResponseCodeAndMessage(200, "OK");
					conn.setInput("<html><body><p>Hello</p></body></html>");
					return conn;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode props = mapper.createObjectNode();
		props.put("url", "http://example.com");
		props.put("textOnly", true);

		// Act
		String result = tools.getWebContent(new Object[] { props, tempDir });

		// Assert
		assertTrue(result.startsWith("HTTP 200 OK\n"));
		assertTrue(result.contains("Hello"));
		assertFalse(result.contains("<p>"));
	}

	@Test
	void getWebContent_whenExceptionOccurs_returnsIoErrorMessage() {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) throws IOException {
				throw new IOException("boom");
			}
		};

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode props = mapper.createObjectNode();
		props.put("url", "http://example.com");

		// Act
		String result = tools.getWebContent(new Object[] { props, tempDir });

		// Assert
		assertTrue(result.startsWith("IO Error: boom"));
	}

	@Test
	void renderTextOnlyIfRequested_whenFalse_returnsOriginal() {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();

		// Act
		String result = invokeRenderTextOnly(tools, false, "<p>X</p>");

		// Assert
		assertEquals("<p>X</p>", result);
	}

	@Test
	void renderTextOnlyIfRequested_whenTrue_rendersPlainText() {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();

		// Act
		String result = invokeRenderTextOnly(tools, true, "<html><body><p>X</p></body></html>");

		// Assert
		assertTrue(result.contains("X"));
		assertFalse(result.contains("<p>"));
	}

	// Sonar java:S1874 - avoid using deprecated URL( String ) constructor.
	private static URL urlFrom(URI uri) {
		try {
			return uri.toURL();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static String invokeRenderTextOnly(WebFunctionTools tools, boolean textOnly, String input) {
		try {
			Method m = WebFunctionTools.class.getDeclaredMethod("renderTextOnlyIfRequested", boolean.class, String.class);
			m.setAccessible(true);
			return (String) m.invoke(tools, textOnly, input);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}
}
