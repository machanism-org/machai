package org.machanism.machai.ai.provider.codemie;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIAdapter;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.provider.claude.ClaudeProvider;
import org.machanism.machai.ai.provider.gemini.GeminiProvider;
import org.machanism.machai.ai.provider.openai.OpenAIProvider;

/**
 * GenAI provider implementation for EPAM CodeMie.
 *
 * <p>
 * This provider obtains an access token from a configurable OpenID Connect
 * token endpoint and then initializes an OpenAI-compatible client (via
 * {@link OpenAIProvider}) to call the CodeMie Code Assistant REST API.
 *
 * <h2>Authentication modes</h2>
 * <p>
 * The authentication mode is selected based on the configured username:
 * <ul>
 * <li>If the username contains {@code "@"}, the password grant is used (typical
 * user e-mail login).</li>
 * <li>Otherwise, the client credentials grant is used
 * (service-to-service).</li>
 * </ul>
 *
 * <h2>Delegation</h2>
 * <p>
 * After a token is retrieved, this provider configures the underlying
 * OpenAI-compatible provider by setting:
 * <ul>
 * <li>{@code OPENAI_BASE_URL} to {@link #baseUrl}</li>
 * <li>{@code OPENAI_API_KEY} to the retrieved access token</li>
 * </ul>
 * and then delegates requests to either {@link OpenAIProvider} (for
 * {@code gpt-*} models) or {@link GeminiProvider} (for {@code claude-*}
 * models).
 */
public class CodeMieProvider extends GenAIAdapter implements GenAIProvider {

	/**
	 * Default OpenID Connect token endpoint for CodeMie.
	 *
	 * <p>
	 * Can be overridden via the {@code AUTH_URL} configuration key.
	 */
	public static String authUrl = "https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token";

	/**
	 * Base URL for the CodeMie Code Assistant API.
	 *
	 * <p>
	 * This base URL is used to configure the underlying OpenAI-compatible client.
	 */
	public static String baseUrl = "https://codemie.lab.epam.com/code-assistant-api/v1";

	/**
	 * Initializes the provider using configuration values.
	 *
	 * <p>
	 * Required configuration keys:
	 * <ul>
	 * <li>{@code GENAI_USERNAME} – user e-mail or client id.</li>
	 * <li>{@code GENAI_PASSWORD} – password or client secret.</li>
	 * <li>{@code chatModel} – model identifier (for example {@code gpt-4o-mini} or
	 * {@code claude-3-5-sonnet}).</li>
	 * </ul>
	 *
	 * <p>
	 * Optional configuration keys:
	 * <ul>
	 * <li>{@code AUTH_URL} – token endpoint override.</li>
	 * </ul>
	 *
	 * @param conf configuration source
	 * @throws IllegalArgumentException if authorization fails or an unsupported
	 *                                  model is configured
	 */
	@Override
	public void init(Configurator conf) {
		String username = conf.get("GENAI_USERNAME");
		String password = conf.get("GENAI_PASSWORD");
		String authUrl = conf.get("AUTH_URL", CodeMieProvider.authUrl);
		String chatModel = conf.get("chatModel");

		try {
			String token = getToken(authUrl, username, password);

			if (System.getenv("OPENAI_API_KEY") != null) {
				throw new IllegalArgumentException(
						"Configuration conflict detected: Please unset the 'OPENAI_API_KEY' environment variable to avoid conflicts with the current configuration.");
			}

			conf.set("OPENAI_BASE_URL", baseUrl);
			conf.set("OPENAI_API_KEY", token);

			if (Strings.CS.startsWithAny(chatModel, "gpt-") || StringUtils.isBlank(chatModel)) {
				provider = new OpenAIProvider();
				setProvider(provider);
			} else if (Strings.CS.startsWithAny(chatModel, "gemini-")) {
				provider = new GeminiProvider();
				setProvider(provider);
			} else if (Strings.CS.startsWithAny(chatModel, "claude-")) {
				provider = new ClaudeProvider();
				setProvider(provider);
			} else {
				throw new IllegalArgumentException("Unsupported model: '" + chatModel + "'.");
			}

			super.init(conf);
		} catch (IOException e) {
			throw new IllegalArgumentException("Authorization failed for user '" + username + "'", e);
		}
	}

	/**
	 * Requests an OAuth 2.0 access token from the given token endpoint.
	 *
	 * <p>
	 * The request uses {@code application/x-www-form-urlencoded} and selects the
	 * grant type based on the {@code username} value:
	 * <ul>
	 * <li>{@code password} if the username contains {@code "@"}.</li>
	 * <li>{@code client_credentials} otherwise.</li>
	 * </ul>
	 *
	 * @param url      token endpoint URL
	 * @param username user e-mail (password grant) or client id (client
	 *                 credentials)
	 * @param password password (password grant) or client secret (client
	 *                 credentials)
	 * @return the {@code access_token} value extracted from the response
	 * @throws IOException if the HTTP request fails, returns a non-200 response, or
	 *                     the token cannot be read
	 */
	public static String getToken(String url, String username, String password) throws IOException {
		String queryTemplate;
		if (username.contains("@")) {
			queryTemplate = "grant_type=password&client_id=codemie-sdk&username=%s&password=%s";
		} else {
			queryTemplate = "grant_type=client_credentials&client_id=%s&client_secret=%s";
		}

		String urlParameters = String.format(queryTemplate, urlEncode(username), urlEncode(password));
		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

		URL obj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setDoOutput(true);

		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(postData);
		}

		int responseCode = conn.getResponseCode();
		if (responseCode == 200) {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String inputLine;
				StringBuilder response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}

				return StringUtils.substringBetween(response.toString(), "\"access_token\":\"", "\",");
			}
		}

		throw new IOException("Failed to obtain token: received HTTP response code " + responseCode);
	}

	/**
	 * URL-encodes a value for {@code application/x-www-form-urlencoded} request
	 * bodies.
	 *
	 * @param value value to encode
	 * @return encoded value using UTF-8
	 * @throws RuntimeException if UTF-8 is not supported (unexpected on a compliant
	 *                          JVM)
	 */
	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is always supported, but handle exception just in case
			throw new RuntimeException("UTF-8 encoding not supported", e);
		}
	}
}
