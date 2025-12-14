package org.machanism.machai.core.embedding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.machanism.machai.schema.BIndex;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EmbeddingBuilder {

	private EmbeddingProvider provider;
	private BIndex bindex;

	public EmbeddingBuilder provider(EmbeddingProvider provider) {
		this.provider = provider;
		return this;
	}

	public EmbeddingBuilder bindex(File file) throws FileNotFoundException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		try (FileInputStream input = new FileInputStream(file)) {
			String bindexStr = IOUtils.toString(input, "UTF8");
			BIndex bindex = mapper.readValue(bindexStr, BIndex.class);
			return bindex(bindex);
		}
	}

	public EmbeddingBuilder bindex(BIndex bindex) {
		this.bindex = bindex;
		return this;
	}

	public String build() throws IOException {
		return provider.create(bindex);
	}

}
