package org.machanism.machai.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.machanism.machai.core.embedding.EmbeddingProvider;

public class SearchBricks {
	public static void main(String[] args) throws IOException {
		try (EmbeddingProvider provider = new EmbeddingProvider("machanism", "bindex")) {
			List<String> bindexList = provider.search(args[0]);
			FileWriter writer = new FileWriter(new File("inputs.txt"));
			IOUtils.write(StringUtils.join(bindexList, "\n\n").getBytes(), writer, "UTF8");
		}
	}
}
