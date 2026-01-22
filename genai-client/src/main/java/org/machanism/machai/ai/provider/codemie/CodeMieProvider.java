package org.machanism.machai.ai.provider.codemie;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.provider.openai.OpenAIProvider;

import com.openai.client.OpenAIClient;

public class CodeMieProvider extends OpenAIProvider {

	public static String url = "https://keycloak.eks-core.aws.main.edp.projects.epam.com/auth/realms/codemie-prod/protocol/openid-connect/token";
	public static String baseUrl = "https://codemie.lab.epam.com/code-assistant-api/v1";

	protected OpenAIClient getClient() {
		String username = System.getProperty("GENAI_USERNAME");
		String password = System.getProperty("GENAI_PASSWORD");
		try {
			String token = getToken(username, password);
			System.setProperty("OPENAI_API_KEY", token);
			System.setProperty("OPENAI_BASE_URL", baseUrl);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return super.getClient();
	}

	public static String getToken(String username, String password) throws IOException {
		String urlParameters = String.format("grant_type=password&client_id=codemie-sdk&username=%s&password=%s",
				username, password);

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
