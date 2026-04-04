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
import org.machanism.machai.ai.manager.Genai;

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
			// Sonar fix java:S1186: empty override is intentional for the HttpURLConnection test double.
		}

		@Override
		public boolean usingProxy() {
			return false;
		}

		@Override
		public void connect() throws IOException {
			// Sonar fix java:S1186: empty override is intentional for the HttpURLConnection test double.
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

	private static final class RecordingGenai implements Genai {
		private final Map<String, ToolFunction> tools = new HashMap<>();

		@Override
		public void init(Configurator configurator) {
			// Sonar fix java:S1186: test stub intentionally does nothing because initialization is not exercised here.
		}

		@Override
		public void prompt(String prompt) {
			// Sonar fix java:S1186: test stub intentionally does nothing because prompt execution is not exercised here.
		}

		@Override
		public java.util.List<Double> embedding(String text, long timeout) {
			return null;
		}

		@Override
		public void clear() {
			// Sonar fix java:S1186: test stub intentionally does nothing because state reset is not exercised here.
		}

		@Override
		public void addTool(String name, String description, ToolFunction function, String... schema) {
			tools.put(name, function);
		}

		@Override
		public void instructions(String instructions) {
			// Sonar fix java:S1186: test stub intentionally does nothing because instructions are not asserted in this test.
		}

		@Override
		public String perform() {
			return null;
		}

		@Override
		public void inputsLog(File file) {
			// Sonar fix java:S1186: test stub intentionally does nothing because input logging is not exercised here.
		}

		@Override
		public void setWorkingDir(File workDir) {
			// Sonar fix java:S1186: test stub intentionally does nothing because working directory mutation is not exercised here.
		}

		@Override
		public org.machanism.machai.ai.manager.Usage usage() {
			return null;
		}
	}

	@Test
	void applyTools_registersBothWebTools() {
		WebFunctionTools tools = new WebFunctionTools();
		RecordingGenai genai = new RecordingGenai();

		tools.applyTools(genai);

		assertTrue(genai.tools.containsKey("get_web_content"));
		assertTrue(genai.tools.containsKey("call_rest_api"));
	}

	@Test
	void applySelectorIfPresent_whenBlankSelector_returnsOriginal() {
		WebFunctionTools tools = new WebFunctionTools();
		String html = "<html><body><div>Hi</div></body></html>";

		String result = tools.applySelectorIfPresent(" ", html);

		assertEquals(html, result);
	}

	@Test
	void applySelectorIfPresent_whenSelectorMatches_returnsOuterHtml() {
		WebFunctionTools tools = new WebFunctionTools();
		String html = "<html><body><div class='x'>A</div><div class='x'>B</div></body></html>";

		String result = tools.applySelectorIfPresent("div.x", html);

		assertTrue(result.contains("<div class=\"x\">A</div>"));
		assertTrue(result.contains("<div class=\"x\">B</div>"));
		assertFalse(result.contains("<body>"));
	}

	@Test
	void fillHeader_whenHeadersNull_doesNothing() throws IOException {
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());

		tools.fillHeader(null, conn);

		assertNull(conn.getRequestProperty("X"));
	}

	@Test
	void fillHeader_whenHeaderLineMissingEquals_isIgnored() throws IOException {
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());

		tools.fillHeader("NoEqualsHere\nX-Test=1", conn);

		assertNull(conn.getRequestProperty("NoEqualsHere"));
		assertEquals("1", conn.getRequestProperty("X-Test"));
	}

	@Test
	void fillHeader_whenHeadersContainPlaceholders_resolvesUsingConfigurator() throws IOException {
		WebFunctionTools tools = new WebFunctionTools();
		Map<String, String> map = new HashMap<>();
		map.put("token", "abc");
		tools.setConfigurator(new MapConfigurator(map));
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());

		tools.fillHeader("Authorization=Bearer ${token}\nX-Test=1", conn);

		assertEquals("Bearer abc", conn.getRequestProperty("Authorization"));
		assertEquals("1", conn.getRequestProperty("X-Test"));
	}

	@Test
	void getWebPage_whenErrorCode_usesErrorStreamAndPrefixesStatusLine() throws Exception {
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());
		conn.setResponseCodeAndMessage(404, "Not Found");
		conn.setError("err-body");

		String result = tools.getWebPage(conn, 1000, StandardCharsets.UTF_8.name());

		assertTrue(result.startsWith("HTTP 404 Not Found\n"));
		assertTrue(result.contains("err-body\n"));
	}

	@Test
	void getWebPage_whenSuccess_usesInputStreamAndPrefixesStatusLine() throws Exception {
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());
		conn.setResponseCodeAndMessage(200, "OK");
		conn.setInput("ok-body");

		String result = tools.getWebPage(conn, 1000, StandardCharsets.UTF_8.name());

		assertTrue(result.startsWith("HTTP 200 OK\n"));
		assertTrue(result.contains("ok-body\n"));
		assertEquals("GET", conn.getRequestMethod());
		assertEquals(1000, conn.getConnectTimeout());
		assertEquals(1000, conn.getReadTimeout());
	}

	@Test
	void getConnection_whenNoUserInfo_opensConnection() throws Exception {
		WebFunctionTools tools = new WebFunctionTools();
		URI uri = URI.create("http://example.com/path");

		HttpURLConnection conn = tools.getConnection(uri, "X-Test=1");

		assertNotNull(conn);
		assertEquals("1", conn.getRequestProperty("X-Test"));
	}

	@Test
	void getConnection_whenUserInfoProvided_doesNotIncludeUserInfoInUrlString() throws Exception {
		WebFunctionTools tools = new WebFunctionTools();
		URI uri = URI.create("http://user:pass@example.com/path");

		HttpURLConnection connection = tools.getConnection(uri, "X-Test=1");

		assertNotNull(connection);
		assertFalse(connection.getURL().toString().contains("user:pass@"));
	}

	@Test
	void callRestApi_whenResponseStreamNull_returnsFallbackMessage() {
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				return new HttpURLConnection(urlFrom(uri)) {
					@Override
					public void disconnect() {
						// Sonar fix java:S1186: empty override is intentional for the HttpURLConnection test double.
					}

					@Override
					public boolean usingProxy() {
						return false;
					}

					@Override
					public void connect() {
						// Sonar fix java:S1186: empty override is intentional for the HttpURLConnection test double.
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

		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "http://example.com/api");
		props.put("method", "GET");

		String result = tools.callRestApi(new Object[] { props });

		assertTrue(result.startsWith("ResponseCode: 204 GET"));
	}

	@Test
	void callRestApi_whenResponseBodyPresent_returnsFullResponseText() throws Exception {
		FakeHttpURLConnection fakeConn = new FakeHttpURLConnection(URI.create("http://example.com/api").toURL());
		fakeConn.setResponseCodeAndMessage(200, "OK");
		fakeConn.setInput("hello\nworld\n");

		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				return fakeConn;
			}
		};

		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "http://example.com/api");
		props.put("method", "GET");

		String result = tools.callRestApi(new Object[] { props });

		assertTrue(result.startsWith("HTTP 200 OK\n"));
		assertTrue(result.contains("hello\n"));
		assertTrue(result.contains("world\n"));
	}

	@Test
	void callRestApi_whenBodyWithPost_writesBodyAndEnablesOutput() throws Exception {
		FakeHttpURLConnection fakeConn = new FakeHttpURLConnection(URI.create("http://example.com/api").toURL());
		fakeConn.setResponseCodeAndMessage(200, "OK");
		fakeConn.setInput("ok\n");

		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				return fakeConn;
			}
		};

		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "http://example.com/api");
		props.put("method", "POST");
		props.put("body", "payload");

		String result = tools.callRestApi(new Object[] { props });

		assertTrue(result.startsWith("HTTP 200 OK\n"));
		assertTrue(fakeConn.getDoOutput());
		assertEquals("payload", fakeConn.getWrittenBodyUtf8());
	}

	@Test
	void callRestApi_whenCharsetAndHeadersProvided_appliesThemToConnection() throws Exception {
		FakeHttpURLConnection fakeConn = new FakeHttpURLConnection(URI.create("http://example.com/api").toURL());
		fakeConn.setResponseCodeAndMessage(200, "OK");
		fakeConn.setInput("ok");
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				assertEquals("X-Test=1", headers);
				return fakeConn;
			}
		};

		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "http://example.com/api");
		props.put("method", "PATCH");
		props.put("body", "payload");
		props.put("headers", "X-Test=1");
		props.put("timeout", 3210);
		props.put("charsetName", StandardCharsets.UTF_8.name());

		tools.callRestApi(new Object[] { props });

		assertEquals("PATCH", fakeConn.getRequestMethod());
		assertEquals(3210, fakeConn.getConnectTimeout());
		assertEquals(3210, fakeConn.getReadTimeout());
	}

	@Test
	void callRestApi_whenConnectionThrows_returnsIoErrorMessage() {
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) throws IOException {
				throw new IOException("rest-boom");
			}
		};

		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "http://example.com/api");

		String result = tools.callRestApi(new Object[] { props });

		assertTrue(result.startsWith("IO Error: rest-boom"));
	}

	@Test
	void getWebContent_whenFileSchemeReadsRelativeToWorkingDir_andThenTextOnlyViaReflection() throws Exception {
		File file = new File(tempDir, "page.html");
		java.nio.file.Files.write(file.toPath(),
				"<html><body><h1>Title</h1><p>Text</p></body></html>".getBytes(StandardCharsets.UTF_8));

		WebFunctionTools tools = new WebFunctionTools();
		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "file:page.html");
		props.put("charsetName", StandardCharsets.UTF_8.name());

		String fileHtml = tools.getWebContent(new Object[] { props, tempDir });
		String plain = invokeRenderTextOnly(tools, true, fileHtml);

		assertNotNull(fileHtml);
		assertNotNull(plain);
		assertFalse(plain.contains("<h1"));
	}

	@Test
	void getWebContent_whenRelativeFileWithoutPath_returnsIoErrorMessage() {
		WebFunctionTools tools = new WebFunctionTools();
		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "file:missing.txt");

		String result = tools.getWebContent(new Object[] { props, tempDir });

		assertTrue(result.startsWith("IO Error:"));
	}

	@Test
	void getWebContent_whenAbsoluteFileSchemeReadsAbsolutePath() throws Exception {
		File file = new File(tempDir, "absolute.html");
		java.nio.file.Files.write(file.toPath(), "absolute".getBytes(StandardCharsets.UTF_8));
		WebFunctionTools tools = new WebFunctionTools();
		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", file.toURI().toString());

		String result = tools.getWebContent(new Object[] { props, tempDir });

		assertEquals("absolute", result);
	}

	@Test
	void getWebContent_whenSelectorProvidedAndTextOnlyTrue_filtersHtmlThenRendersText() {
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				try {
					FakeHttpURLConnection conn = new FakeHttpURLConnection(urlFrom(uri));
					conn.setResponseCodeAndMessage(200, "OK");
					conn.setInput("<html><body><div class='keep'><p>Hello</p></div><div>Ignore</div></body></html>");
					return conn;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "http://example.com");
		props.put("textOnly", true);
		props.put("selector", "div.keep");

		String result = tools.getWebContent(new Object[] { props, tempDir });

		assertTrue(result.contains("Hello"));
		assertFalse(result.contains("Ignore"));
		assertFalse(result.contains("<div"));
	}

	@Test
	void getWebContent_whenHttpSchemeFetchesAndReturnsTextOnly() {
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

		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "http://example.com");
		props.put("textOnly", true);

		String result = tools.getWebContent(new Object[] { props, tempDir });

		assertTrue(result.startsWith("HTTP 200 OK\n"));
		assertTrue(result.contains("Hello"));
		assertFalse(result.contains("<p>"));
	}

	@Test
	void getWebContent_whenUrlUsesConfiguratorPlaceholder_replacesBeforeRequest() {
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) {
				assertEquals("http://example.com/final", uri.toString());
				try {
					FakeHttpURLConnection conn = new FakeHttpURLConnection(urlFrom(uri));
					conn.setResponseCodeAndMessage(200, "OK");
					conn.setInput("done");
					return conn;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		Map<String, String> map = new HashMap<>();
		map.put("target.url", "http://example.com/final");
		tools.setConfigurator(new MapConfigurator(map));
		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "${target.url}");

		String result = tools.getWebContent(new Object[] { props, tempDir });

		assertTrue(result.contains("done"));
	}

	@Test
	void getWebContent_whenExceptionOccurs_returnsIoErrorMessage() {
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) throws IOException {
				throw new IOException("boom");
			}
		};

		ObjectNode props = new ObjectMapper().createObjectNode();
		props.put("url", "http://example.com");

		String result = tools.getWebContent(new Object[] { props, tempDir });

		assertTrue(result.startsWith("IO Error: boom"));
	}

	@Test
	void renderTextOnlyIfRequested_whenFalse_returnsOriginal() {
		WebFunctionTools tools = new WebFunctionTools();

		String result = invokeRenderTextOnly(tools, false, "<p>X</p>");

		assertEquals("<p>X</p>", result);
	}

	@Test
	void renderTextOnlyIfRequested_whenTrue_rendersPlainText() {
		WebFunctionTools tools = new WebFunctionTools();

		String result = invokeRenderTextOnly(tools, true, "<html><body><p>X</p></body></html>");

		assertTrue(result.contains("X"));
		assertFalse(result.contains("<p>"));
	}

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
