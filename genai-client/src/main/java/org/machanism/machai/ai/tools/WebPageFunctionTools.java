package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jsoup.Jsoup;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Installs HTTP retrieval tools into a {@link GenAIProvider}.
 *
 * <p>
 * This tool set provides two host-side functions:
 * </p>
 * <ul>
 *   <li>{@code get_web_content} – Fetches web page content over HTTP(S) via GET and optionally returns plain text
 *       or content selected via a CSS selector.</li>
 *   <li>{@code call_rest_api} – Executes a generic REST call using an arbitrary HTTP method with optional headers
 *       and request body.</li>
 * </ul>
 *
 * <h2>Header variable placeholders</h2>
 * <p>
 * Header values may include placeholders in the form ${propertyName}. When a {@link Configurator} is provided via
 * {@link #setConfigurator(Configurator)}, those placeholders are resolved at runtime.
 * </p>
 *
 * <h2>Authentication</h2>
 * <p>
 * HTTP Basic authentication is supported via the URL {@code userInfo} component (for example
 * {@code https://user:password@host/path}), which is converted into an {@code Authorization: Basic ...} header.
 * You can also specify an explicit {@code Authorization} header.
 * </p>
 *
 * <p>
 * Outbound network policy (allow/deny lists) is intentionally left to the host application.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class WebPageFunctionTools implements FunctionTools {

	private static final int TIMEOUT = 10000;

	/** Logger for web fetch tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(WebPageFunctionTools.class);

	/** Default character set for decoding responses and encoding request bodies. */
	private static final String defaultCharset = "UTF-8";

	/**
	 * Optional configuration source used to resolve ${...} placeholders in header values.
	 */
	private Configurator configurator;

	/**
	 * Registers web content and REST API function tools with the provided {@link GenAIProvider}.
	 *
	 * @param provider the provider to register tools with
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool("get_web_content",
				"Fetches the content of a web page using an HTTP GET request. The URL may include user credentials in the userInfo format (e.g., https://user:password@host/path) for basic authentication.",
				this::getWebContent,
				"url:string:required:The URL of the web page to fetch. Supports userInfo format (e.g., https://user:password@host/path) for basic authentication.",
				"headers:string:optional:Specifies HTTP headers as a single string, with each header in the format NAME=VALUE, separated by newline characters (\\n). If null, no additional headers are sent.",
				"timeout:integer:optional:The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.",
				"charsetName:string:optional:The name of the character set to use when decoding the response content. Default: "
						+ defaultCharset,
				"textOnly:boolean:optional:If true, only the plain text content of the web page is returned (HTML tags are stripped). If false or not specified, the full HTML content is returned.",
				"cssSelectorQuery:string:optional:If provided, extracts and returns only the content matching the specified CSS selector. If textOnly is also true, returns only the text of the selected elements; otherwise, returns their HTML.");

		provider.addTool("call_rest_api",
				"Executes a REST API call to the specified URL using the given HTTP method. The URL may include user credentials in the userInfo format (e.g., https://user:password@host/path) for basic authentication.",
				this::callRestApi,
				"url:string:required:The URL of the REST endpoint. Supports userInfo format (e.g., https://user:password@host/path) for basic authentication.",
				"method:string:optional:The HTTP method to use (GET, POST, PUT, PATCH, DELETE, etc.). Default is GET.",
				"headers:string:optional:Specifies HTTP headers as a single string, with each header in the format NAME=VALUE, separated by newline characters (\\n). If null, no additional headers are sent.",
				"body:string:optional:The request body to send (for POST, PUT, PATCH, etc.).",
				"timeout:integer:optional:The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.",
				"charsetName:string:optional:The name of the character set to use when decoding the response content. Default: "
						+ defaultCharset);
	}

	/**
	 * Implements {@code get_web_content} by retrieving web content via an HTTP GET request.
	 *
	 * <p>
	 * Parameters are passed in {@code params}:
	 * </p>
	 * <ol>
	 *   <li>{@link JsonNode} containing the tool arguments</li>
	 *   <li>(optional) additional runtime-supplied arguments, ignored by this tool</li>
	 * </ol>
	 *
	 * <p>
	 * Supported JSON properties:
	 * </p>
	 * <ul>
	 *   <li>{@code url} (required) – target URL</li>
	 *   <li>{@code headers} (optional) – newline-separated {@code NAME=VALUE} pairs</li>
	 *   <li>{@code timeout} (optional) – timeout in milliseconds (default {@value #TIMEOUT})</li>
	 *   <li>{@code charsetName} (optional) – response decoding charset (default {@code UTF-8})</li>
	 *   <li>{@code textOnly} (optional) – if {@code true}, strips HTML to plain text</li>
	 *   <li>{@code cssSelectorQuery} (optional) – extracts content matching the CSS selector (text or HTML depending
	 *       on {@code textOnly})</li>
	 * </ul>
	 *
	 * @param params tool arguments
	 * @return response content or an error message
	 */
	public String getWebContent(Object[] params) {
		String requestId = Integer.toHexString(new Random().nextInt());
		logger.info("Fetching web content [{}]: {}", requestId, Arrays.toString(params));

		JsonNode props = (JsonNode) params[0];
		String url = props.get("url").asText();

		String headers = props.has("headers") ? props.get("headers").asText(null) : null;
		int timeout = props.has("timeout") ? props.get("timeout").asInt(TIMEOUT) : TIMEOUT;
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(defaultCharset)
				: defaultCharset;
		boolean textOnly = props.has("textOnly") && props.get("textOnly").asBoolean(false);
		String cssSelectorQuery = props.has("cssSelectorQuery") ? props.get("cssSelectorQuery").asText(null) : null;

		try {
			HttpURLConnection connection = getConnection(new URL(url), headers, charsetName);
			logger.info("[WEB {}] URL: {}", requestId, connection.getURL());

			fillHeader(headers, connection);

			String response = getWebPage(connection, timeout, charsetName);

			if (cssSelectorQuery != null && !cssSelectorQuery.isEmpty()) {
				org.jsoup.nodes.Document doc = Jsoup.parse(response);
				org.jsoup.select.Elements elements = doc.select(cssSelectorQuery);
				StringBuilder selectedContent = new StringBuilder();
				for (org.jsoup.nodes.Element element : elements) {
					selectedContent.append(textOnly ? element.text() : element.outerHtml())
							.append(System.lineSeparator());
				}
				String result = selectedContent.toString().trim();
				logger.info("[WEB {}] Downloaded web content ({} bytes), CSS selector '{}' matched {} elements.",
						requestId, response.length(), cssSelectorQuery, elements.size());
				return result;
			}

			if (textOnly) {
				String plainText = Jsoup.parse(response).text();
				logger.info("[WEB {}] Downloaded web content ({} bytes, plain text: {} chars).", requestId,
						response.length(), plainText.length());
				return plainText;
			}

			logger.info("[WEB {}] Downloaded web content ({} bytes).", requestId, response.length());
			return response;

		} catch (IOException e) {
			logger.error("[WEB {}] IO error during web content fetch", requestId, e);
			return "IO Error: " + e.getMessage();
		}
	}

	/**
	 * Creates and configures an {@link HttpURLConnection}.
	 *
	 * <p>
	 * If the URL contains {@code userInfo}, it is removed from the URL and used to set an HTTP Basic
	 * {@code Authorization} header.
	 * </p>
	 *
	 * @param url         URL to connect to
	 * @param headers     optional headers (newline-separated {@code NAME=VALUE})
	 * @param charsetName charset name (currently unused, but kept for API symmetry)
	 * @return connection
	 * @throws IOException if opening a connection fails
	 */
	private HttpURLConnection getConnection(URL url, String headers, String charsetName) throws IOException {
		url = new URL(replace(url.toString(), configurator));
		try {
			HttpURLConnection connection;

			String userInfo = url.getUserInfo();
			if (userInfo != null) {
				String cleanUrl = url.toString().replace("//" + userInfo + "@", "//");

				url = new URL(cleanUrl);
				connection = (HttpURLConnection) url.openConnection();

				byte[] bytes = userInfo.getBytes(StandardCharsets.UTF_8);
				String basicToken = Base64.getEncoder().encodeToString(bytes);
				connection.setRequestProperty("Authorization", "Basic " + basicToken);

			} else {
				connection = (HttpURLConnection) url.openConnection();
			}

			fillHeader(headers, connection);
			return connection;

		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
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
	private String getWebPage(HttpURLConnection connection, int timeout, String charsetName)
			throws IOException, MalformedURLException, ProtocolException, UnsupportedEncodingException {
		StringBuilder output = new StringBuilder();

		connection.setRequestMethod("GET");
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		int responseCode = connection.getResponseCode();
		output.append("HTTP ").append(Integer.toString(responseCode)).append(" ")
				.append(connection.getResponseMessage()).append(System.lineSeparator());

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(), charsetName))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append(System.lineSeparator());
			}
		}

		return output.toString();
	}

	/**
	 * Implements {@code call_rest_api} by executing an HTTP request against the provided endpoint.
	 *
	 * <p>
	 * Supported JSON properties:
	 * </p>
	 * <ul>
	 *   <li>{@code url} (required) – endpoint URL</li>
	 *   <li>{@code method} (optional) – HTTP method (default {@code GET})</li>
	 *   <li>{@code headers} (optional) – newline-separated {@code NAME=VALUE} pairs</li>
	 *   <li>{@code body} (optional) – request body (used for POST/PUT/PATCH only)</li>
	 *   <li>{@code timeout} (optional) – timeout in milliseconds (default {@value #TIMEOUT})</li>
	 *   <li>{@code charsetName} (optional) – request/response charset (default {@code UTF-8})</li>
	 * </ul>
	 *
	 * @param params tool arguments
	 * @return response content including an initial HTTP status line, or an error message
	 */
	public String callRestApi(Object[] params) {
		String requestId = Integer.toHexString(new Random().nextInt());
		logger.info("Executing REST call [{}]: {}", requestId, Arrays.toString(params));

		JsonNode props = (JsonNode) params[0];
		String url = props.get("url").asText();
		String method = props.has("method") ? props.get("method").asText("GET") : "GET";
		String headers = props.has("headers") ? props.get("headers").asText(null) : null;
		String body = props.has("body") ? props.get("body").asText(null) : null;
		int timeout = props.has("timeout") ? props.get("timeout").asInt(TIMEOUT) : TIMEOUT;
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(defaultCharset)
				: defaultCharset;

		try {
			URL callUrl = new URL(url);
			HttpURLConnection connection = getConnection(callUrl, headers, charsetName);
			logger.info("[REST {}] URL: {}", requestId, connection.getURL());

			fillHeader(headers, connection);

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

			int responseCode = connection.getResponseCode();
			StringBuilder response = new StringBuilder();
			response.append("HTTP ").append(responseCode).append(" ").append(connection.getResponseMessage())
					.append(System.lineSeparator());

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(), charsetName))) {
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line).append(System.lineSeparator());
				}
			}

			String result = response.toString();
			logger.info("[REST {}] Received response ({} bytes): {}", requestId, response.length(),
					StringUtils.abbreviate(result.replaceAll("\\R", " "), 60));
			return result;

		} catch (IOException e) {
			logger.error("[REST {}] IO error during REST call: {}", requestId, e.getMessage());
			return "IO Error: " + e.getMessage();
		}
	}

	/**
	 * Applies headers to the connection.
	 *
	 * <p>
	 * Each header line must be in the form {@code NAME=VALUE}. Header values may include ${...} placeholders.
	 * </p>
	 *
	 * @param headers    newline-separated header definitions
	 * @param connection connection to configure
	 */
	private void fillHeader(String headers, HttpURLConnection connection) {
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
	 * Resolves ${...} placeholders using the provided configurator.
	 *
	 * @param value raw value that may contain placeholders
	 * @param conf  configurator used for lookup; if {@code null}, the value is returned unchanged
	 * @return resolved value
	 */
	private String replace(String value, Configurator conf) {
		if (value == null || conf == null) {
			return value;
		}

		Properties properties = new Properties();

		Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			String propName = matcher.group(1);
			String propValue = conf.get(propName);
			if (propValue != null) {
				properties.put(propName, propValue);
			}
		}

		return StringSubstitutor.replace(value, properties);
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
