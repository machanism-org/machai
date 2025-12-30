package org.machanism.machai.core.ai;

import com.openai.models.ChatModel;

public class GenAIProviderManager {

	public static GenAIProvider getProvider(String pickChatModel) {
		return new OpenAIProvider(ChatModel.of(pickChatModel));
	}

}
