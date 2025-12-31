package org.machanism.machai.ai.manager;

import org.apache.commons.lang.StringUtils;
import org.machanism.machai.ai.openAI.OpenAIProvider;

import com.openai.models.ChatModel;

public class GenAIProviderManager {

	public static GenAIProvider getProvider(String chatModel) {
		String providerName = StringUtils.substringBefore(chatModel, ":");
		String chatModelName = StringUtils.substringAfter(chatModel, ":");

		if (StringUtils.isBlank(chatModelName)) {
			chatModelName = providerName;
			providerName = "OpenAI";
		}

		if (!StringUtils.equals(providerName, "OpenAI")) {
			throw new IllegalArgumentException("GenAI provider:" + providerName + " is not supported.");
		}
		return new OpenAIProvider(ChatModel.of(chatModelName));
	}

}
