package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.schema.BIndex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.models.ChatModel;

public class EmbeddingBuilder {

	private GenAIProvider provider;
	private String description;

	public EmbeddingBuilder provider(GenAIProvider provider) {
		this.provider = provider;
		return this;
	}

	public EmbeddingBuilder bindex(File file) throws FileNotFoundException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String bindexStr = IOUtils.toString(new FileInputStream(file), "UTF8");
		BIndex bindex = mapper.readValue(bindexStr, BIndex.class);
		return bindex(bindex);
	}

	public EmbeddingBuilder bindex(BIndex bindex) {
		if (bindex != null) {
			description(bindex.getDescription());
		}
		return this;
	}

	private void description(String description) {
		this.description = description;
	}

	public List<Float> build() throws IOException {
		List<Float> embedding = provider.embedding(description);
		return embedding;
	}

}
