package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Usage;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.provider.codemie.CodeMieProvider;
import org.machanism.machai.ai.tools.ToolFunction;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

/**
 * Anthropic-backed implementation of MachAI's {@link Genai} abstraction.
 *
 * <p>
 * This provider adapts the Anthropic Java SDK to MachAI's provider interface.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.1.13
 */
public class ClaudeProvider implements Genai {

	private Configurator config;

	@Override
	public void init(Configurator config) {
		this.config = config;
	}

	@Override
	public void prompt(String text) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public String perform() {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void instructions(String instructions) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	public void promptBundle(ResourceBundle promptBundle) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void inputsLog(File inputsLog) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public void setWorkingDir(File workingDir) {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public Usage usage() {
		throw new UnsupportedOperationException("ClaudeProvider is not implemented yet.");
	}

	@Override
	public List<Double> embedding(String text, long dimensions) {
		return Collections.emptyList();
	}

	protected AnthropicClient getClient() {
		AnthropicClient client = AnthropicOkHttpClient.builder()
				.baseUrl(CodeMieProvider.BASE_URL)
				.authToken(config.get(CodeMieProvider.OPENAI_API_KEY))
				.build();
		return client;
	}

//	public static void main(String[] args) {
//		MessageCreateParams params = MessageCreateParams.builder()
//				.model("claude-sonnet-4-5-20250929")
//				.maxTokens(1024L)
//				.addUserMessage("What is the capital of France?")
//				.build();
//
//		Message response = client.messages().create(params);
//		System.out.println(response.content().get(0).text().get().text());
//	}
}
