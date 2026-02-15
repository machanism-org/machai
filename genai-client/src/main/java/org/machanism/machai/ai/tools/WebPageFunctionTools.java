package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class WebPageFunctionTools {

	private static final int TIMEOUT = 10000;

	/** Logger for shell tool execution and diagnostics. */
	private static final Logger logger = LoggerFactory.getLogger(WebPageFunctionTools.class);

	private static final String defaultCharset = "UTF-8";

	/**
	 * Registers the {@code get_web_content} functional tool with the provided
	 * {@link GenAIProvider}.
	 * <p>
	 * This tool allows fetching the content of a web page using an HTTP GET
	 * request. The following parameters are supported:
	 * <ul>
	 * <li><b>url</b> (string, required): The URL of the web page to fetch.</li>
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
	 * content of the web page is returned (HTML tags are stripped using jsoup). If
	 * {@code false} or not specified, the full HTML content is returned.</li>
	 * </ul>
	 * <p>
	 * The tool is registered with the provider and can be invoked to retrieve web
	 * content as part of GenAI-powered workflows.
	 *
	 * @param provider the {@link GenAIProvider} instance to which the tool will be
	 *                 registered
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool("get_web_content", "Fetches the content of a web page using an HTTP GET request.",
				this::getWebContent, "url:string:required:The URL of the web page to fetch.",
				"headers:string:optional:Specifies HTTP headers as a single string, with each header in the format NAME=VALUE, separated by newline characters (\\n). If null, no additional headers are sent.",
				"timeout:integer:optional:The maximum time in milliseconds to wait for the HTTP response. If not specified, a default timeout will be used.",
				"charsetName:string:optional:The name of the character set to use when decoding the response content. Default: "
						+ defaultCharset,
				"textOnly:boolean:optional:If true, only the plain text content of the web page is returned (HTML tags are stripped). If false or not specified, the full HTML content is returned.");
	}

	/**
	 * Fetches the content of a web page using an HTTP GET request, with support for
	 * custom headers, timeout, character set, and an option to return only the
	 * plain text content.
	 * <p>
	 * The method expects the first element of {@code params} to be a
	 * {@link JsonNode} containing the following properties:
	 * <ul>
	 * <li><b>url</b> (string, required): The URL of the web page to fetch.</li>
	 * <li><b>headers</b> (string, optional): HTTP headers as a single string, with
	 * each header in the format {@code NAME=VALUE}, separated by newline characters
	 * ({@code \n}).</li>
	 * <li><b>timeout</b> (integer, optional): The maximum time in milliseconds to
	 * wait for the HTTP response. Defaults to 10,000 ms (10 seconds) if not
	 * specified.</li>
	 * <li><b>charsetName</b> (string, optional): The name of the character set to
	 * use when decoding the response content. Defaults to the value of
	 * {@code defaultCharset}.</li>
	 * <li><b>textOnly</b> (boolean, optional): If {@code true}, only the plain text
	 * content of the web page is returned (HTML tags are stripped using jsoup). If
	 * {@code false} or not specified, the full HTML content is returned.</li>
	 * </ul>
	 * <p>
	 * The method logs the download operation and returns the requested content as a
	 * string. If an error occurs, an error message is returned.
	 *
	 * @param params an array where the first element is a {@link JsonNode}
	 *               containing request parameters
	 * @return the fetched web content as a string (plain text or HTML), or an error
	 *         message if the request fails
	 */
	private String getWebContent(Object[] params) {
		String requestId = Integer.toHexString(new Random().nextInt());
		logger.info("Fetching web content [{}]: {}", requestId, Arrays.toString(params));

		JsonNode props = (JsonNode) params[0];
		String url = props.get("url").asText();

		String headers = props.has("headers") ? props.get("headers").asText(null) : null;
		int timeout = props.has("timeout") ? props.get("timeout").asInt(TIMEOUT) : 10000;
		String charsetName = props.has("charsetName") ? props.get("charsetName").asText(defaultCharset)
				: defaultCharset;
		boolean textOnly = props.has("textOnly") && props.get("textOnly").asBoolean(false);

		try {
			String response = getWebPage(url, headers, timeout, charsetName);

			if (textOnly) {
				// Extract only plain text using jsoup
				String plainText = Jsoup.parse(response).text();
				logger.info("[WEB {}] Downloaded web content ({} bytes, plain text: {} chars).", requestId,
						response.length(), plainText.length());
				return plainText;
			} else {
				logger.info("[WEB {}] Downloaded web content ({} bytes).", requestId, response.length());
				return response;
			}

		} catch (IOException e) {
			logger.error("[WEB {}] IO error during web content fetch", requestId, e);
			return "IO Error: " + e.getMessage();
		}
	}

	private String getWebPage(String url, String headers, int timeout, String charsetName)
			throws IOException, MalformedURLException, ProtocolException, UnsupportedEncodingException {
		StringBuilder output = new StringBuilder();

		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("GET");
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

}
