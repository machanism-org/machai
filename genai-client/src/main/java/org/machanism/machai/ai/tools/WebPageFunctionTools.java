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
 * Provides tools for fetching web content and executing REST API calls, and
 * installs them into a {@link GenAIProvider} for use in GenAI-powered
 * workflows.
 *
 * <p>
 * The main tools installed are:
 * <ul>
 * <li>{@code get_web_content} – Fetches the content of a web page using an HTTP
 * GET request. Supports returning either the full HTML content or plain text
 * (HTML tags stripped).</li>
 * <li>{@code call_rest_api} – Executes a REST API call to the specified URL
 * using the given HTTP method (GET, POST, PUT, PATCH, DELETE, etc.), with
 * support for custom headers and request body.</li>
 * </ul>
 *
 * <h2>HTTP Header Variable Placeholders</h2>
 * <p>
 * When specifying HTTP headers for either tool, you can use variable
 * placeholders in the header value. Placeholders are written in the format
 * <b>{@code ${propertyName}}</b>. At runtime, the value will be resolved using
 * the {@link Configurator} instance provided to this class. If the property is
 * not found, the original value is used.
 * </p>
 * <p>
 * Example header string:
 * 
 * <pre>
 * Authorization=Bearer ${apiToken}
 * Content-Type=application/json
 * </pre>
 * <ul>
 * <li>{@code Authorization} header will be set to {@code Bearer <value>} where
 * {@code <value>} is resolved from {@code apiToken} in the configurator.</li>
 * <li>{@code Content-Type} header will be set to {@code application/json}.</li>
 * </ul>
 *
 * <h2>Network Policy</h2>
 * <p>
 * This class does not enforce outbound network policies (such as allow/deny
 * lists). Such controls must be implemented by the hosting application.
 * </p>
 *
 * <h2>Usage</h2>
 * <ol>
 * <li>Instantiate and configure {@code WebPageFunctionTools} with a
 * {@link Configurator} if variable resolution is needed.</li>
 * <li>Call {@link #applyTools(GenAIProvider)} to register the tools.</li>
 * <li>Invoke the tools via the provider, passing parameters as described in the
 * tool documentation.</li>
 * </ol>
 *
 * @author Viktor Tovstyi
 */
public class WebPageFunctionTools implements FunctionTools {

	private static final int TIMEOUT = 10000;

	/** Logger for web fetch tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(WebPageFunctionTools.class);

	private static final String defaultCharset = "UTF-8";

	private Configurator configurator;

	/**
	 * Registers web content and REST API functional tools with the provided
	 * {@link GenAIProvider}.
	 * <p>
	 * The following tools are installed:
	 * <ul>
	 * <li><b>get_web_content</b> – Fetches the content of a web page using an HTTP
	 * GET request. <br>
	 * The URL may include user credentials in the userInfo format (e.g.,
	 * {@code https://user:password@host/path}) for basic authentication. <br>
	 * <b>Parameters:</b>
	 * <ul>
	 * <li><b>url</b> (string, required): The URL of the web page to fetch. Supports
	 * userInfo format for basic authentication.</li>
	 * <li><b>headers</b> (string, optional): HTTP headers as a single string, with
	 * each header in the format {@code NAME=VALUE}, separated by newline characters
	 * ({@code \n}). If {@code null}, no additional headers are sent.</li>
	 * <li><b>timeout</b> (integer, optional): The maximum time in milliseconds to
	 * wait for the HTTP response. If not specified, a default timeout will be
	 * used.</li>
	 * <li><b>charsetName</b> (string, optional): The name of the character set to
	 * use when decoding the response content. Defaults to the value of
	 * {@code defaultCharset}.</li>
	 * <li><b>textOnly</b> (boolean, optional): If {@code true}, only the plain text
	 * content of the web page is returned (HTML tags are stripped). If
	 * {@code false} or not specified, the full HTML content is returned.</li>
	 * <li><b>cssSelectorQuery</b> (string, optional): If provided, extracts and
	 * returns only the content matching the specified CSS selector. If
	 * {@code textOnly} is also {@code true}, returns only the text of the selected
	 * elements; otherwise, returns their HTML.</li>
	 * </ul>
	 * </li>
	 * <li><b>call_rest_api</b> – Executes a REST API call to the specified URL
	 * using the given HTTP method. <br>
	 * The URL may include user credentials in the userInfo format (e.g.,
	 * {@code https://user:password@host/path}) for basic authentication. <br>
	 * <b>Parameters:</b>
	 * <ul>
	 * <li><b>url</b> (string, required): The URL of the REST endpoint. Supports
	 * userInfo format for basic authentication.</li>
	 * <li><b>method</b> (string, optional): The HTTP method to use (GET, POST, PUT,
	 * PATCH, DELETE, etc.). Default is GET.</li>
	 * <li><b>headers</b> (string, optional): HTTP headers as a single string, with
	 * each header in the format {@code NAME=VALUE}, separated by newline characters
	 * ({@code \n}). If {@code null}, no additional headers are sent.</li>
	 * <li><b>body</b> (string, optional): The request body to send (for POST, PUT,
	 * PATCH, etc.).</li>
	 * <li><b>timeout</b> (integer, optional): The maximum time in milliseconds to
	 * wait for the HTTP response. If not specified, a default timeout will be
	 * used.</li>
	 * <li><b>charsetName</b> (string, optional): The name of the character set to
	 * use when decoding the response content. Defaults to the value of
	 * {@code defaultCharset}.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <p>
	 * Both tools support variable placeholders in header values (e.g.,
	 * ${propertyName}), which are resolved using the {@link Configurator} if
	 * available. <br>
	 * For HTTP Basic Authentication, you may either use the userInfo format in the
	 * URL or set the {@code Authorization} header directly.
	 *
	 * @param provider the {@link GenAIProvider} instance to which the tools will be
	 *                 registered
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
	 * Fetches the content of a web page using an HTTP GET request, with support for
	 * custom headers, timeout, character set, plain text extraction, and CSS
	 * selector-based content extraction.
	 * <p>
	 * This method allows for flexible web content retrieval and post-processing. It
	 * supports extracting only the plain text from the page, or extracting and
	 * returning only the content matching a specified CSS selector.
	 * <p>
	 * The method expects the first element of {@code params} to be a
	 * {@link JsonNode} containing the following properties:
	 * <ul>
	 * <li><b>url</b> (string, required): The URL of the web page to fetch.</li>
	 * <li><b>headers</b> (string, optional): HTTP headers as a single string, with
	 * each header in the format {@code NAME=VALUE}, separated by newline characters
	 * ({@code \n}). If {@code null}, no additional headers are sent. <br>
	 * Supports variable placeholders in the format {@code ${propertyName}}, which
	 * are resolved using the {@link Configurator} if available. <br>
	 * To use HTTP Basic Authentication, include an {@code Authorization} header
	 * with the value {@code Basic <base64-encoded-credentials>}.</li>
	 * <li><b>timeout</b> (integer, optional): The maximum time in milliseconds to
	 * wait for the HTTP response. Defaults to the class constant {@code TIMEOUT} if
	 * not specified.</li>
	 * <li><b>charsetName</b> (string, optional): The name of the character set to
	 * use when decoding the response content. Defaults to the class constant
	 * {@code defaultCharset}.</li>
	 * <li><b>textOnly</b> (boolean, optional): If {@code true}, only the plain text
	 * content of the web page is returned (HTML tags are stripped using jsoup). If
	 * {@code false} or not specified, the full HTML content is returned.</li>
	 * <li><b>cssSelectorQuery</b> (string, optional): If provided, extracts and
	 * returns only the content matching the specified CSS selector. If
	 * {@code textOnly} is also {@code true}, returns only the text of the selected
	 * elements; otherwise, returns their HTML.</li>
	 * </ul>
	 * <p>
	 * <b>Behavior:</b>
	 * <ul>
	 * <li>If {@code cssSelectorQuery} is provided and not empty, the method uses
	 * jsoup to select matching elements from the HTML response. The returned
	 * content is either the text or HTML of those elements, depending on
	 * {@code textOnly}.</li>
	 * <li>If {@code cssSelectorQuery} is not provided but {@code textOnly} is
	 * {@code true}, the method returns the plain text of the entire page.</li>
	 * <li>If neither {@code cssSelectorQuery} nor {@code textOnly} is set, the
	 * method returns the full HTML response.</li>
	 * </ul>
	 * <p>
	 * The method logs the request and response details, and returns the requested
	 * content as a string. If an error occurs, an error message is returned.
	 *
	 * @param params an array where the first element is a {@link JsonNode}
	 *               containing the web content fetch parameters
	 * @return the fetched web content as a string (plain text, HTML, or selected
	 *         content), or an error message if the request fails
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
	 * Performs the HTTP request and returns the response content.
	 * 
	 * @param connection
	 * @param headers     optional headers as {@code NAME=VALUE} pairs separated by
	 *                    newlines
	 * @param timeout     timeout in milliseconds
	 * @param charsetName charset used to decode the response
	 * @param url         URL to fetch
	 *
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
	 * Executes a REST API call to the specified URL using the provided HTTP method,
	 * headers, request body, and other options.
	 * <p>
	 * This method supports a wide range of REST operations (GET, POST, PUT, PATCH,
	 * DELETE, etc.) and allows for custom configuration of headers, timeouts,
	 * character encoding, and request body. It also supports HTTP Basic
	 * Authentication via headers.
	 * <p>
	 * The method expects the first element of {@code params} to be a
	 * {@link JsonNode} containing the following properties:
	 * <ul>
	 * <li><b>url</b> (string, required): The URL of the REST endpoint to call.</li>
	 * <li><b>method</b> (string, optional): The HTTP method to use (e.g., GET,
	 * POST, PUT, PATCH, DELETE). Defaults to GET if not specified.</li>
	 * <li><b>headers</b> (string, optional): HTTP headers as a single string, with
	 * each header in the format {@code NAME=VALUE}, separated by newline characters
	 * ({@code \n}). If {@code null}, no additional headers are sent. <br>
	 * Supports variable placeholders in the format {@code ${propertyName}}, which
	 * are resolved using the {@link Configurator} if available. <br>
	 * To use HTTP Basic Authentication, include an {@code Authorization} header
	 * with the value {@code Basic <base64-encoded-credentials>}.</li>
	 * <li><b>body</b> (string, optional): The request body to send (for POST, PUT,
	 * PATCH, etc.). Ignored for GET and DELETE requests.</li>
	 * <li><b>timeout</b> (integer, optional): The maximum time in milliseconds to
	 * wait for the HTTP response. Defaults to the class constant {@code TIMEOUT} if
	 * not specified.</li>
	 * <li><b>charsetName</b> (string, optional): The name of the character set to
	 * use when encoding the request body and decoding the response content.
	 * Defaults to the class constant {@code defaultCharset}.</li>
	 * </ul>
	 * <p>
	 * <b>Example usage:</b>
	 * 
	 * <pre>
	 * {
	 *   "url": "https://api.example.com/resource",
	 *   "method": "POST",
	 *   "headers": "Authorization=Basic dXNlcjpwYXNz\nContent-Type=application/json",
	 *   "body": "{\"key\":\"value\"}",
	 *   "timeout": 5000,
	 *   "charsetName": "UTF-8"
	 * }
	 * </pre>
	 * <p>
	 * The method logs the request and response details, and returns the full HTTP
	 * response as a string, including the status line. If an error occurs, an error
	 * message is returned.
	 *
	 * @param params an array where the first element is a {@link JsonNode}
	 *               containing the REST call parameters
	 * @return the HTTP response as a string (including status line and body), or an
	 *         error message if the request fails
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

			// Write body for POST, PUT, PATCH, etc.
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

	private String replace(String value, Configurator conf) {
		Properties properties = new Properties();

		Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			String propName = matcher.group(1);
			String propValue = conf.get(propName);
			properties.put(propName, propValue);
		}

		value = StringSubstitutor.replace(value, properties);
		return value;
	}

	@Override
	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
}
