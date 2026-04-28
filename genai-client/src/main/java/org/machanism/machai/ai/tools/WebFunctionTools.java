package org.machanism.machai.ai.tools;

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
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jsoup.Jsoup;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import net.htmlparser.jericho.Source;

/**
 * Installs HTTP retrieval tools into a {@link Genai}.
 *
 * <p>
 * This tool set provides two host-side functions:
 * </p>
 * <ul>
 * <li>{@code get_web_content} – Fetches web page content over HTTP(S) via GET
 * and optionally returns plain text or content selected via a CSS
 * selector.</li>
 * <li>{@code call_rest_api} – Executes a generic REST call using an arbitrary
 * HTTP method with optional headers and request body.</li>
 * </ul>
 *
 * <h2>Header variable placeholders</h2>
 * <p>
 * Header values may include placeholders in the form ${propertyName}. When a
 * {@link Configurator} is provided via {@link #setConfigurator(Configurator)},
 * those placeholders are resolved at runtime.
 * </p>
 *
 * <h2>Authentication</h2>
 * <p>
 * HTTP Basic authentication is supported via the URL {@code userInfo} component
 * (for example {@code https://user:password@host/path}), which is converted
 * into an {@code Authorization: Basic ...} header. You can also specify an
 * explicit {@code Authorization} header.
 * </p>
 *
 * <p>
 * Outbound network policy (allow/deny lists) is intentionally left to the host
 * application.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class WebFunctionTools implements FunctionTools {

	private static final int TIMEOUT = 10000;

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final String HEADERS_FIELD = "headers";
	private static final String TIMEOUT_FIELD = "timeout";
	private static final String CHARSET_NAME_FIELD = "charsetName";
	private static final String TEXT_ONLY_FIELD = "textOnly";
	private static final String SELECTOR_FIELD = "selector";

	private static final SecureRandom REQUEST_ID_RANDOM = new SecureRandom();

	/** Logger for web fetch tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(WebFunctionTools.class);

	/**
	 * Optional configuration source used to resolve ${...} placeholders in header
	 * values.
	 */
	private Configurator configurator;

	/**
	 * Registers web content and REST API function tools with the provided
	 * {@link Genai}.
	 *
	 * @param provider the provider to register tools with
	 */
	public void applyTools(Genai provider) {
		provider.addTool("get_web_content",
				"Fetches the content of a web page using an HTTP GET request. The URL may include user credentials in the userInfo format (e.g., https://user:password@host/path) for basic authentication.",
				this::getWebContent,
				"url:string:required:The URL of the web page to fetch. Supports userInfo format (e.g., https://user:password@host/path) for basic authentication.",
				"headers:string:optional:Specifies HTTP headers as a single string, with each header in the format NAME=VALUE, separated by newline characters (\\n). If null, no additional headers are sent.",
				"timeout:integer:optional:The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.",
				"charsetName:string:optional:The name of the character set to use when decoding the response content. Default: "
						+ DEFAULT_CHARSET,
				"textOnly:boolean:optional:If true, only the plain text content of the web page is returned (HTML tags are stripped). If false or not specified, the full HTML content is returned.",
				"selector:string:optional:If provided, extracts and returns only the content matching the specified CSS selector. If textOnly is also true, returns only the text of the selected elements; otherwise, returns their HTML.");

		provider.addTool("call_rest_api",
				"Executes a REST API call to the specified URL using the given HTTP method. The URL may include user credentials in the userInfo format (e.g., https://user:password@host/path) for basic authentication.",
				this::callRestApi,
				"url:string:required:The URL of the REST endpoint. Supports userInfo format (e.g., https://user:password@host/path) for basic authentication.",
				"method:string:optional:The HTTP method to use (GET, POST, PUT, PATCH, DELETE, etc.). Default is GET.",
				"headers:string:optional:Specifies HTTP headers as a single string, with each header in the format NAME=VALUE, separated by newline characters (\\n). If null, no additional headers are sent.",
				"body:string:optional:The request body to send (for POST, PUT, PATCH, etc.).",
				"timeout:integer:optional:The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.",
				"charsetName:string:optional:The name of the character set to use when decoding the response content. Default: "
						+ DEFAULT_CHARSET);
	}

	/**
	 * Implements {@code get_web_content} by retrieving web content via an HTTP GET
	 * request.
	 *
	 * <p>
	 * Parameters are passed in {@code params}:
	 * </p>
	 * <ol>
	 * <li>{@link JsonNode} containing the tool arguments</li>
	 * <li>(optional) additional runtime-supplied arguments, ignored by this
	 * tool</li>
	 * </ol>
	 *
	 * <p>
	 * Supported JSON properties:
	 * </p>
	 * <ul>
	 * <li>{@code url} (required) – target URL</li>
	 * <li>{@code headers} (optional) – newline-separated {@code NAME=VALUE}
	 * pairs</li>
	 * <li>{@code timeout} (optional) – timeout in milliseconds (default
	 * {@value #TIMEOUT})</li>
	 * <li>{@code charsetName} (optional) – response decoding charset (default
	 * {@code UTF-8})</li>
	 * <li>{@code textOnly} (optional) – if {@code true}, strips HTML to plain
	 * text</li>
	 * <li>{@code selector} (optional) – extracts content matching the CSS selector
	 * (text or HTML depending on {@code textOnly})</li>
	 * </ul>
	 *
	 * @param params tool arguments
	 * @return response content or an error message
	 */
	public String getWebContent(Object[] params) {
		String requestId = Integer.toHexString(REQUEST_ID_RANDOM.nextInt());
		if (logger.isInfoEnabled()) {
			logger.info("Fetching web content [{}]: {}", requestId, Arrays.toString(params));
		}

		JsonNode props = (JsonNode) params[0];
		String url = props.get("url").asText();

		url = replace(url, configurator);

		String headers = props.has(HEADERS_FIELD) ? props.get(HEADERS_FIELD).asText(null) : null;
		int timeout = props.has(TIMEOUT_FIELD) ? props.get(TIMEOUT_FIELD).asInt(TIMEOUT) : TIMEOUT;
		String charsetName = props.has(CHARSET_NAME_FIELD) ? props.get(CHARSET_NAME_FIELD).asText(DEFAULT_CHARSET)
				: DEFAULT_CHARSET;
		boolean textOnly = props.has(TEXT_ONLY_FIELD) && props.get(TEXT_ONLY_FIELD).asBoolean(false);
		String selector = props.has(SELECTOR_FIELD) ? props.get(SELECTOR_FIELD).asText(null) : null;

		try {
			URI uri = URI.create(url);
			String response;
			if ("file".equals(uri.getScheme())) {
				response = readFileUriContent(params, charsetName, uri);
			} else {
				response = fetchHttpContent(requestId, selector, headers, timeout, charsetName, textOnly, uri);
			}

			if (logger.isInfoEnabled()) {
				logger.info("[WEB {}] Downloaded web content ({} bytes): {}.", requestId, response.length(),
						StringUtils.abbreviate(response, 80).replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""));
			}
			return response;

		} catch (Exception e) {
			logger.error("[WEB {}] IO error during web content fetch", requestId, e);
			return "IO Error: " + e.getMessage();
		}
	}

	private String readFileUriContent(Object[] params, String charsetName, URI uri) {
		File workingDir = (File) params[1];
		String path = uri.getPath();
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(workingDir, path);
		}
		return readFileContent(file, charsetName);
	}

	private String fetchHttpContent(String requestId, String selector, String headers, int timeout, String charsetName,
			boolean textOnly, URI uri) throws IOException {
		HttpURLConnection connection = getConnection(uri, headers);
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
			selectedContent.append(element.outerHtml()).append(Genai.LINE_SEPARATOR);
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
		return new Source(response).getRenderer().setMaxLineLength(180).setNewLine(Genai.LINE_SEPARATOR).toString();
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
	 * @param headers optional headers (newline-separated {@code NAME=VALUE})
	 * @return connection
	 * @throws IOException if opening a connection fails
	 */
	HttpURLConnection getConnection(URI uri, String headers) throws IOException {
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

		fillHeader(headers, connection);
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
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		int responseCode = connection.getResponseCode();
		output.append("HTTP ").append(Integer.toString(responseCode)).append(" ")
				.append(connection.getResponseMessage()).append(Genai.LINE_SEPARATOR);

		try (InputStream in = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(charsetName)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append(Genai.LINE_SEPARATOR);
			}
		}

		return output.toString();
	}

	/**
	 * Implements {@code call_rest_api} by executing an HTTP request against the
	 * provided endpoint.
	 *
	 * <p>
	 * Supported JSON properties:
	 * </p>
	 * <ul>
	 * <li>{@code url} (required) – endpoint URL</li>
	 * <li>{@code method} (optional) – HTTP method (default {@code GET})</li>
	 * <li>{@code headers} (optional) – newline-separated {@code NAME=VALUE}
	 * pairs</li>
	 * <li>{@code body} (optional) – request body (used for POST/PUT/PATCH
	 * only)</li>
	 * <li>{@code timeout} (optional) – timeout in milliseconds (default
	 * {@value #TIMEOUT})</li>
	 * <li>{@code charsetName} (optional) – request/response charset (default
	 * {@code UTF-8})</li>
	 * </ul>
	 *
	 * @param params tool arguments
	 * @return response content including an initial HTTP status line, or an error
	 *         message
	 */
	public String callRestApi(Object[] params) {
		String requestId = Integer.toHexString(REQUEST_ID_RANDOM.nextInt());
		if (logger.isInfoEnabled()) {
			logger.info("Executing REST call [{}]: {}", requestId, Arrays.toString(params));
		}

		JsonNode props = (JsonNode) params[0];
		String url = props.get("url").asText();

		url = replace(url, configurator);

		try {
			String charsetName = props.has(CHARSET_NAME_FIELD) ? props.get(CHARSET_NAME_FIELD).asText(DEFAULT_CHARSET)
					: DEFAULT_CHARSET;

			HttpURLConnection connection = getConnection(requestId, props, url, charsetName);

			int responseCode = connection.getResponseCode();
			StringBuilder response = new StringBuilder();
			response.append("HTTP ").append(responseCode).append(" ").append(connection.getResponseMessage())
					.append(Genai.LINE_SEPARATOR);

			return parseResult(requestId, charsetName, connection, responseCode, response);

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
					response.append(line).append(Genai.LINE_SEPARATOR);
				}
			}

			String result = response.toString();
			if (logger.isInfoEnabled()) {
				logger.info("[REST {}] Received response ({} bytes): {}", requestId, response.length(),
						StringUtils.abbreviate(result.replaceAll("\\R", " "), 120));
			}
			logger.debug("[REST {}] Received response ({} bytes): {}", requestId, response.length(), result);
			return result;
		}

		return "ResponseCode: " + connection.getResponseCode() + " " + connection.getRequestMethod();
	}

	private HttpURLConnection getConnection(String requestId, JsonNode props, String url, String charsetName)
			throws IOException {
		String method = props.has("method") ? props.get("method").asText("GET") : "GET";
		String headers = props.has(HEADERS_FIELD) ? props.get(HEADERS_FIELD).asText(null) : null;
		String body = props.has("body") ? props.get("body").asText(null) : null;
		int timeout = props.has(TIMEOUT_FIELD) ? props.get(TIMEOUT_FIELD).asInt(TIMEOUT) : TIMEOUT;

		HttpURLConnection connection = getConnection(URI.create(url), headers);
		logger.info("[REST {}] URL: {}", requestId, connection.getURL());

		connection.setRequestMethod(method);
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		if (body != null && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
				|| "PATCH".equalsIgnoreCase(method))) {
			connection.setDoOutput(true);
			try (OutputStream os = connection.getOutputStream()) {
				os.write(body.getBytes(charsetName));
			}
		}
		return connection;
	}

	/**
	 * Applies headers to the connection.
	 *
	 * <p>
	 * Each header line must be in the form {@code NAME=VALUE}. Header values may
	 * include ${...} placeholders.
	 * </p>
	 *
	 * @param headers    newline-separated header definitions
	 * @param connection connection to configure
	 */
	void fillHeader(String headers, HttpURLConnection connection) {
		if (headers != null) {
			for (String headerLine : headers.split("\\R")) {
				int idx = headerLine.indexOf('=');
				if (idx > 0) {
					String name = headerLine.substring(0, idx).trim();
					String value = headerLine.substring(idx + 1).trim();

					value = replace(value, configurator);
					connection.setRequestProperty(name, value);
				}
			}
		}
	}

	/**
	 * Supplies configuration for resolving header placeholders.
	 *
	 * @param configurator configurator to use (may be {@code null})
	 */
	@Override
	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
}
