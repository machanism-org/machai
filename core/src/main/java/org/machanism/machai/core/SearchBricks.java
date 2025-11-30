package org.machanism.machai.core;

import java.io.IOException;

import org.machanism.machai.core.embedding.EmbeddingProvider;

public class SearchBricks {
	public static void main(String[] args) throws IOException {
		try (EmbeddingProvider provider = new EmbeddingProvider("machanism", "bindex")) {
			provider.search(args[0]);
		}
	}
}
