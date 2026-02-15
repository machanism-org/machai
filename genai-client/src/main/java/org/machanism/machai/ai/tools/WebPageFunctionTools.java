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
import java.util.Random;

import org.jsoup.Jsoup;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Installs an HTTP GET web-page fetching tool into a {@link GenAIProvider}.
 *
 * <p>
 * The installed tool retrieves the response body for a given URL and optionally
 * strips HTML tags to return plain text.
 *
 * <h2>Installed tool</h2>
 * <ul>
 * <li>{@code get_web_content} – performs an HTTP GET and returns either the
 * response body or extracted text</li>
 * </ul>
 *
 * <p>
 * This class does not enforce outbound network policies (for example allow/deny
 * lists). Such controls must be implemented by the hosting application.
 *
 * @author Viktor Tovstyi
 */
public class WebPageFunctionTools implements FunctionTools {

	private static final int TIMEOUT = 10000;

	/** Logger for web fetch tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(WebPageFunctionTools.class);

	private static final String defaultCharset = "UTF-8";

	/**
	 * Registers the {@code get_web_content} tool with the provided
	 * {@link GenAIProvider}.
	 *
	 * <p>
	 * Supported parameters:
	 * <ul>
	 * <li><b>url</b> (string, required): the URL to fetch</li>
	 * <li><b>headers</b> (string, optional): HTTP headers as {@code NAME=VALUE}
	 * pairs separated by newlines ({@code \n}); if omitted, no extra headers are
	 * sent</li>
	 * <li><b>timeout</b> (integer, optional): request timeout in milliseconds;
	 * defaults to {@value #TIMEOUT}</li>
	 * <li><b>charsetName</b> (string, optional): character set to decode the
	 * response with; defaults to {@code UTF-8}</li>
	 * <li><b>textOnly</b> (boolean, optional): if {@code true}, returns the page
	 * text content (HTML stripped via jsoup); otherwise returns the full response
	 * content</li>
	 * </ul>
	 *
	 * @param provider provider instance to augment
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

		if (headers != null) {
			for (String headerLine : headers.split("\\R")) {
				int idx = headerLine.indexOf('=');
				if (idx > 0) {
					String name = headerLine.substring(0, idx).trim();
					String value = headerLine.substring(idx + 1).trim();
					connection.setRequestProperty(name, value);
				}
			}
		}

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

			// Set headers if provided
			if (headers != null) {
				for (String headerLine : headers.split("\\R")) {
					int idx = headerLine.indexOf('=');
					if (idx > 0) {
						String name = headerLine.substring(0, idx).trim();
						String value = headerLine.substring(idx + 1).trim();
						connection.setRequestProperty(name, value);
					}
				}
			}

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

}
