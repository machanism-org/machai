package org.machanism.machai.ai.provider.codemie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.provider.GenaiAdapter;
import org.machanism.machai.ai.provider.claude.ClaudeProvider;
import org.machanism.machai.ai.provider.openai.OpenAIProvider;

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
		System.clearProperty(CodeMieProvider.AUTH_URL_PROP_NAME);
		System.clearProperty(ClaudeProvider.ANTHROPIC_BASE_URL);
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
	void getToken_success_returnsNullWhenAccessTokenIsLastFieldWithoutTrailingComma() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/token", new TokenHandler(200, "{\"access_token\":\"tailToken\"}"));
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
	void getToken_whenConnectionCannotBeOpened_propagatesIOException() {
		String url = "http://127.0.0.1:1/token";

		IOException ex = assertThrows(IOException.class, () -> CodeMieProvider.getToken(url, "user@example.com", "pw"));

		assertTrue(ex.getMessage() == null || !ex.getMessage().isEmpty());
	}

	@Test
	void getToken_invalidUrl_throwsIllegalArgumentExceptionFromUriCreate() {
		assertThrows(IllegalArgumentException.class, () -> CodeMieProvider.getToken("http://[invalid", "u", "p"));
	}

	@Test
	void init_blankModel_usesOpenAiProviderAndConfiguresBaseUrlAndSystemProperty() throws Exception {
		CodeMieProvider provider = new CodeMieProvider();
		MapBackedConfigurator config = new MapBackedConfigurator();
		config.set(CodeMieProvider.AUTH_URL_PROP_NAME, "https://custom-auth.example/token");

		provider.init("", config);

		Object delegate = getDelegate(provider);
		assertNotNull(delegate);
		assertTrue(OpenAIProvider.class.isAssignableFrom(delegate.getClass()));
		assertEquals(CodeMieProvider.BASE_URL, config.get(OpenAIProvider.OPENAI_BASE_URL_NAME));
		assertEquals("https://custom-auth.example/token", System.getProperty(CodeMieProvider.AUTH_URL_PROP_NAME));
	}

	@Test
	void init_openAiCompatibleModel_usesOpenAiProvider() throws Exception {
		CodeMieProvider provider = new CodeMieProvider();
		MapBackedConfigurator config = new MapBackedConfigurator();

		provider.init("gpt-4o-mini", config);

		assertTrue(OpenAIProvider.class.isAssignableFrom(getDelegate(provider).getClass()));
	}

	@Test
	void init_embeddingCompatibleModel_usesOpenAiProvider() throws Exception {
		CodeMieProvider provider = new CodeMieProvider();
		MapBackedConfigurator config = new MapBackedConfigurator();

		provider.init("text-embedding-3-small", config);

		assertTrue(OpenAIProvider.class.isAssignableFrom(getDelegate(provider).getClass()));
	}

	@Test
	void init_codemieEmbeddingCompatibleModel_usesOpenAiProvider() throws Exception {
		CodeMieProvider provider = new CodeMieProvider();
		MapBackedConfigurator config = new MapBackedConfigurator();

		provider.init("codemie-text-embedding-large", config);

		assertTrue(OpenAIProvider.class.isAssignableFrom(getDelegate(provider).getClass()));
	}

	@Test
	void init_amazonEmbeddingCompatibleModel_usesOpenAiProvider() throws Exception {
		CodeMieProvider provider = new CodeMieProvider();
		MapBackedConfigurator config = new MapBackedConfigurator();

		provider.init("amazon.titan-embed-text-v2:0", config);

		assertTrue(OpenAIProvider.class.isAssignableFrom(getDelegate(provider).getClass()));
	}

	@Test
	void init_claudeModel_usesClaudeProviderAndConfiguresBaseUrls() throws Exception {
		CodeMieProvider provider = new CodeMieProvider();
		MapBackedConfigurator config = new MapBackedConfigurator();
		config.set(CodeMieProvider.AUTH_URL_PROP_NAME, "https://claude-auth.example/token");

		provider.init("claude-3-5-sonnet", config);

		Object delegate = getDelegate(provider);
		assertNotNull(delegate);
		assertTrue(ClaudeProvider.class.isAssignableFrom(delegate.getClass()));
		assertEquals(CodeMieProvider.BASE_URL, config.get(ClaudeProvider.ANTHROPIC_BASE_URL));
		assertEquals("https://claude-auth.example/token", System.getProperty(ClaudeProvider.ANTHROPIC_BASE_URL));
	}

	@Test
	void init_unsupportedModel_throwsIllegalArgumentException() {
		CodeMieProvider provider = new CodeMieProvider();
		MapBackedConfigurator config = new MapBackedConfigurator();

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> provider.init("unsupported-model", config));

		assertEquals("Unsupported model: 'unsupported-model'.", ex.getMessage());
	}

	@Test
	void embedding_delegatesToEmbeddingProvider() throws Exception {
		CodeMieProvider provider = new CodeMieProvider();
		List<Double> expected = java.util.Arrays.asList(1.0d, 2.5d, 3.75d);
		setDelegate(provider, new StubEmbeddingGenai(expected));
		setModel(provider, "text-embedding-3-small");

		List<Double> actual = provider.embedding("hello", 3L);

		assertSame(expected, actual);
	}

	@Test
	void embedding_whenDelegateDoesNotSupportEmbeddings_throwsIllegalArgumentException() throws Exception {
		CodeMieProvider provider = new CodeMieProvider();
		setDelegate(provider, new StubGenai());
		setModel(provider, "claude-3-5-sonnet");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> provider.embedding("hello", 10L));

		assertEquals("embedding not support for `claude-3-5-sonnet`", ex.getMessage());
	}

	private static Object getDelegate(CodeMieProvider provider) throws Exception {
		Field field = GenaiAdapter.class.getDeclaredField("provider");
		field.setAccessible(true);
		return field.get(provider);
	}

	private static void setDelegate(CodeMieProvider provider, Genai delegate) throws Exception {
		Field field = GenaiAdapter.class.getDeclaredField("provider");
		field.setAccessible(true);
		field.set(provider, delegate);
	}

	private static void setModel(CodeMieProvider provider, String value) throws Exception {
		Field field = CodeMieProvider.class.getDeclaredField("model");
		field.setAccessible(true);
		field.set(provider, value);
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

	private static final class MapBackedConfigurator implements Configurator {
		private final Map<String, String> values = new HashMap<String, String>();

		private MapBackedConfigurator() {
			values.put(Genai.USERNAME_PROP_NAME, "user@example.com");
			values.put(Genai.PASSWORD_PROP_NAME, "password");
		}

		@Override
		public String get(String key) {
			String value = values.get(key);
			if (value == null) {
				throw new IllegalArgumentException("Missing config key: " + key);
			}
			return value;
		}

		@Override
		public String get(String key, String defaultValue) {
			return values.getOrDefault(key, defaultValue);
		}

		@Override
		public int getInt(String key) {
			return Integer.parseInt(get(key));
		}

		@Override
		public Integer getInt(String key, Integer defaultValue) {
			String value = values.get(key);
			return value == null ? defaultValue : Integer.valueOf(value);
		}

		@Override
		public boolean getBoolean(String key) {
			return Boolean.parseBoolean(get(key));
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			String value = values.get(key);
			return value == null ? defaultValue : Boolean.valueOf(value);
		}

		@Override
		public long getLong(String key) {
			return Long.parseLong(get(key));
		}

		@Override
		public Long getLong(String key, Long defaultValue) {
			String value = values.get(key);
			return value == null ? defaultValue : Long.valueOf(value);
		}

		@Override
		public File getFile(String key) {
			return new File(get(key));
		}

		@Override
		public File getFile(String key, File defaultValue) {
			String value = values.get(key);
			return value == null ? defaultValue : new File(value);
		}

		@Override
		public double getDouble(String key) {
			return Double.parseDouble(get(key));
		}

		@Override
		public Double getDouble(String key, Double defaultValue) {
			String value = values.get(key);
			return value == null ? defaultValue : Double.valueOf(value);
		}

		@Override
		public String getName() {
			return "test";
		}

		@Override
		public void set(String key, String value) {
			values.put(key, value);
		}
	}

	private static final class StubEmbeddingGenai implements Genai, EmbeddingProvider {
		private final List<Double> response;

		private StubEmbeddingGenai(List<Double> response) {
			this.response = response;
		}

		@Override
		public void init(String model, Configurator conf) {
		}

		@Override
		public List<Double> embedding(String text, long dimensions) {
			return response;
		}

		@Override
		public void prompt(String text) {
		}

		@Override
		public void clear() {
		}

		@Override
		public void addTool(String name, String description, org.machanism.machai.ai.tools.ToolFunction function,
				String... paramsDesc) {
		}

		@Override
		public void instructions(String instructions) {
		}

		@Override
		public String perform() {
			return null;
		}

		@Override
		public void inputsLog(File bindexTempDir) {
		}

		@Override
		public void setWorkingDir(File workingDir) {
		}

		@Override
		public org.machanism.machai.ai.manager.Usage usage() {
			return null;
		}
	}

	private static final class StubGenai implements Genai {
		@Override
		public void init(String model, Configurator conf) {
		}

		@Override
		public void instructions(String instructions) {
		}

		@Override
		public void setWorkingDir(File workingDir) {
		}

		@Override
		public void inputsLog(File logDir) {
		}

		@Override
		public String perform() {
			return null;
		}

		@Override
		public void prompt(String prompt) {
		}

		@Override
		public void addTool(String name, String description, org.machanism.machai.ai.tools.ToolFunction function,
				String... parameters) {
		}

		@Override
		public void clear() {
		}

		@Override
		public org.machanism.machai.ai.manager.Usage usage() {
			return null;
		}
	}
}
