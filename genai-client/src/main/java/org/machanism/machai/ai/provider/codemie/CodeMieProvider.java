package org.machanism.machai.ai.provider.codemie;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.openai.OpenAIProvider;

public class CodeMieProvider extends OpenAIProvider {

	public static String authUrl = "https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token";
	public static String baseUrl = "https://codemie.lab.epam.com/code-assistant-api/v1";

	@Override
	public void init(Configurator conf) {
		String username = conf.get("GENAI_USERNAME");
		String password = conf.get("GENAI_PASSWORD");
		String authUrl = conf.get("AUTH_URL", CodeMieProvider.authUrl);

		try {
			String token = getToken(authUrl, username, password);
			super.createClient(baseUrl, token);

		} catch (IOException e) {
			throw new IllegalArgumentException("Authorization failed for user '" + username + "'", e);
		}
	}

	public static String getToken(String url, String username, String password) throws IOException {
		String queryTemplate;
		if (username.contains("@")) {
			queryTemplate = "grant_type=password&client_id=codemie-sdk&username=%s&password=%s";
		} else {
			queryTemplate = "grant_type=client_credentials&client_id=%s&client_secret=%s";
		}

		String urlParameters = String.format(queryTemplate, username, password);

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

				String accessToken = StringUtils.substringBetween(response.toString(), "\"access_token\":\"", "\",");
				return accessToken;
			}
		} else {
			throw new IOException("Failed to obtain token: received HTTP response code " + responseCode);
		}
	}

}
