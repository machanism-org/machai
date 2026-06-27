package org.machanism.machai.ai.provider.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.FallbackConfigurator;
import org.machanism.macha.core.commons.configurator.MutableConfigurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.provider.GenaiAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.client.AnthropicClient;
import com.openai.client.OpenAIClient;

/**
 * {@link Genai} implementation that integrates with EPAM CodeMie.
 *
 * <p>
 * This provider authenticates against a CodeMie OpenID Connect (OIDC) token
 * endpoint to obtain an OAuth 2.0 access token and then configures an
 * OpenAI-compatible backend (CodeMie Code Assistant REST API).
 * </p>
 *
 * <h2>Authentication modes</h2>
 * <ul>
 * <li><b>Password grant</b> is used when {@code GENAI_USERNAME} contains
 * {@code "@"} (typical e-mail login).</li>
 * <li><b>Client credentials</b> is used otherwise (service-to-service).</li>
 * </ul>
 *
 * <h2>Provider delegation</h2>
 * <p>
 * After retrieving a token, this provider sets the following configuration keys
 * before delegating to a downstream provider:
 * </p>
 * <ul>
 * <li>{@code OPENAI_BASE_URL} to {@link #BASE_URL}</li>
 * <li>{@code OPENAI_API_KEY} to the retrieved access token</li>
 * </ul>
 *
 * <p>
 * Delegation is selected based on the configured {@code model} prefix:
 * </p>
 * <ul>
 * <li>{@code gpt-*} (or blank/unspecified) models delegate to
 * {@link OpenAIProvider}</li>
 * <li>{@code claude-*} models delegate to {@link AnthropicProvider}</li>
 * </ul>
 */
public class CodeMieProvider extends GenaiAdapter implements EmbeddingProvider {

	private static final Logger logger = LoggerFactory.getLogger(CodeMieProvider.class);

	private final class ClaudeProviderExtension extends AnthropicProvider {
		private final MutableConfigurator conf;
		private final String username;
		private final String resolvedAuthUrl;
		private final String password;

		private ClaudeProviderExtension(Configurator conf, String username, String resolvedAuthUrl, String password) {
			this.conf = new FallbackConfigurator(conf);
			this.username = username;
			this.resolvedAuthUrl = resolvedAuthUrl;
			this.password = password;
		}

		@Override
		protected AnthropicClient getClient() {
			try {
				String token = getToken(resolvedAuthUrl, username, password);
				conf.set(AnthropicProvider.ANTHROPIC_API_KEY, token);
			} catch (IOException e) {
				throw new IllegalArgumentException("Authorization failed for user '" + username + "'", e);
			}

			return super.getClient();
		}
	}

	private final class OpenAIProviderExtension extends OpenAIProvider {
		private final MutableConfigurator conf;
		private final String username;
		private final String resolvedAuthUrl;
		private final String password;

		private OpenAIProviderExtension(MutableConfigurator conf, String username, String resolvedAuthUrl,
				String password) {
			this.conf = conf;
			this.username = username;
			this.resolvedAuthUrl = resolvedAuthUrl;
			this.password = password;
		}

		@Override
		public OpenAIClient getClient() {
			try {
				String token = getToken(resolvedAuthUrl, username, password);
				conf.set(OPENAI_API_KEY, token);
			} catch (IOException e) {
				throw new IllegalArgumentException("Authorization failed for user '" + username + "'", e);
			}

			return super.getClient();
		}
	}

	/**
	 * Configuration key used to override the default token endpoint.
	 */
	public static final String AUTH_URL_PROP_NAME = "AUTH_URL";

	/**
	 * Default OpenID Connect token endpoint for CodeMie.
	 *
	 * <p>
	 * Can be overridden via the {@code AUTH_URL} configuration key.
	 * </p>
	 */
	public static final String AUTH_URL = "https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token";

	/**
	 * Base URL for the CodeMie Code Assistant API.
	 *
	 * <p>
	 * This base URL is used to configure the underlying OpenAI-compatible client.
	 * </p>
	 */
	public static final String BASE_URL = "https://codemie.lab.epam.com/code-assistant-api";

	private static final String[] OPENAI_COMPATIBLE_MODELS_PREFIXES = { "gpt-", "gemini-", "text-embedding-",
			"codemie-text-embedding-",
			"amazon.titan-embed-text-" };

	private static final String[] CLAUDE_COMPATIBLE_MODELS_PREFIXES = { "claude-" };

	private String model;

	/**
	 * Initializes the provider using configuration values.
	 *
	 * <p>
	 * Required configuration keys:
	 * </p>
	 * <ul>
	 * <li>{@code GENAI_USERNAME} – user e-mail or client id.</li>
	 * <li>{@code GENAI_PASSWORD} – password or client secret.</li>
	 * <li>{@code model} – model identifier (for example {@code gpt-4o-mini},
	 * {@code gemini-1.5-pro}, {@code claude-3-5-sonnet}).</li>
	 * </ul>
	 *
	 * <p>
	 * Optional configuration keys:
	 * </p>
	 * <ul>
	 * <li>{@code AUTH_URL} – token endpoint override.</li>
	 * </ul>
	 * 
	 * @param conf configuration source
	 *
	 * @throws IllegalArgumentException if a configuration conflict is detected,
	 *                                  authorization fails, or an unsupported model
	 *                                  is configured
	 */
	@Override
	public void init(String model, Configurator conf) {

		FallbackConfigurator configurator = new FallbackConfigurator(conf);

		String username = configurator.get(AbstractAIProvider.USERNAME_PROP_NAME);
		String password = configurator.get(AbstractAIProvider.PASSWORD_PROP_NAME);
		String resolvedAuthUrl = configurator.get(AUTH_URL_PROP_NAME, AUTH_URL);

		logger.debug("CodeMie Username: {}", username);

		if (Strings.CS.startsWithAny(model, OPENAI_COMPATIBLE_MODELS_PREFIXES)
				|| StringUtils.isBlank(model)) {
			configurator.set(OpenAIProvider.OPENAI_BASE_URL_NAME, BASE_URL);
			System.setProperty(AUTH_URL_PROP_NAME, resolvedAuthUrl);
			OpenAIProvider openAIProvider = new OpenAIProviderExtension(configurator, username, resolvedAuthUrl,
					password);
			provider = openAIProvider;
			setProvider(provider);
		} else if (Strings.CS.startsWithAny(model, CLAUDE_COMPATIBLE_MODELS_PREFIXES)) {
			System.setProperty(AnthropicProvider.ANTHROPIC_BASE_URL, resolvedAuthUrl);
			configurator.set(AnthropicProvider.ANTHROPIC_BASE_URL, BASE_URL);
			provider = new ClaudeProviderExtension(configurator, username, resolvedAuthUrl, password);
			setProvider(provider);
		} else {
			throw new IllegalArgumentException("Unsupported model: '" + model + "'.");
		}

		setProvider(provider);

		super.init(model, configurator);
	}

	/**
	 * Requests an OAuth 2.0 access token from the given token endpoint.
	 *
	 * <p>
	 * The request uses {@code application/x-www-form-urlencoded} and selects the
	 * grant type based on the {@code username} value:
	 * </p>
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

		HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setDoOutput(true);

		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(postData);
		}

		int responseCode = conn.getResponseCode();
		if (responseCode == 200) {
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
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
	 */
	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException("UTF-8 encoding not supported", new IOException(e));
		}
	}

	/**
	 * Requests an embedding vector for the given input text.
	 *
	 * @param text       input to embed
	 * @param dimensions number of dimensions requested from the embedding model
	 * @return embedding as a list of {@code double} values, or {@code null} when
	 *         {@code text} is {@code null}
	 */
	@Override
	public List<Double> embedding(String text, long dimensions) {
		if (provider instanceof EmbeddingProvider) {
			return ((EmbeddingProvider) provider).embedding(text, dimensions);
		}
		throw new IllegalArgumentException("embedding not support for `" + model + "`");
	}

}
