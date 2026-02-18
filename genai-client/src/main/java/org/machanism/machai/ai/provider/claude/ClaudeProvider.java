package org.machanism.machai.ai.provider.claude;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient.Builder;

/**
 * Anthropic-backed implementation of MachAI's {@link GenAIProvider}
 * abstraction.
 *
 * <p>
 * This provider adapts the Anthropic Java SDK to MachAI's provider interface.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 0.0.11
 */
public class ClaudeProvider implements GenAIProvider {

	private static Logger logger = LoggerFactory.getLogger(ClaudeProvider.class);

	private AnthropicClient client;
	private Map<String, Function<Object[], Object>> toolMap = new HashMap<>();
	private Usage lastUsage = new Usage(0, 0, 0);
	private String instructions;

	private String model;

	private ResourceBundle promptBundle;

	private File inputsLog;

	private File workingDir;

	@Override
	public void init(Configurator config) {
		String apiKey = config.get("ANTHROPIC_API_KEY");
		String baseUrl = config.get("ANTHROPIC_BASE_URL");
		model = config.get("claudeModel");

		Builder builder = AnthropicOkHttpClient.builder().apiKey(apiKey).baseUrl(baseUrl);
		if (baseUrl != null) {
			builder.baseUrl(baseUrl);
		}
		client = builder.build();
	}

	@Override
	public void prompt(String text) {
	}

	@Override
	public void promptFile(File file, String bundleMessageName) throws IOException {
	}

	@Override
	public void addFile(File file) throws IOException {
	}

	@Override
	public void addFile(URL fileUrl) throws IOException {
		prompt("File URL: " + fileUrl.toString());
	}

	@Override
	public String perform() {
		return null;
	}

	@Override
	public void clear() {
	}

	@Override
	public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
	}

	@Override
	public void instructions(String instructions) {
		this.instructions = instructions;
	}

	public void promptBundle(ResourceBundle promptBundle) {
		this.promptBundle = promptBundle;
	}

	@Override
	public void inputsLog(File inputsLog) {
		this.inputsLog = inputsLog;
	}

	@Override
	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	@Override
	public Usage usage() {
		return lastUsage;
	}

	@Override
	public void close() {
		client.close();
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	@Override
	public List<Float> embedding(String text) {
		return null;
	}
}