package org.machanism.machai.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.core.bindex.EmbeddingBuilder;
import org.machanism.machai.schema.BIndex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.models.ChatModel;

public class Register {

	public static void main(String[] args) throws IOException {
		GenAIProvider provider = new GenAIProvider(ChatModel.GPT_5);
		provider.setDebugMode(true);
		provider.setRequestDisable(true);

		BIndex bindex = new BIndexBuilder()
				.projectDir(new File("D:\\projects\\machanism.org\\macha\\core\\commons"))
				.provider(provider).build();

		String bindexStr = new ObjectMapper().writeValueAsString(bindex);
		System.out.println(bindexStr);

		List<Float> embeddingBuilder = new EmbeddingBuilder().provider(provider).bindex(bindex).build();
		System.out.println(embeddingBuilder);
	}
}
