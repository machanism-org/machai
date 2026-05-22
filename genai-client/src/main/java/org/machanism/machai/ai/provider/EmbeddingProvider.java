package org.machanism.machai.ai.provider;

import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;

public interface EmbeddingProvider {

	void init(Configurator conf);

	/**
	 * Computes an embedding vector for the provided text.
	 *
	 * @param text       the input text
	 * @param dimensions desired embedding dimensionality (provider-specific)
	 * @return the embedding vector
	 */
	List<Double> embedding(String text, long dimensions);

}
