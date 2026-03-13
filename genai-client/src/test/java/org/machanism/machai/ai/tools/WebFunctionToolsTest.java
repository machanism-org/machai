package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class WebFunctionToolsTest {

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
		public java.io.File getFile(String key) {
			String val = get(key);
			return val == null ? null : new java.io.File(val);
		}

		@Override
		public java.io.File getFile(String key, java.io.File defaultValue) {
			java.io.File val = getFile(key);
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
		private int code = 200;
		private String message = "OK";
		private ByteArrayInputStream input = new ByteArrayInputStream("body".getBytes(StandardCharsets.UTF_8));
		private ByteArrayInputStream error = new ByteArrayInputStream("err".getBytes(StandardCharsets.UTF_8));

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

		private void setResponseCodeAndMessage(int code, String message) {
			this.code = code;
			this.message = message;
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
	void fillHeader_whenHeadersNull_doesNothing() throws Exception {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools();
		FakeHttpURLConnection conn = new FakeHttpURLConnection(URI.create("http://example.com").toURL());

		// Act
		tools.fillHeader(null, conn);

		// Assert
		assertNull(conn.getRequestProperty("X"));
	}

	@Test
	void fillHeader_whenHeadersContainPlaceholders_resolvesUsingConfigurator() throws Exception {
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

		// Act
		String result = tools.getWebPage(conn, 1000, StandardCharsets.UTF_8.name());

		// Assert
		assertTrue(result.startsWith("HTTP 404 Not Found\n"));
		assertTrue(result.contains("err"));
	}

	@Test
	void getConnection_whenUserInfoProvided_doesNotIncludeUserInfoInUrlString() throws Exception {
		// Arrange
		WebFunctionTools tools = new WebFunctionTools() {
			@Override
			HttpURLConnection getConnection(URI uri, String headers) throws IOException {
				return super.getConnection(uri, headers);
			}
		};

		URI uri = URI.create("http://user:pass@example.com/path");

		// Act
		HttpURLConnection connection = tools.getConnection(uri, "X-Test=1");

		// Assert
		assertNotNull(connection);
		assertFalse(connection.getURL().toString().contains("user:pass@"));
		// do not assert Authorization header here: HttpURLConnection may restrict reading it back
	}
}
