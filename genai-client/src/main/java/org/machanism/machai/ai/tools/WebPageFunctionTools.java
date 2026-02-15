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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
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
	 * <b>${propertyName}</b>. At runtime, the value will be resolved using the
	 * {@link Configurator} instance provided to this class. If the property is not
	 * found, the original value is used.
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
	public void applyTools(GenAIProvider provider) {
		provider.addTool("get_web_content", "Fetches the content of a web page using an HTTP GET request.",
				this::getWebContent, "url:string:required:The URL of the web page to fetch.",
				"headers:string:optional:Specifies HTTP headers as a single string, with each header in the format NAME=VALUE, separated by newline characters (\\n). If null, no additional headers are sent.",
				"timeout:integer:optional:The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.",
				"charsetName:string:optional:The name of the character set to use when decoding the response content. Default: "
						+ defaultCharset,
				"textOnly:boolean:optional:If true, only the plain text content of the web page is returned (HTML tags are stripped). If false or not specified, the full HTML content is returned.");

		provider.addTool("call_rest_api", "Executes a REST API call to the specified URL using the given HTTP method.",
				this::callRestApi, "url:string:required:The URL of the REST endpoint.",
				"method:string:optional:The HTTP method to use (GET, POST, PUT, PATCH, DELETE, etc.). Default is GET.",
				"headers:string:optional:Specifies HTTP headers as a single string, with each header in the format NAME=VALUE, separated by newline characters (\\n). If null, no additional headers are sent.",
				"body:string:optional:The request body to send (for POST, PUT, PATCH, etc.).",
				"timeout:integer:optional:The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.",
				"charsetName:string:optional:The name of the character set to use when decoding the response content. Default: "
						+ defaultCharset);
	}

	/**
	 * Fetches the content of a web page using an HTTP GET request.
	 *
	 * @param params tool invocation parameters
	 * @return response content (HTML or text) or an error message
	 */
	private String getWebContent(Object[] params) {
		String requestId = Integer.toHexString(new Random().nextInt());
		logger.info("Fetching web content [{}]: {}", requestId, Arrays.toString(params));

		JsonNode props = (JsonNode) params[0];
		String url = props.get("url").asText();

		String headers = props.has("headers") ? props.get("headers").asText(null) : null;
		int timeout = props.has("timeout") ? props.get("timeout").asInt(TIMEOUT) : TIMEOUT;
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(defaultCharset)
				: defaultCharset;
		boolean textOnly = props.has("textOnly") && props.get("textOnly").asBoolean(false);

		try {
			String response = getWebPage(url, headers, timeout, charsetName);

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
	 * Performs the HTTP request and returns the response content.
	 *
	 * @param url         URL to fetch
	 * @param headers     optional headers as {@code NAME=VALUE} pairs separated by
	 *                    newlines
	 * @param timeout     timeout in milliseconds
	 * @param charsetName charset used to decode the response
	 * @return response content including an initial status line
	 * @throws IOException if the request cannot be executed
	 */
	private String getWebPage(String url, String headers, int timeout, String charsetName)
			throws IOException, MalformedURLException, ProtocolException, UnsupportedEncodingException {
		StringBuilder output = new StringBuilder();

		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		fillHeader(headers, connection);

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
	 * Executes a REST call to the specified URL using the given HTTP method.
	 *
	 * @param params tool invocation parameters (expects a JsonNode with keys: url,
	 *               method, headers, body, timeout, charsetName)
	 * @return response content (body as string) or an error message
	 */
	private String callRestApi(Object[] params) {
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
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod(method);
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);

			fillHeader(headers, connection);

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

			logger.info("[REST {}] Received response ({} bytes).", requestId, response.length());
			return response.toString();

		} catch (IOException e) {
			logger.error("[REST {}] IO error during REST call", requestId, e);
			return "IO Error: " + e.getMessage();
		}
	}

	private void fillHeader(String headers, HttpURLConnection connection) {
		// Set headers if provided
		if (headers != null) {
			for (String headerLine : headers.split("\\R")) {
				int idx = headerLine.indexOf('=');
				if (idx > 0) {
					String name = headerLine.substring(0, idx).trim();
					String value = headerLine.substring(idx + 1).trim();

					value = replase(value, configurator);
					connection.setRequestProperty(name, value);
				}
			}
		}
	}

	private String replase(String value, Configurator conf) {
		Properties properties = new Properties();

		Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			String propName = matcher.group(1);
			properties.put(propName, conf.get(propName, "${" + propName + "}"));
		}

		value = StringSubstitutor.replace(value, properties);
		return value;
	}

	@Override
	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
}
