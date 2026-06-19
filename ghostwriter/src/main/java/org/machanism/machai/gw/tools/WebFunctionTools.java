package org.machanism.machai.gw.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jsoup.Jsoup;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.htmlparser.jericho.Source;

/**
 * Provides host-side HTTP retrieval tools for a {@link Genai} provider.
 *
 * <p>
 * This tool set exposes two main functions:
 * </p>
 * <ul>
 *   <li><b>{@code get_web_content}</b> – Fetches web page content over HTTP(S) via GET,
 *       optionally returning plain text or content selected via a CSS selector.</li>
 *   <li><b>{@code call_rest_api}</b> – Executes a generic REST call using an arbitrary
 *       HTTP method with optional headers and request body.</li>
 * </ul>
 *
 * <h2>Header variable placeholders</h2>
 * <p>
 * Header values may include placeholders in the form <code>${propertyName}</code>. When a
 * {@link Configurator} is provided, those placeholders are resolved at runtime.
 * </p>
 *
 * <h2>Authentication</h2>
 * <p>
 * HTTP Basic authentication is supported via the URL {@code userInfo} component
 * (e.g., <code>https://user:password@host/path</code>), which is converted
 * into an <code>Authorization: Basic ...</code> header. You can also specify an
 * explicit <code>Authorization</code> header.
 * </p>
 *
 * <p>
 * Outbound network policy (allow/deny lists) is intentionally left to the host
 * application.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * WebFunctionTools tools = new WebFunctionTools();
 * String html = tools.getWebContent("https://example.com", null, 5000, "UTF-8", false, "", projectDir, configurator);
 * String apiResult = tools.callRestApi("https://api.example.com", "POST", headers, body, 5000, "UTF-8", projectDir, configurator);
 * }</pre>
 *
 * @author Viktor Tovstyi
 */
public class WebFunctionTools implements FunctionTools {

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final SecureRandom REQUEST_ID_RANDOM = new SecureRandom();

	/** Logger for web fetch tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(WebFunctionTools.class);

	/**
	 * Fetches the content of a web page using an HTTP GET request.
	 *
	 * <p>
	 * Supports userInfo format in the URL for basic authentication, custom headers,
	 * timeout, charset, plain text extraction, and CSS selector filtering.
	 * If the URL uses the {@code file} scheme, content is read from the local file system.
	 * </p>
	 *
	 * <p>
	 * If {@code textOnly} is true, the returned content is stripped of HTML tags and rendered as plain text.
	 * If {@code selector} is provided, only the content matching the specified CSS selector is returned.
	 * If both {@code selector} and {@code textOnly} are set, only the text of the selected elements is returned.
	 * </p>
	 *
	 * <p>
	 * Header values may include property placeholders resolved via the provided {@link Configurator}.
	 * HTTP Basic authentication is supported via userInfo in the URL (e.g., {@code https://user:password@host/path}).
	 * </p>
	 *
	 * @param url         The URL of the web page to fetch. Supports userInfo format (e.g., https://user:password@host/path) for basic authentication.
	 * @param headers     Specifies HTTP header properties. If null, no additional headers are sent.
	 * @param timeout     The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.
	 * @param charsetName The name of the character set to use when decoding the response content. Default: UTF-8.
	 * @param textOnly    If true, only the plain text content of the web page is returned (HTML tags are stripped). If false or not specified, the full HTML content is returned.
	 * @param selector    If provided, extracts and returns only the content matching the specified CSS selector. If textOnly is also true, returns only the text of the selected elements; otherwise, returns their HTML.
	 * @param projectDir  The project directory context for file-based URLs.
	 * @param configurator The configuration object for property resolution and header placeholder substitution.
	 * @return The fetched web content as a string, or an error message if the fetch fails.
	 */
	@Tool(name = "get_web_content", description = "Fetches the content of a web page using an HTTP GET request. The URL may include user credentials in the userInfo format "
			+ "(e.g., https://user:password@host/path) for basic authentication.")
	public String getWebContent(
			@Param(name = "url", description = "The URL of the web page to fetch. Supports userInfo format (e.g., https://user:password@host/path) for basic authentication.") String url,
			@Param(name = "headers", description = "Specifies HTTP header properties. If null, no additional headers are sent.", defaultValue = "") Map<String, String> headers,
			@Param(name = "timeout", description = "The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.", defaultValue = "0") int timeout,
			@Param(name = "charset_name", description = "The name of the character set to use when decoding the response content. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "text_only", description = "If true, only the plain text content of the web page is returned (HTML tags are stripped). If false or not specified, the full HTML content is returned.", defaultValue = "false") boolean textOnly,
			@Param(name = "selector", description = "If provided, extracts and returns only the content matching the specified CSS selector. If textOnly is also true, returns only the text of the selected elements; otherwise, returns their HTML.", defaultValue = "") String selector,
			@Param(name = "project_dir", description = "The project dir.") File projectDir, Configurator configurator) {
		String requestId = Integer.toHexString(REQUEST_ID_RANDOM.nextInt());

		url = CommandFunctionTools.replace(url, configurator);

		try {
			URI uri = URI.create(url);
			String response;
			if ("file".equals(uri.getScheme())) {
				response = readFileUriContent(projectDir, charsetName, uri);
			} else {
				response = fetchHttpContent(requestId, selector, headers, timeout, charsetName, textOnly, uri,
						configurator);
			}

			if (logger.isInfoEnabled()) {
				logger.info("[WEB {}] Downloaded web content ({} bytes): {}.", requestId, response.length(),
						StringUtils.abbreviate(response, AbstractAIProvider.LOG_LINE_LENG)
								.replace(AbstractAIProvider.LINE_SEPARATOR, " ").replace("\r", ""));
			}
			return response;

		} catch (Exception e) {
			logger.error("[WEB {}] IO error during web content fetch", requestId, e);
			return "IO Error: " + e.getMessage();
		}
	}

	private String readFileUriContent(File projectDir, String charsetName, URI uri) {
		String path = uri.getPath();
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(projectDir, path);
		}
		return readFileContent(file, charsetName);
	}

	private String fetchHttpContent(String requestId, String selector, Map<String, String> headers, int timeout, String charsetName,
			boolean textOnly, URI uri, Configurator config) throws IOException {
		HttpURLConnection connection = getConnection(uri, headers, config);
		logger.info("[WEB {}] URL: {}", requestId, connection.getURL());

		String response = getWebPage(connection, timeout, charsetName);

		String contentType = connection.getContentType();
		if (Strings.CS.contains(contentType, "html")) {
			response = applySelectorIfPresent(selector, response);
			response = renderTextOnlyIfRequested(textOnly, response);
		}

		return response;
	}

	private String readFileContent(File file, String charsetName) {
		try (FileInputStream io = new FileInputStream(file)) {
			return IOUtils.toString(io, charsetName);
		} catch (FileNotFoundException e) {
			return "File not found.";
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

    /**
     * Applies a CSS selector to the response HTML if one was provided.
     *
     * @param selector CSS selector (may be blank)
     * @param response full response content
     * @return selected HTML content (joined with newlines) or the original response
     *         if {@code selector} is blank
     */
	String applySelectorIfPresent(String selector, String response) {
		if (StringUtils.isBlank(selector)) {
			return response;
		}

		org.jsoup.nodes.Document doc = Jsoup.parse(response);
		org.jsoup.select.Elements elements = doc.select(selector);
		StringBuilder selectedContent = new StringBuilder();
		for (org.jsoup.nodes.Element element : elements) {
			selectedContent.append(element.outerHtml()).append(AbstractAIProvider.LINE_SEPARATOR);
		}
		return selectedContent.toString().trim();
	}

    /**
     * Converts the response to plain text when requested.
     *
     * @param textOnly whether to render text only
     * @param response response content (typically HTML)
     * @return rendered text content if {@code textOnly} is {@code true}; otherwise
     *         the original response
     */
	private String renderTextOnlyIfRequested(boolean textOnly, String response) {
		if (!textOnly) {
			return response;
		}
		return new Source(response).getRenderer().setMaxLineLength(180).setNewLine(AbstractAIProvider.LINE_SEPARATOR)
				.toString();
	}

	/**
	 * Creates and configures an {@link HttpURLConnection}.
	 *
	 * <p>
	 * If the URI contains {@code userInfo}, it is removed from the request URI and
	 * used to set an HTTP Basic {@code Authorization} header.
	 * </p>
	 *
	 * @param uri     URI to connect to
	 * @param headers optional headers
	 * @param config
	 * @return connection
	 * @throws IOException if opening a connection fails
	 */
	HttpURLConnection getConnection(URI uri, Map<String, String> headers, Configurator config) throws IOException {
		URI cleanUri = uri;
		HttpURLConnection connection;

		String userInfo = uri.getUserInfo();
		if (userInfo != null) {
			cleanUri = URI.create(uri.toString().replace("//" + userInfo + "@", "//"));
			byte[] bytes = userInfo.getBytes(StandardCharsets.UTF_8);
			String basicToken = Base64.getEncoder().encodeToString(bytes);

			connection = (HttpURLConnection) cleanUri.toURL().openConnection();
			connection.setRequestProperty("Authorization", "Basic " + basicToken);
		} else {
			connection = (HttpURLConnection) cleanUri.toURL().openConnection();
		}

		fillHeader(headers, connection, config);
		return connection;
	}

	/**
	 * Performs the HTTP GET request and returns the response content.
	 *
	 * @param connection  open connection
	 * @param timeout     timeout in milliseconds
	 * @param charsetName charset used to decode the response
	 * @return response content including an initial status line
	 * @throws IOException if the request cannot be executed
	 */
	String getWebPage(HttpURLConnection connection, int timeout, String charsetName) throws IOException {
		StringBuilder output = new StringBuilder();

		connection.setRequestMethod("GET");
		if (timeout > 0) {
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
		}

		int responseCode = connection.getResponseCode();
		output.append("HTTP ").append(Integer.toString(responseCode)).append(" ")
				.append(connection.getResponseMessage()).append(AbstractAIProvider.LINE_SEPARATOR);

		try (InputStream in = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(charsetName)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append(AbstractAIProvider.LINE_SEPARATOR);
			}
		}

		return output.toString();
	}

	/**
	 * Executes a REST API call to the specified URL using the given HTTP method.
	 *
	 * <p>
	 * Supports userInfo format in the URL for basic authentication, custom headers,
	 * request body, timeout, and charset. Handles HTTP methods such as GET, POST, PUT,
	 * PATCH, DELETE, etc. If the URL contains user credentials (e.g., {@code https://user:password@host/path}),
	 * they are used for HTTP Basic authentication. Header values may include property placeholders
	 * resolved via the provided {@link Configurator}.
	 * </p>
	 *
	 * <p>
	 * The response includes an initial status line (e.g., {@code HTTP 200 OK}) followed by the response body.
	 * </p>
	 *
	 * @param url         The URL of the REST endpoint. Supports userInfo format (e.g., https://user:password@host/path) for basic authentication.
	 * @param method      The HTTP method to use (GET, POST, PUT, PATCH, DELETE, etc.). Default is GET.
	 * @param headers     Specifies HTTP header properties. If null, no additional headers are sent.
	 * @param body        The request body to send (for POST, PUT, PATCH, etc.).
	 * @param timeout     The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.
	 * @param charsetName The name of the character set to use when decoding the response content. Default: UTF-8.
	 * @param projectDir  The project directory context for file-based URLs.
	 * @param configurator The configuration object for property resolution and header placeholder substitution.
	 * @return The REST API response as a string, including the status line and response body, or an error message if the call fails.
	 */
	@Tool(name = "call_rest_api", description = "Executes a REST API call to the specified URL using the given HTTP method. The URL may include user credentials in "
			+ "the userInfo format (e.g., https://user:password@host/path) for basic authentication.")
	public String callRestApi(
			@Param(name = "url", description = "The URL of the REST endpoint. Supports userInfo format (e.g., https://user:password@host/path) for basic authentication.") String url,
			@Param(name = "method", description = "The HTTP method to use (GET, POST, PUT, PATCH, DELETE, etc.). Default is GET.", defaultValue = "") String method,
			@Param(name = "headers", description = "Specifies HTTP header properties. If null, no additional headers are sent.", defaultValue = Param.NULL) Map<String, String> headers,
			@Param(name = "body", description = "The request body to send (for POST, PUT, PATCH, etc.).", defaultValue = "") String body,
			@Param(name = "timeout", description = "The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.", defaultValue = "0") int timeout,
			@Param(name = "charset_name", description = "The name of the character set to use when decoding the response content. Default: "
					+ DEFAULT_CHARSET, defaultValue = DEFAULT_CHARSET) String charsetName,
			@Param(name = "project_dir", description = "The project dir.") File projectDir, Configurator configurator) {
		String requestId = Integer.toHexString(REQUEST_ID_RANDOM.nextInt());
		url = CommandFunctionTools.replace(url, configurator);

		try {
			HttpURLConnection connection = getConnection(requestId, url, charsetName, method, timeout, headers, body,
					configurator);

			int responseCode = connection.getResponseCode();
			StringBuilder response = new StringBuilder();
			response.append("HTTP ").append(responseCode).append(" ").append(connection.getResponseMessage())
					.append(AbstractAIProvider.LINE_SEPARATOR);

			String result = parseResult(requestId, charsetName, connection, responseCode, response);
			if (logger.isInfoEnabled()) {
				logger.info("[REST {}] Response: {}", requestId,
						StringUtils.abbreviate(result.replaceAll("\\R", " "), AbstractAIProvider.LOG_LINE_LENG));
			}
			return result;

		} catch (Exception e) {
			logger.error("[REST {}] IO error during REST call: {}", requestId, e.getMessage(), e);
			return "IO Error: " + e.getMessage();
		}
	}

	/**
	 * Reads the response stream and returns the full response text.
	 *
	 * @param requestId    correlation id used for logs
	 * @param charsetName  response decoding charset
	 * @param connection   open connection
	 * @param responseCode HTTP response code
	 * @param response     builder already containing the status line
	 * @return response text
	 * @throws IOException if reading the response fails
	 */
	private String parseResult(String requestId, String charsetName, HttpURLConnection connection, int responseCode,
			StringBuilder response) throws IOException {
		InputStream in = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
		if (in != null) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(charsetName)))) {
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line).append(AbstractAIProvider.LINE_SEPARATOR);
				}
			}

			String result = response.toString();
			if (logger.isInfoEnabled()) {
				logger.info("[REST {}] Received response ({} bytes): {}", requestId, response.length(),
						StringUtils.abbreviate(result.replaceAll("\\R", " "), AbstractAIProvider.LOG_LINE_LENG));
			}
			logger.debug("[REST {}] Received response ({} bytes): {}", requestId, response.length(), result);
			return result;
		}

		return "ResponseCode: " + connection.getResponseCode() + " " + connection.getRequestMethod();
	}

	private HttpURLConnection getConnection(String requestId, String url, String charsetName, String method,
			int timeout, Map<String, String> headers, String body, Configurator config)
			throws IOException {
		HttpURLConnection connection = getConnection(URI.create(url), headers, config);
		logger.info("[REST {}] URL: {}", requestId, connection.getURL());

		connection.setRequestMethod(method);

		if (timeout > 0) {
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
		}

		if (!body.isEmpty() && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
				|| "PATCH".equalsIgnoreCase(method))) {
			connection.setDoOutput(true);
			try (OutputStream os = connection.getOutputStream()) {
				os.write(body.getBytes(charsetName));
			}
		}
		return connection;
	}

	/**
	 * Applies HTTP headers to the given connection, resolving any property placeholders.
	 *
	 * <p>
	 * Each header entry is set as a request property on the {@link HttpURLConnection}.
	 * Header values may include placeholders in the form <code>${propertyName}</code>,
	 * which are resolved using the provided {@link Configurator}.
	 * </p>
	 *
	 * @param headers      Map of header names to values. If {@code null}, no headers are applied.
	 * @param connection   The {@link HttpURLConnection} to configure.
	 * @param configurator The {@link Configurator} used to resolve property placeholders in header values.
	 */
	void fillHeader(Map<String, String> headers, HttpURLConnection connection, Configurator configurator) {
		if (headers != null) {
			for (Entry<String, String> entry : headers.entrySet()) {
				String value = CommandFunctionTools.replace(entry.getValue(), configurator);
				connection.setRequestProperty(entry.getKey(), value);
			}
		}
	}

}
